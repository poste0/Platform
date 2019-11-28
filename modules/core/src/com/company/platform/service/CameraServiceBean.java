package com.company.platform.service;

import com.company.platform.core.CameraStatusBean;
import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.app.UserSessions;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import org.bytedeco.javacv.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service(CameraService.NAME)
public class CameraServiceBean implements CameraService {


    public FFmpegFrameGrabber getGrabber(String address) throws FrameGrabber.Exception {
        FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(address);
        return grabber;
    }

    public FFmpegFrameRecorder getRecorder(File file, FFmpegFrameGrabber grabber) throws FrameRecorder.Exception {
        FFmpegFrameRecorder recorder = FFmpegFrameRecorder.createDefault(file, grabber.getImageWidth(), grabber.getImageHeight());
        set(grabber, recorder);
        return recorder;
    }

    private void set(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder) {
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
    }

    private class FFMpegFrameWrapper{

        private FFmpegFrameGrabber grabber;

        private FFmpegFrameRecorder recorder;

        private Camera camera;

        private File file;

        private String name;

        private boolean isRecording;

        public FFMpegFrameWrapper(Camera camera){
            this.camera = camera;
            this.name = prepareFile(camera);
            isRecording = false;
        }

        private void createFile(){
            file = new File(name);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        private String prepareFile(Camera item){
            if(Objects.isNull(item)){
                throw new IllegalArgumentException();
            }

            /*File file;
            File path = new File(item.getId().toString());
            if(!path.exists()) {
                path.mkdir();
            }
            file = new File(path.getAbsolutePath() + "/" + LocalDateTime.now() + ".avi");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            File path = new File(item.getId().toString());
            if(!path.exists()) {
                path.mkdir();
            }
            String name = path.getAbsolutePath() + "/" + LocalDateTime.now() + ".avi";
            return name;
        }

        public FFmpegFrameGrabber getGrabber() throws FrameGrabber.Exception {
            if(grabber == null) {
                grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
                grabber.setOption("rtsp_transport", "tcp");
                grabber.setFrameRate(camera.getFrameRate());
                grabber.setImageHeight(camera.getHeight());
                grabber.setImageWidth(camera.getWeight());
            }
            return grabber;
        }

        public FFmpegFrameRecorder getRecorder() throws FrameRecorder.Exception {
            if(recorder == null) {
                recorder = FFmpegFrameRecorder.createDefault(file, grabber.getImageWidth(), grabber.getImageHeight());
            }
            set();
            return recorder;
        }

        private void set(){
            recorder.setVideoCodec(grabber.getVideoCodec());
            recorder.setVideoBitrate(grabber.getVideoBitrate());
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setImageHeight(grabber.getImageHeight() / 4);
            recorder.setImageWidth(grabber.getImageWidth() / 4);
        }

    }

    private Map<UserSession, List<FFMpegFrameWrapper>> ffMpegs;

    private Executor executor;

    private SecurityContext context;

    public CameraServiceBean(){

    }

    public void init(){
        if(ffMpegs == null) {
            ffMpegs = new HashMap<>();
        }
        //context = AppBeans.get(SecurityContext.class);
        List<UserSession> userSessions = getSessions();
        processSessions(userSessions);
        ffMpegs.forEach(new BiConsumer<UserSession, List<FFMpegFrameWrapper>>() {
            @Override
            public void accept(UserSession userSession, List<FFMpegFrameWrapper> ffMpegFrameWrappers) {
                System.out.println(userSession.getUser().getName());
                ffMpegFrameWrappers.forEach(new Consumer<FFMpegFrameWrapper>() {
                    @Override
                    public void accept(FFMpegFrameWrapper ffMpegFrameWrapper) {
                        System.out.println("    " + ffMpegFrameWrapper.camera.getAddress());
                    }
                });
            }
        });
    }

    private List<UserSession> getSessions(){
        UserSessions tempUserSessions = AppBeans.get(UserSessions.NAME);
        List<UserSession> userSessions = tempUserSessions.getUserSessionsStream().collect(Collectors.toList());
        return userSessions;
    }

    private void processSessions(List<UserSession> userSessions){
        DataManager dataManager = AppBeans.get(DataManager.NAME);
        userSessions.stream()
                .filter(userSession -> !ffMpegs.containsKey(userSession))
                .forEach(userSession -> {
                    List<Camera> cameras = dataManager.loadValue("SELECT c FROM platform_Camera c " +
                            "WHERE c.user.id = :user", Camera.class).setParameters(Collections.singletonMap("user", userSession.getUser().getId())).list();
                    List<FFMpegFrameWrapper> wrappers = new ArrayList<>();
                    cameras.forEach(camera -> {
                        wrappers.add(new FFMpegFrameWrapper(camera));
                    });
                    ffMpegs.put(userSession, wrappers);
                });
    }

    public void write(Camera camera) throws FrameGrabber.Exception {
        if(Objects.isNull(camera)){
            throw new IllegalArgumentException();
        }
        FFMpegFrameWrapper wrapper = getWrapper(camera);
        wrapper.createFile();
        wrapper.isRecording = true;
        FFmpegFrameGrabber grabber;
        try {
            grabber = wrapper.getGrabber();
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            wrapper.isRecording = false;
            e.printStackTrace();
            throw new FrameGrabber.Exception("An error while grabbing");
        }

        FFmpegFrameRecorder recorder;
        try {
            recorder = wrapper.getRecorder();
            recorder.start();
        }
        catch(FrameRecorder.Exception e){
            wrapper.isRecording = false;
            throw new FrameGrabber.Exception("An error while recording");
        }



        executor = new ConcurrentTaskExecutor();
        executor.execute(() -> {
            //AppContext.setSecurityContext(context);
            try {
                int q = 0;
                while (wrapper.isRecording) {
                    q++;
                    if(q == 25){
                        System.out.println("w");
                        q = 0;
                    }
                    Frame frame = grabber.grab();
                    recorder.record(frame);
                }


                wrapper.recorder.stop();
                wrapper.grabber.stop();
                wrapper.grabber = null;
                wrapper.recorder = null;
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }

        });

    }

    private FFMpegFrameWrapper getWrapper(Camera camera){
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        List<FFMpegFrameWrapper> wrappers = ffMpegs.get(session);
        FFMpegFrameWrapper wrapper = null;
        for(FFMpegFrameWrapper w: wrappers){
            if(w.camera.getId().equals(camera.getId())){
                wrapper = w;
            }
        }
        return wrapper;
    }

    public void stop(Camera camera) {
        if(Objects.isNull(camera)){
            throw new IllegalArgumentException();
        }
        FFMpegFrameWrapper wrapper = getWrapper(camera);
        wrapper.isRecording = false;

    }

    public boolean isRecording(Camera camera){
        FFMpegFrameWrapper wrapper = getWrapper(camera);
        return wrapper.isRecording;
    }

    public void update(User user, Camera camera){
        FFMpegFrameWrapper wrapper = new FFMpegFrameWrapper(camera);
        UserSession userSession = AppBeans.get(UserSessionSource.class).getUserSession();
        List<FFMpegFrameWrapper> wrappers = ffMpegs.get(userSession);
        wrappers.add(new FFMpegFrameWrapper(camera));


        ffMpegs.put(userSession, wrappers);
    }

    public boolean testConnection(Camera camera){
        try {
            final String[] address = camera.getAddress().split("@")[1].split(":");
            final int port = address.length == 2 ? Integer.parseInt(address[1]) : 0;
            Socket socket = new Socket(address[0], port);
            boolean result = socket.isConnected();
            socket.close();
            return result;
        } catch (IOException e) {
            return false;
        }
    }

    public Status getStatus(Camera camera){
        CameraStatusBean statusBean = AppBeans.get(CameraStatusBean.NAME);

        return statusBean.getCameraStatus(testConnection(camera), isRecording(camera));
    }


}