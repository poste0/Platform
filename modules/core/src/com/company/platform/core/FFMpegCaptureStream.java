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

@Component(FFMpegCaptureStream.NAME)
@Scope("prototype")
public class FFMpegCaptureStream extends AbstractFFMpegCapture {
    public static final String NAME = "platform_FFMpegCaptureStream";

    private static final Logger log = LoggerFactory.getLogger(FFMpegCaptureStream.class);

    public FFMpegCaptureStream(Camera camera) throws FrameGrabber.Exception {
        super(camera);
    }

    @Override
    protected File createFile() {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("file/")
                .append(camera.getName())
                .append(".m3u8");

        String path = pathBuilder.toString();

        File file = new File(path);
        this.file = file;
        log.info("File has been created");
        return file;
    }

    @Override
    protected void setUpRecorder(){
        super.setUpRecorder();
        recorder.setOption("f", "hls");
        recorder.setOption("hls_time", "4");

        int hw = Math.max(camera.getHeight(), camera.getWeight());
        hw = hw / 500;
        recorder.setImageHeight(camera.getHeight() / hw);
        recorder.setImageWidth(camera.getWeight() / hw);

        log.info("Recorder has been set up");

    }

    @Override
    public void stop(){
        super.stop();

        File file = new File(".");
        FileFilter filter = new WildcardFileFilter(camera.getName() + "*.ts");
        File[] files = file.listFiles(filter);
        for(File f: files){
            try {
                Files.delete(f.toPath());
                log.info("Files ts of live stream have been deleted");
            } catch (IOException e) {
                log.error("Files ts of live stream have not been deleted");
                e.printStackTrace();
            }
        }

        file = new File(camera.getName() + ".m3u8");
        try {
            Files.delete(file.toPath());
            log.info("File m3u8 of live stream has been deleted");
        } catch (IOException e) {
            log.error("File m3u8 of live stream has not been deleted");
            e.printStackTrace();
        }


    }
}