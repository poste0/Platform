package com.company.platform.service;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.security.global.UserSession;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

public interface CameraService {
    String NAME = "platform_CameraService";

    //FFmpegFrameGrabber getGrabber(String address) throws FrameGrabber.Exception;

    //FFmpegFrameRecorder getRecorder(File file, FFmpegFrameGrabber grabber) throws FrameRecorder.Exception;

    void write(Camera camera) throws FrameGrabber.Exception, FrameRecorder.Exception;

    void stop(Camera camera) throws FrameRecorder.Exception, FrameGrabber.Exception;

    void init();

    boolean isRecording(Camera camera);
}