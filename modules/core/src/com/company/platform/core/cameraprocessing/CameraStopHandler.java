package com.company.platform.core.cameraprocessing;

import com.company.platform.core.Capture;
import org.springframework.stereotype.Component;

@Component(CameraStopHandler.NAME)
public class CameraStopHandler implements CameraReadWriteHandler{
    public static final String NAME = "platform_CameraStopHandler";

    @Override
    public boolean handle(Capture capture, String method) {
        if(method.equals("STOP")){
            return capture.isRecording();
        }
        return false;
    }

    @Override
    public void setNext(CameraReadWriteHandler handler) {

    }
}
