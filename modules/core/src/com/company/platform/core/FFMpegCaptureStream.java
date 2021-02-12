package com.company.platform.core;

import com.company.platform.entity.Camera;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;

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

        String path = "file/" +
                camera.getName() +
                ".m3u8";

        this.file = new File(path);
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

        final String delimiter = "/";
        String[] parts = this.file.getPath().split(delimiter);
        StringBuilder tsFileBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            tsFileBuilder.append(parts[i])
                    .append(delimiter);
        }
        File tsFile = new File(tsFileBuilder.toString());
        FileFilter filter = new WildcardFileFilter(camera.getName() + "*.ts");
        File[] files = tsFile.listFiles(filter);
        if(files != null) {
            for (File f : files) {
                try {
                    Files.delete(f.toPath());
                    log.info("Files ts of live stream have been deleted");
                } catch (IOException e) {
                    log.error("Files ts of live stream have not been deleted", e);
                }
            }
        }

        try {
            Files.delete(this.file.toPath());
            log.info("File m3u8 of live stream has been deleted");
        } catch (IOException e) {
            log.error("File m3u8 of live stream has not been deleted", e);
        }
    }
}