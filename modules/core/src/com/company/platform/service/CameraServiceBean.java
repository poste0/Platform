package com.company.platform.service;

import com.company.platform.core.CameraStatusBean;
import com.company.platform.core.Capture;
import com.company.platform.core.FFMpegCapture;
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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            this.name = prepareFile(this.camera);
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

    private Map<User, List<Capture>> ffMpegs;

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
        ffMpegs.forEach(new BiConsumer<User, List<Capture>>() {
            @Override
            public void accept(User user, List<Capture> ffMpegFrameWrappers) {
                System.out.println(user.getName());
                ffMpegFrameWrappers.forEach(new Consumer<Capture>() {
                    @Override
                    public void accept(Capture capture) {
                        System.out.println("    " + capture.isRecording());
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
                .filter(userSession -> !ffMpegs.containsKey(userSession.getUser()))
                .forEach(userSession -> {
                    List<Camera> cameras = dataManager.loadValue("SELECT c FROM platform_Camera c " +
                            "WHERE c.user.id = :user", Camera.class).setParameters(Collections.singletonMap("user", userSession.getUser().getId())).list();
                    List<Capture> wrappers = new ArrayList<>();
                    cameras.forEach(camera -> {
                        wrappers.add(AppBeans.getPrototype(FFMpegCapture.NAME, camera));
                    });
                    ffMpegs.put(userSession.getUser(), wrappers);
                });
    }

    public void write(Camera camera) throws FrameGrabber.Exception {
        /*if(Objects.isNull(camera)){
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

         */

        Capture capture = getWrapper(camera);
        try {
            capture.process();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    private Capture getWrapper(Camera camera){
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        List<Capture> wrappers = ffMpegs.get(session.getUser());
        Capture capture = null;
        for(Capture w: wrappers){
            if(w.getCamera().getId().equals(camera.getId())){
                capture = w;
            }
        }
        return capture;
    }

    public void stop(Camera camera) {
        Capture capture = getWrapper(camera);
        capture.stop();

    }

    public boolean isRecording(Camera camera){
        /*FFMpegFrameWrapper wrapper = getWrapper(camera);
        return wrapper.isRecording;
         */
        Capture capture = getWrapper(camera);
        return capture.isRecording();
    }

    public void update(User user, Camera camera){
        FFMpegFrameWrapper wrapper = new FFMpegFrameWrapper(camera);
        UserSession userSession = AppBeans.get(UserSessionSource.class).getUserSession();
        List<Capture> wrappers = ffMpegs.get(userSession.getUser());
        wrappers.add(AppBeans.getPrototype(FFMpegCapture.NAME, camera));


        ffMpegs.put(userSession.getUser(), wrappers);
    }

    public boolean testConnection(Camera camera){
        try(Socket socket = new Socket();) {
            Pattern pattern = Pattern.compile("([0-9]{1,3}[\\.]){3}[0-9]{1,3}");
            Matcher matcher = pattern.matcher(camera.getAddress());
            matcher.find();
            final String address = matcher.group(0);

            int port = 5000;
            final String[] splitAddress = camera.getAddress().split("([0-9]{1,3}[\\.]){3}[0-9]{1,3}");
            if(splitAddress.length == 2){
                if(splitAddress[1].charAt(0) == ':'){
                    if(splitAddress[1].contains("/")){
                        port = Integer.valueOf(splitAddress[1].substring(1).split("/")[0]);
                    }
                    else {
                        port = Integer.valueOf(splitAddress[1].substring(1));
                    }
                }

            }
            System.out.println(address);
            System.out.println(port);

            socket.connect(new InetSocketAddress(address, port), 500);
            boolean result = socket.isConnected();
            socket.close();
            System.out.println(result);
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