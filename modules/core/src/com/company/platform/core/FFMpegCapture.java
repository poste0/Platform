package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.company.platform.entity.Video;
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
import java.util.List;
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

    private void after(){
        FileDescriptor descriptor = metadata.create(FileDescriptor.class);
        DataManager manager = AppBeans.get(DataManager.class);
        long count = manager.loadValue("SELECT f FROM sys$FileDescriptor f", FileDescriptor.class).list().stream().count();
        String name = file.getName();
        descriptor.setName(name);
        descriptor.setExtension("mp4");
        descriptor.setCreateDate(new Date());
        descriptor.setSize(this.file.length());
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

        Video video = new Video();
        video.setName(file.getName());
        video.setFileDescriptor(descriptor);
        video.setCamera(camera);
        video.setStatus("ready");
        video.setParentVideo(video);

        dataManager.commit(video);
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
        List<Video> videos = dataManager.loadList(LoadContext.create(Video.class).setQuery(LoadContext.createQuery("SELECT v FROM platform_Video v WHERE v.createdBy= :user").setParameter("user", camera.getCreatedBy())));
        post = String.valueOf(videos.size() + 1);
        String name = path.getAbsolutePath() + "/" + camera.getUser().getLogin() + "_" + post  + ".mp4";
            return name;
    }
    @Override
    public void stop() {
        super.stop();
        after();
    }


}