package com.company.platform.service;

import com.company.platform.entity.Camera;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

import java.io.File;

@Service(CameraService.NAME)
public class CameraServiceBean implements CameraService {

    public FFmpegFrameGrabber getGrabber(String address) throws FrameGrabber.Exception {
        FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(address);
        return grabber;
    }

    public FFmpegFrameRecorder getRecorder(File file, FFmpegFrameGrabber grabber) throws FrameRecorder.Exception {
        FFmpegFrameRecorder recorder = FFmpegFrameRecorder.createDefault(file, grabber.getImageWidth(), grabber.getImageHeight());
        set(grabber, recorder);
        return recorder;
    }

    private void set(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder) {
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
    }



}