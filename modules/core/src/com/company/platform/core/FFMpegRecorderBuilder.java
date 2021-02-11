package com.company.platform.core;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import javax.validation.constraints.Positive;
import java.io.File;

public class FFMpegRecorderBuilder {
    private final FFmpegFrameRecorder recorder;

    private final FFmpegFrameGrabber grabber;

    public FFMpegRecorderBuilder(File file, FFmpegFrameGrabber grabber){
        this.grabber = grabber;
        this.recorder = new HlsRecorder(file, grabber.getImageHeight(), grabber.getImageWidth());
    }

    public FFMpegRecorderBuilder withCodec(){
        this.recorder.setVideoCodec(this.grabber.getVideoCodec());
        return this;
    }

    public FFMpegRecorderBuilder withBitrate(){
        this.recorder.setVideoBitrate(this.grabber.getVideoBitrate());
        return this;
    }

    public FFMpegRecorderBuilder withFrameRate(){
        this.recorder.setFrameRate(this.grabber.getFrameRate());
        return this;
    }

    public FFMpegRecorderBuilder withGopSize(){
        this.recorder.setFrameRate(this.grabber.getFrameRate());
        return this;
    }

    public FFMpegRecorderBuilder withAudioCodec(){
        this.recorder.setAudioCodec(this.grabber.getAudioCodec());
        return this;
    }

    public FFMpegRecorderBuilder withAudioChannels(){
        this.recorder.setAudioChannels(this.grabber.getAudioChannels());
        return this;
    }

    public FFMpegRecorderBuilder withWidth(@Positive Integer width){
        this.recorder.setImageWidth(width);
        return this;
    }

    public FFMpegRecorderBuilder withHeight(@Positive Integer height){
        this.recorder.setImageHeight(height);
        return this;
    }

    public FFMpegRecorderBuilder with(String key, String value){
        this.recorder.setOption(key, value);
        return this;
    }

    public FFmpegFrameRecorder build(){
        return this.recorder;
    }


}
