package com.company.platform.service;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.app.UserSessions;
import com.haulmont.cuba.security.global.UserSession;
import org.bytedeco.javacv.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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

    private Map<UserSession, Map<Camera, FFMpegFrameWrapper>> ffMpegs;

    private Executor executor;

    private SecurityContext context;

    public CameraServiceBean(){

    }

    public void init(){
        if(ffMpegs == null) {
            ffMpegs = new HashMap<>();
        }
        //context = AppBeans.get(SecurityContext.class);
        UserSessions tempUserSessions = AppBeans.get(UserSessions.NAME);
        List<UserSession> userSessions = tempUserSessions.getUserSessionsStream().collect(Collectors.toList());


        DataManager dataManager = AppBeans.get(DataManager.NAME);
        userSessions.stream()
                .filter(userSession -> !ffMpegs.containsKey(userSession))
                .forEach(userSession -> {
                    List<Camera> cameras = dataManager.loadValue("SELECT c FROM platform_Camera c " +
                            "WHERE c.user.id = :user", Camera.class).setParameters(Collections.singletonMap("user", userSession.getUser().getId())).list();
                    Map<Camera, FFMpegFrameWrapper> ffMpegMap = new HashMap<>();
                    cameras.forEach(camera -> ffMpegMap.put(camera, new FFMpegFrameWrapper(camera)));
                    ffMpegs.put(userSession, ffMpegMap);
                });

        ffMpegs.forEach(new BiConsumer<UserSession, Map<Camera, FFMpegFrameWrapper>>() {
            @Override
            public void accept(UserSession userSession, Map<Camera, FFMpegFrameWrapper> cameraFFMpegFrameWrapperMap) {
                cameraFFMpegFrameWrapperMap.forEach(new BiConsumer<Camera, FFMpegFrameWrapper>() {
                    @Override
                    public void accept(Camera camera, FFMpegFrameWrapper ffMpegFrameWrapper) {
                        System.out.println("asdasdasd");
                    }
                });
            }
        });
    }

    public void write(Camera camera) throws FrameGrabber.Exception {
        if(Objects.isNull(camera)){
            throw new IllegalArgumentException();
        }

        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        Map<Camera, FFMpegFrameWrapper> cameraMap = ffMpegs.get(session);
        FFMpegFrameWrapper wrapper = cameraMap.get(camera);

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


            System.out.println(124124124);
        });

    }

    public void stop(Camera camera) {
        if(Objects.isNull(camera)){
            throw new IllegalArgumentException();
        }
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        Map<Camera, FFMpegFrameWrapper> cameraMap = ffMpegs.get(session);

        FFMpegFrameWrapper wrapper = cameraMap.get(camera);
        wrapper.isRecording = false;

    }

    public boolean isRecording(Camera camera){
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        Map<Camera, FFMpegFrameWrapper> cameraMap = ffMpegs.get(session);

        FFMpegFrameWrapper wrapper = cameraMap.get(camera);
        return wrapper.isRecording;
    }



}