package com.company.platform.core;

import com.company.platform.entity.Camera;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(Capture.NAME)
@Scope("prototype")
public interface Capture {
    public static final String NAME = "platform_Capture";

    void process() throws FrameGrabber.Exception, FrameRecorder.Exception;

    void stop();

    void setCamera(Camera camera);

    Camera getCamera();

    boolean isRecording();
}