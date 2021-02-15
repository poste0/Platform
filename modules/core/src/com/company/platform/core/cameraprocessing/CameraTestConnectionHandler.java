package com.company.platform.core.cameraprocessing;

import com.company.platform.core.CameraStatusBean;
import com.company.platform.core.Capture;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component(CameraTestConnectionHandler.NAME)
public class CameraTestConnectionHandler implements CameraReadWriteHandler{
    public static final String NAME = "platform_CameraTestConnectionHandler";
    private CameraReadWriteHandler next;

    @Inject
    private CameraStatusBean cameraStatusBean;

    @Override
    public boolean handle(Capture capture, String method) {
        if(!cameraStatusBean.testConnection(capture)){
            return false;
        }
        else{
            return next.handle(capture, method);
        }
    }

    public void setNext(CameraReadWriteHandler next) {
        this.next = next;
    }
}
