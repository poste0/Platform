package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.sys.AppContext;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component(FFMpegCaptureStream.NAME)
@Scope("prototype")
public class FFMpegCaptureStream extends AbstractFFMpegCapture {
    public static final String NAME = "platform_FFMpegCaptureStream";

    public FFMpegCaptureStream(Camera camera) throws FrameGrabber.Exception {
        super(camera);
    }

    @Override
    protected File createFile() {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(camera.getName())
                .append(".m3u8");

        String path = pathBuilder.toString();

        File file = new File(path);
        return file;
    }

    @Override
    protected void setUpRecorder(){
        super.setUpRecorder();
        recorder.setOption("f", "hls");
    }

    @Override
    public void stop() {
        isRecording = false;
        while(!isStopped){
            System.out.println("not");
            continue;
        }
        isStopped = false;
        try {
            recorder.stop();
            grabber.stop();
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }
}