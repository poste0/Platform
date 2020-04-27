package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.company.platform.entity.Video;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import org.bytedeco.javacv.*;
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
        //recorder.setOption("g", String.valueOf(grabber.getFrameRate()));
        //recorder.setOption("movflags", "faststart");
        //recorder.setOption("sc_threshold", "0");
        recorder.setOption("crf", "20");
        recorder.setOption("movflags", "faststart");
        recorder.setOption("sc_threshold", "0");
        recorder.setOption("g", String.valueOf(grabber.getFrameRate()));
        recorder.setAudioCodec(grabber.getAudioCodec());
        recorder.setAudioChannels(grabber.getAudioChannels());

    }

    protected void setUpGrabber() throws FrameGrabber.Exception{
        grabber.setOption("rtsp_transport", "tcp");
        //grabber.setOption("vcodec", "copy");
        //grabber.setOption("acodec", "copy");
        grabber.setFrameRate(camera.getFrameRate());
        grabber.setImageHeight(camera.getHeight());
        grabber.setImageWidth(camera.getWeight());
    }

    protected abstract File createFile();

    @Override
    public void process() throws FrameRecorder.Exception, FrameGrabber.Exception{
        isRecording = true;
        setUpGrabber();
        grabber.start();
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
                        System.out.println("1 second");
                        q = 0;
                    }
                    Frame frame = grabber.grab();
                    recorder.record(frame);
                }
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            finally {
                isStopped = true;
            }

        });
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