package com.company.platform.service;

import com.company.platform.entity.Camera;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;

public interface CameraService {
    String NAME = "platform_CameraService";

    //FFmpegFrameGrabber getGrabber(String address) throws FrameGrabber.Exception;

    //FFmpegFrameRecorder getRecorder(File file, FFmpegFrameGrabber grabber) throws FrameRecorder.Exception;

    void write(Camera camera);

    void stop(Camera camera);


}