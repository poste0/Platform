package com.company.platform.service;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.app.UserSessions;
import com.haulmont.cuba.security.global.UserSession;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

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

        public FFMpegFrameWrapper(Camera camera){
            this.camera = camera;
            this.file = prepareFile(camera);
        }

        private File prepareFile(Camera item){
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
            grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
            return grabber;
        }

        public FFmpegFrameRecorder getRecorder() throws FrameRecorder.Exception {
            recorder = FFmpegFrameRecorder.createDefault(file, grabber.getImageWidth(), grabber.getImageHeight());
            return recorder;
        }
    }

    private Map<UserSession, Map<Camera, FFMpegFrameWrapper>> ffMpegs;

    public CameraServiceBean(){

    }

    public void write(Camera camera){

    }

    public void stop(Camera camera){

    }



}