package com.company.platform.core.cameraprocessing;

import com.company.platform.core.Capture;
import com.haulmont.cuba.core.global.AppBeans;

public interface CameraReadWriteHandler {
    boolean handle(Capture capture, String method);

    void setNext(CameraReadWriteHandler handler);

    static CameraReadWriteHandler getDefaultHandler(){
        CameraReadWriteHandler testConnectionHandler = AppBeans.get(CameraTestConnectionHandler.NAME);
        CameraReadWriteHandler writeHandler = AppBeans.get(CameraWriteHandler.NAME);
        CameraReadWriteHandler stopHandler = AppBeans.get(CameraStopHandler.NAME);
        testConnectionHandler.setNext(writeHandler);
        writeHandler.setNext(stopHandler);

        return testConnectionHandler;
    }
}
