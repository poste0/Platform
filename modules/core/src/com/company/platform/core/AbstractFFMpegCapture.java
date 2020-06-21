package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.company.platform.entity.Video;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

//@Component(AbstractFFMpegCapture.NAME)
public abstract class AbstractFFMpegCapture implements Capture {
    public static final String NAME = "platform_AbstractFFMpegCapture";

    protected FFmpegFrameGrabber grabber;

    protected FFmpegFrameRecorder recorder;

    protected Camera camera;

    protected boolean isRecording;

    protected boolean isStopped = false;

    protected Executor executor;

    protected File file;

    private static final Logger log = LoggerFactory.getLogger(AbstractFFMpegCapture.class);

    public AbstractFFMpegCapture(Camera camera) throws FrameGrabber.Exception {
        this.grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
        this.camera = camera;
        this.isRecording = false;
        this.executor = new ConcurrentTaskExecutor();
    }


    protected void setUpRecorder(){
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setImageHeight(camera.getHeight());
        recorder.setImageWidth(camera.getWeight());
        recorder.setGopSize((int) grabber.getFrameRate());
        recorder.setAudioCodec(grabber.getAudioCodec());
        recorder.setAudioChannels(grabber.getAudioChannels());
        recorder.setOption("movflags", "faststart");
        recorder.setOption("crf", "20");
        recorder.setOption("sc_threshold", "0");

        log.info("Recorder has been set up");
    }

    protected void setUpGrabber() throws FrameGrabber.Exception{
        grabber.setOption("rtsp_transport", "tcp");
        grabber.setImageHeight(camera.getHeight());
        grabber.setImageWidth(camera.getWeight());

        log.info("Grabber has been set up");
    }

    protected abstract File createFile();

    @Override
    public void process() throws FrameRecorder.Exception, FrameGrabber.Exception{
        isRecording = true;
        setUpGrabber();
        startGrabber();
        File file = createFile();
        recorder = new HlsRecorder(file, grabber.getImageWidth(), grabber.getImageHeight());
        setUpRecorder();
        recorder.start();
        final SecurityContext context = AppContext.getSecurityContext();
        executor.execute(() -> {
            AppContext.setSecurityContext(context);
            try {
                int q = 0;
                while (isRecording) {
                    q++;
                    if(q == grabber.getFrameRate()){
                        log.info("1 second has been recorder");
                        q = 0;
                    }
                    Frame frame = grabber.grab();
                    recorder.record(frame);
                }
            } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
                log.error("Recording has errors");
                e.printStackTrace();
            } finally {
                log.info("Recording has stopped");
                isStopped = true;
            }

        });
    }

    private void startGrabber() throws FrameGrabber.Exception {
        grabber.start();

        int failCounter = 0;
        try {
            while(grabber.getFrameRate() != camera.getFrameRate()){
                grabber.stop();

                failCounter ++;
                if(failCounter > 10){
                    log.error("Frame rate of the camera != frame rate of the grabber");
                    throw new IllegalStateException("Frame rate of the camera is not equal to the rate of the grabber. May be the frame rate of the camera is wrong.");
                }

                grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
                setUpGrabber();
                grabber.start();

                log.info("Grabber has started");
            }
        } catch (FrameGrabber.Exception e) {
            log.error("Grabber has not started");
            e.printStackTrace();
        }



    }

    public boolean isRecording(){
        return isRecording;
    }

    public void setCamera(Camera camera){
        this.camera = camera;
    }

    public Camera getCamera(){
        return this.camera;
    }

    public void stop(){
        isRecording = false;
        while(!isStopped){
            log.info("Recording is being stopped");
            continue;
        }
        isStopped = false;
        try {
            recorder.stop();
            grabber.stop();

            log.info("Recording has stopped");
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            log.error("Recording has not stopped");
            e.printStackTrace();
        }
    }


}