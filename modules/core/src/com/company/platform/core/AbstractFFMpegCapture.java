package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractFFMpegCapture implements Capture {
    public static final String NAME = "platform_AbstractFFMpegCapture";

    protected FFMpegGrabberBuilder grabberBuilder;

    protected FFMpegRecorderBuilder recorderBuilder;

    protected Camera camera;

    protected AtomicBoolean isRecording;

    protected boolean isStopped = false;

    protected Executor executor;

    protected File file;

    private static final Logger log = LoggerFactory.getLogger(AbstractFFMpegCapture.class);

    public AbstractFFMpegCapture(Camera camera) throws FrameGrabber.Exception {
        this.grabberBuilder = new FFMpegGrabberBuilder(camera.getAddress());
        this.camera = camera;
        this.isRecording = new AtomicBoolean(false);
        this.executor = new ConcurrentTaskExecutor();
    }


    protected void setUpRecorder(){
        recorderBuilder.withCodec()
                .withBitrate()
                .withFrameRate()
                .withHeight(camera.getHeight())
                .withWidth(camera.getWeight())
                .withGopSize()
                .withAudioCodec()
                .withAudioChannels()
                .with("movflags", "faststart")
                .with("crf", "20")
                .with("sc_threshold", "0");

        log.info("Recorder has been set up");
    }

    protected void setUpGrabber() {
        grabberBuilder.withOption("rtsp_transport", "tcp")
                .withWidth(camera.getWeight())
                .withHeight(camera.getHeight());

        log.info("Grabber has been set up");
    }

    protected abstract void createFile();

    @Override
    public void process() throws FrameRecorder.Exception, FrameGrabber.Exception{
        isRecording.set(true);
        setUpGrabber();
        startGrabber();
        createFile();
        FFmpegFrameGrabber grabber = grabberBuilder.build();
        recorderBuilder = new FFMpegRecorderBuilder(file, grabber);
        setUpRecorder();
        recorderBuilder.build().start();
        final SecurityContext context = AppContext.getSecurityContext();
        executor.execute(() -> {
            AppContext.setSecurityContext(context);
            try {
                int q = 0;
                while (isRecording.get()) {
                    q++;
                    if(q == grabber.getFrameRate()){
                        log.info("1 second has been recorder");
                        q = 0;
                    }
                    Frame frame = grabber.grab();
                    recorderBuilder.build().record(frame);
                }
            } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
                log.error("Recording has errors", e);
            } finally {
                log.info("Recording has stopped");
                isStopped = true;
            }

        });
    }

    private void startGrabber() throws FrameGrabber.Exception {
        FFmpegFrameGrabber grabber = grabberBuilder.build();
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

                grabberBuilder = new FFMpegGrabberBuilder(camera.getAddress());
                grabber = grabberBuilder.build();
                setUpGrabber();
                createFile();
                recorderBuilder = new FFMpegRecorderBuilder(file, grabber);
                setUpRecorder();
                grabber.start();

                log.info("Grabber has started");
            }
        } catch (FrameGrabber.Exception e) {
            log.error("Grabber has not started", e);
        }



    }

    public boolean isRecording(){
        return isRecording.get();
    }

    public void setCamera(Camera camera){
        this.camera = camera;
    }

    public Camera getCamera(){
        return this.camera;
    }

    public void stop(){
        isRecording.set(false);
        while(!isStopped){
            log.info("Recording is being stopped");
        }
        isStopped = false;
        try {
            recorderBuilder.build().stop();
            grabberBuilder.build().stop();

            log.info("Recording has stopped");
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            log.error("Recording has not stopped", e);
        }
    }


}