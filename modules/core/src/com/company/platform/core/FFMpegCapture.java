package com.company.platform.core;

import com.company.platform.entity.Camera;
import org.bytedeco.javacv.*;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executor;

@Component(FFMpegCapture.NAME)
@Scope("prototype")
public class FFMpegCapture implements Capture {
    public static final String NAME = "platform_FFMpegCapture";

    private FFmpegFrameGrabber grabber;

    private FFmpegFrameRecorder recorder;

    private Camera camera;

    private boolean isRecording;

    private Executor executor;

    public FFMpegCapture(Camera camera) throws FrameGrabber.Exception {
        this.grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
        this.camera = camera;
        this.isRecording = false;
        this.executor = new ConcurrentTaskExecutor();
    }

    @Override
    public void process() throws FrameGrabber.Exception, FrameRecorder.Exception {
        isRecording = true;
        setUpGrabber();
        grabber.start();
        File file = createFile();
        recorder = FFmpegFrameRecorder.createDefault(file, grabber.getImageWidth(), grabber.getImageHeight());
        setUpRecorder();
        recorder.start();
        executor.execute(() -> {
            try {
                int q = 0;
                while (isRecording) {
                    q++;
                    if(q == 25){
                        System.out.println("w");
                        q = 0;
                    }
                    Frame frame = grabber.grab();
                    recorder.record(frame);
                }


                recorder.stop();
                grabber.stop();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }

        });

    }

    private void setUpRecorder(){
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setImageHeight(grabber.getImageHeight() / 4);
        recorder.setImageWidth(grabber.getImageWidth() / 4);
    }

    private void setUpGrabber() throws FrameGrabber.Exception {
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setFrameRate(camera.getFrameRate());
            grabber.setImageHeight(camera.getHeight());
            grabber.setImageWidth(camera.getWeight());
    }
    private File createFile(){
        String name = prepareFile();
        File file = new File(name);
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String prepareFile(){
            if(Objects.isNull(camera)){
                throw new IllegalArgumentException();
            }

            /*File file;
            File path = new File(item.getId().toString());
            if(!path.exists()) {
                path.mkdir();
            }
            file = new File(path.getAbsolutePath() + "/" + LocalDateTime.now() + ".avi");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            File path = new File(camera.getId().toString());
            if(!path.exists()) {
                path.mkdir();
            }
        String post = "";
        try {
            post = String.valueOf(Files.walk(path.toPath(), FileVisitOption.FOLLOW_LINKS).filter(path1 -> {
                return path1.toFile().getName().contains(".avi");
            }).count());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = path.getAbsolutePath() + "/" + camera.getCreatedBy() + post  + ".avi";
            return name;
    }
    @Override
    public void stop() {
        isRecording = false;
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
}