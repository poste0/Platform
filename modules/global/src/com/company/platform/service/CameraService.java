package com.company.platform.service;

import com.company.platform.entity.Camera;

import java.util.List;

public interface CameraService {
    String NAME = "platform_CameraService";

    enum Status{
        NOT_CONNECTED, CONNECTED, RECORDING
    }

    void write(Camera camera) throws IllegalStateException;

    void stop(Camera camera) throws IllegalStateException;

    void init();

    @Deprecated
    boolean isRecording(Camera camera);

    void update(Camera camera);

    @Deprecated
    boolean testConnection(Camera camera);

    Status getStatus(Camera camera);

    List<Camera> getCameras() throws IllegalStateException;
}