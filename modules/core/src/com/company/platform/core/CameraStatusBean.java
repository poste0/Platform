package com.company.platform.core;

import com.company.platform.service.CameraService;
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
}