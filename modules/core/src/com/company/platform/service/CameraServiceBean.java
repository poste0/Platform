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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

        private boolean isRecording;

        public FFMpegFrameWrapper(Camera camera){
            this.camera = camera;
            this.file = prepareFile(camera);
            isRecording = false;
        }

        private File prepareFile(Camera item){
            if(Objects.isNull(item)){
                throw new IllegalArgumentException();
            }

            File file;
            File path = new File(item.getId().toString());
            if(!path.exists()) {
                path.mkdir();
            }
            file = new File(path.getAbsolutePath() + "/" + LocalDateTime.now() + ".avi");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }

        public FFmpegFrameGrabber getGrabber() throws FrameGrabber.Exception {
            if(grabber == null) {
                grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
                grabber.setOption("rtsp_transport", "tcp");
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

        FFmpegFrameGrabber grabber;
        try {
            grabber = wrapper.getGrabber();
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            throw new FrameGrabber.Exception("An error while grabbing");
        }

        FFmpegFrameRecorder recorder;
        try {
            recorder = wrapper.getRecorder();
            recorder.start();
        }
        catch(FrameRecorder.Exception e){
            throw new FrameGrabber.Exception("An error while recording");
        }


        executor = new ConcurrentTaskExecutor();
        executor.execute(() -> {
            //AppContext.setSecurityContext(context);
            wrapper.isRecording = true;
            try {
                while (wrapper.isRecording) {

                    Frame frame = grabber.grab();
                    recorder.record(frame);

                }


                wrapper.recorder.stop();
                wrapper.grabber.stop();
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
            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
            grabber.start();
            grabber.stop();
            return true;
        } catch (FrameGrabber.Exception e) {
            return false;
        }
    }

    public Status getStatus(Camera camera){
        CameraStatusBean statusBean = AppBeans.get(CameraStatusBean.NAME);

        return statusBean.getCameraStatus(camera, testConnection(camera), isRecording(camera));
    }


}