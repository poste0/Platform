package com.company.platform.core.cameraprocessing;

import com.company.platform.core.Capture;
import org.springframework.stereotype.Component;

@Component(CameraWriteHandler.NAME)
public class CameraWriteHandler implements CameraReadWriteHandler {
    public static final String NAME = "platform_CameraWriteHandler";
    private CameraReadWriteHandler next;

    @Override
    public boolean handle(Capture capture, String method) {
        if(method.equals("WRITE")){
            return !capture.isRecording();
        }
        return next.handle(capture, method);
    }

    public void setNext(CameraReadWriteHandler next) {
        this.next = next;
    }
}
