package com.company.platform.service;

import com.company.platform.entity.Camera;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

public interface CameraService {
    String NAME = "platform_CameraService";

    boolean testConnection() throws FrameGrabber.Exception;

    void start(File file) throws FrameRecorder.Exception, FrameGrabber.Exception;

    void write(File file) throws FrameGrabber.Exception, FrameRecorder.Exception;

    void stop() throws FrameRecorder.Exception, FrameGrabber.Exception;

    void setCamera(Camera camera);

    Camera getCamera();

    boolean isRecording();

}