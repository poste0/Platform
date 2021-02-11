package com.company.platform.core;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

import javax.validation.constraints.Positive;

public class FFMpegGrabberBuilder {
    private final FFmpegFrameGrabber grabber;

    public FFMpegGrabberBuilder(String address) throws FrameGrabber.Exception {
        this.grabber = FFmpegFrameGrabber.createDefault(address);
    }

    public FFMpegGrabberBuilder withOption(String key, String value){
        this.grabber.setOption(key, value);
        return this;
    }

    public FFMpegGrabberBuilder withHeight(@Positive Integer height){
        this.grabber.setImageHeight(height);
        return this;
    }

    public FFMpegGrabberBuilder withWidth(@Positive Integer width){
        this.grabber.setImageWidth(width);
        return this;
    }

    public FFmpegFrameGrabber build(){
        return this.grabber;
    }
}
