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
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Component(FFMpegCapture.NAME)
@Scope("prototype")
public class FFMpegCapture extends AbstractFFMpegCapture {
    public static final String NAME = "platform_FFMpegCapture";


    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Inject
    private FileLoader fileLoader;



    public FFMpegCapture(Camera camera) throws FrameGrabber.Exception {
        super(camera);
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


    @Override
    protected File createFile(){
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
        after();
    }


}