package com.company.platform.service;

import com.company.platform.entity.Camera;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

import java.io.File;

@Service(CameraService.NAME)
public class CameraServiceBean implements CameraService {
    private FFmpegFrameGrabber grabber;

    private FFmpegFrameRecorder recorder;

    private Camera camera;

    private boolean isRecording = false;

    @Override
    public boolean testConnection() throws FrameGrabber.Exception {
        grabber = new FFmpegFrameGrabber(camera.getAddress());
        grabber.start();
        boolean result = grabber.hasVideo();
        grabber.stop();
        grabber = null;
        return result;
    }

    private void set() {
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
    }

    public void start(File file) throws FrameRecorder.Exception, FrameGrabber.Exception {
        Thread thread;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isRecording = true;
                grabber = new FFmpegFrameGrabber(camera.getAddress());
                grabber.setOption("rtsp_transport", "tcp");
                try {
                    grabber.start();
                    recorder = new FFmpegFrameRecorder(file, grabber.getImageWidth(), grabber.getImageHeight());
                    set();
                    recorder.start();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.run();


    }
    @Override
    public void write(File file) throws FrameGrabber.Exception, FrameRecorder.Exception {
        while(isRecording) {
            Frame frame = grabber.grab();
            recorder.record(frame);
        }



    }

    @Override
    public void stop() throws FrameRecorder.Exception, FrameGrabber.Exception {
        recorder.stop();
        grabber.stop();
        isRecording = false;
    }

    @Override
    public void setCamera(Camera camera){
        this.camera = camera;
    }

    @Override
    public Camera getCamera(){
        return camera;
    }


    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

}