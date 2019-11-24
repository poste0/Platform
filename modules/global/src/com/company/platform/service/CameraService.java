package com.company.platform.service;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

public interface CameraService {
    String NAME = "platform_CameraService";

    //FFmpegFrameGrabber getGrabber(String address) throws FrameGrabber.Exception;

    //FFmpegFrameRecorder getRecorder(File file, FFmpegFrameGrabber grabber) throws FrameRecorder.Exception;

    void write(Camera camera) throws FrameGrabber.Exception, FrameRecorder.Exception;

    void stop(Camera camera);

    void init();

    boolean isRecording(Camera camera);

    void update(User user, Camera camera);

    boolean testConnection(Camera camera);
}