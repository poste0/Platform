package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import org.bytedeco.javacv.*;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.Date;
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

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Inject
    private FileLoader fileLoader;

    private File file;

    public FFMpegCapture(Camera camera) throws FrameGrabber.Exception {
        this.grabber = FFmpegFrameGrabber.createDefault(camera.getAddress());
        this.camera = camera;
        this.isRecording = false;
        this.executor = new ConcurrentTaskExecutor();
    }

    private void startGrabber(){
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        if (grabber.getFrameRate() != camera.getFrameRate()){
            System.out.println("Change frame rate");
        }
    }
    @Override
    public void process() throws FrameGrabber.Exception, FrameRecorder.Exception {
        isRecording = true;
        setUpGrabber();
        startGrabber();
        File file = createFile();
        recorder = FFmpegFrameRecorder.createDefault(file, grabber.getImageWidth(), grabber.getImageHeight());
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


                recorder.stop();
                grabber.stop();
                after();
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }

        });

    }

    private void after(){
        FileDescriptor descriptor = metadata.create(FileDescriptor.class);
        DataManager manager = AppBeans.get(DataManager.class);
        long count = manager.loadValue("SELECT f FROM sys$FileDescriptor f", FileDescriptor.class).list().stream().count();
        String name = camera.getName();
        if(count > 0){
            name += count;
        }
        descriptor.setName(name);
        descriptor.setExtension("mp4");
        descriptor.setCreateDate(new Date());
        descriptor.setSize(this.file.getTotalSpace());
        try {
            fileLoader.saveStream(descriptor, () -> {
                try {
                    return new FileInputStream(this.file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (FileStorageException e) {
            e.printStackTrace();
        }

        dataManager.commit(descriptor);
        deleteFile();
    }

    private void deleteFile(){
        this.file.delete();
    }

    private void setUpRecorder(){
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setImageHeight(camera.getHeight());
        recorder.setImageWidth(camera.getWeight());
    }

    private void setUpGrabber() throws FrameGrabber.Exception {
            grabber.setOption("rtsp_transport", "tcp");
            grabber.setOption("vcodec", "copy");
            grabber.setOption("acodec", "copy");
            grabber.setOption("crf", "20");
            grabber.setFrameRate(camera.getFrameRate());
            grabber.setImageHeight(camera.getHeight());
            grabber.setImageWidth(camera.getWeight());
    }
    private File createFile(){
        String name = prepareFile();
        File file = new File(name);
        try {
            file.createNewFile();
            this.file = file;
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

            File path = new File(camera.getName().toString());
            if(!path.exists()) {
                path.mkdir();
            }
        String post = "";
        try {
            post = String.valueOf(Files.walk(path.toPath(), FileVisitOption.FOLLOW_LINKS).filter(path1 -> {
                return path1.toFile().getName().contains(".mp4");
            }).count());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = path.getAbsolutePath() + "/" + camera.getCreatedBy() + post  + ".mp4";
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