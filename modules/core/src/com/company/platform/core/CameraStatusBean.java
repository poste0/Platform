package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(CameraStatusBean.NAME)
public class CameraStatusBean {
    public static final String NAME = "platform_CameraStatusBean";

    private static final Logger log = LoggerFactory.getLogger(CameraStatusBean.class);

    public CameraService.Status getCameraStatus(boolean isConnected, boolean isRecording) {
        if(!isConnected){
            log.info("Camera is not connected");
            return CameraService.Status.NOT_CONNECTED;
        }
        else if(isRecording){
            log.info("Camera is being recorder");
            return CameraService.Status.RECORDING;
        }
        log.info("Camera is connected");
        return CameraService.Status.CONNECTED;
    }

    CameraService.Status getCameraStatus(Capture capture){
        return getCameraStatus(testConnection(capture), capture.isRecording());
    }

    public boolean testConnection(Capture capture){
        try {
            Camera camera = capture.getCamera();
            FFMpegGrabberBuilder grabberBuilder = new FFMpegGrabberBuilder(camera.getAddress());
            grabberBuilder.withOption("rtsp_transport", "tcp");
            grabberBuilder.build().start();
            grabberBuilder.build().stop();
        } catch (FrameGrabber.Exception e) {
            return false;
        }
        return true;
    }
}