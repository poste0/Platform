package com.company.platform.core;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.sys.AppContext;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Component(FFMpegCaptureStream.NAME)
@Scope("prototype")
public class FFMpegCaptureStream extends AbstractFFMpegCapture {
    public static final String NAME = "platform_FFMpegCaptureStream";

    private static final Logger log = LoggerFactory.getLogger(FFMpegCaptureStream.class);

    public FFMpegCaptureStream(Camera camera) throws FrameGrabber.Exception {
        super(camera);
    }

    @Override
    protected void createFile() {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("file/")
                .append(camera.getName())
                .append(".m3u8");

        String path = pathBuilder.toString();

        File file = new File(path);
        this.file = file;
        log.info("File has been created");
    }

    @Override
    protected void setUpRecorder(){
        super.setUpRecorder();
        int hw = Math.max(camera.getHeight(), camera.getWeight());
        hw = hw / 500;

        recorderBuilder.with("f", "hls")
                .with("hls_time", "4")
                .withHeight(camera.getHeight() / hw)
                .withWidth(camera.getWeight() / hw);

        log.info("Recorder has been set up");

    }

    @Override
    public void stop(){
        super.stop();

        final String dilimeter = "/";
        String[] parts = this.file.getPath().split(dilimeter);
        StringBuilder tsFileBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            tsFileBuilder.append(parts[i])
                    .append(dilimeter);
        }
        File tsFile = new File(tsFileBuilder.toString());
        FileFilter filter = new WildcardFileFilter(camera.getName() + "*.ts");
        File[] files = tsFile.listFiles(filter);
        for(File f: files){
            try {
                Files.delete(f.toPath());
                log.info("Files ts of live stream have been deleted");
            } catch (IOException e) {
                log.error("Files ts of live stream have not been deleted");
                e.printStackTrace();
            }
        }

        try {
            Files.delete(this.file.toPath());
            log.info("File m3u8 of live stream has been deleted");
        } catch (IOException e) {
            log.error("File m3u8 of live stream has not been deleted");
            e.printStackTrace();
        }


    }
}