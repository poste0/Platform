package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.haulmont.cuba.core.global.AppBeans;
import org.springframework.stereotype.Component;

@Component(CameraStatusBean.NAME)
public class CameraStatusBean {
    public static final String NAME = "platform_CameraStatusBean";


    public CameraService.Status getCameraStatus(boolean isConnected, boolean isRecording) {


        if(!isConnected){
            return CameraService.Status.NOT_CONNECTED;
        }
        if(isRecording && isConnected){
            return CameraService.Status.RECORDING;
        }
        if(isConnected && !isRecording){
            return CameraService.Status.CONNECTED;
        }
        return null;
    }
}