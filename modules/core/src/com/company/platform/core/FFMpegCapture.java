package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.company.platform.entity.Video;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component(FFMpegCapture.NAME)
@Scope("prototype")
public class FFMpegCapture extends AbstractFFMpegCapture {
    public static final String NAME = "platform_FFMpegCapture";


    private final Metadata metadata;

    private final DataManager dataManager;

    private final FileLoader fileLoader;

    private static final Logger log = LoggerFactory.getLogger(FFMpegCapture.class);


    public FFMpegCapture(Camera camera, Metadata metadata, DataManager dataManager, FileLoader fileLoader) throws FrameGrabber.Exception {
        super(camera);
        this.metadata = metadata;
        this.dataManager = dataManager;
        this.fileLoader = fileLoader;
    }

    private void after(){
        FileDescriptor descriptor = metadata.create(FileDescriptor.class);
        String name = file.getName();
        descriptor.setName(name);
        descriptor.setExtension("mp4");
        descriptor.setCreateDate(new Date());
        descriptor.setSize(this.file.length());
        try {
            log.info("File is being saved after recording");
            fileLoader.saveStream(descriptor, () -> {
                try {
                    return new FileInputStream(this.file);
                } catch (FileNotFoundException e) {
                    log.error("File has not been saved", e);
                }
                return null;
            });
        } catch (FileStorageException e) {
            log.error("File has not been saved", e);
        }
        finally {
            try {
                deleteFile();
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            }
            log.info("Temp file has been deleted");
        }

        dataManager.commit(descriptor);

        Video video = dataManager.create(Video.class);
        video.setName(file.getName());
        video.setFileDescriptor(descriptor);
        video.setCamera(camera);
        video.setStatus("ready");
        video.setParentVideo(video);

        dataManager.commit(video);
        log.info("Video has been committed");
    }

    private void deleteFile() throws IOException {
        boolean isDeleted = this.file.delete();
        if(!isDeleted){
            throw new IOException("The file can't be deleted");
        }
    }

    @Override
    protected void createFile(){
        try {
            String name = prepareFile();
            File file = new File(name);

            boolean isFileAlreadyExist = file.createNewFile();
            this.file = file;
            log.info("File has been created");
            if(isFileAlreadyExist){
                log.warn("The file already exists");
            }
        } catch (IOException e) {
            log.error("File has not been created", e);
        }
    }

    private String prepareFile() throws IOException {
        if(Objects.isNull(camera)){
            log.error("Camera is null");
            throw new IllegalArgumentException();
        }

        File path = new File(camera.getName());
        if(!path.exists()) {
            log.info("Path is not exists");
            boolean isCreated = path.mkdir();
            if(!isCreated){
                throw new IOException("Teh directory is not created");
            }
        }

        String post;
        String getVideoQuery = "SELECT v FROM platform_Video v WHERE v.createdBy = :user";
        List<Video> videos = dataManager.loadList(LoadContext.create(Video.class)
                .setQuery(LoadContext.createQuery(getVideoQuery)
                        .setParameter("user", camera.getCreatedBy())));
        post = String.valueOf(videos.size() + 1);
        String name = path.getAbsolutePath() + "/" + camera.getUser().getLogin() + "_" + post  + ".mp4";
        log.info("File name {}", name);
        return name;
    }
    @Override
    public void stop() {
        log.info("Recording has stopped");
        super.stop();
        after();
    }


}