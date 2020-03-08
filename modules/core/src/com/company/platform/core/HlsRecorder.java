package com.company.platform.core;

import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.Buffer;

import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_NONE;

public class HlsRecorder extends FFmpegFrameRecorder {
    public HlsRecorder(File file, int audioChannels) {
        super(file, audioChannels);
    }

    public HlsRecorder(String filename, int audioChannels) {
        super(filename, audioChannels);
    }

    public HlsRecorder(File file, int imageWidth, int imageHeight) {
        super(file, imageWidth, imageHeight);
    }

    public HlsRecorder(String filename, int imageWidth, int imageHeight) {
        super(filename, imageWidth, imageHeight);
    }

    public HlsRecorder(File file, int imageWidth, int imageHeight, int audioChannels) {
        super(file, imageWidth, imageHeight, audioChannels);
    }

    public HlsRecorder(String filename, int imageWidth, int imageHeight, int audioChannels) {
        super(filename, imageWidth, imageHeight, audioChannels);
    }

    public HlsRecorder(OutputStream outputStream, int audioChannels) {
        super(outputStream, audioChannels);
    }

    public HlsRecorder(OutputStream outputStream, int imageWidth, int imageHeight) {
        super(outputStream, imageWidth, imageHeight);
    }

    public HlsRecorder(OutputStream outputStream, int imageWidth, int imageHeight, int audioChannels) {
        super(outputStream, imageWidth, imageHeight, audioChannels);
    }

    @Override
    public void flush(){
        try{
            Class recorderClass = getClass().getSuperclass();
            Field oc_Field = recorderClass.getDeclaredField("oc");
            oc_Field.setAccessible(true);
            AVFormatContext oc = (AVFormatContext) oc_Field.get(this);

            Field video_st_Field = recorderClass.getDeclaredField("video_st");
            video_st_Field.setAccessible(true);
            AVStream video_st = (AVStream) video_st_Field.get(this);

            Field audio_st_Field = recorderClass.getDeclaredField("audio_st");
            audio_st_Field.setAccessible(true);
            AVStream audio_st = (AVStream) audio_st_Field.get(this);

            Field ifmt_ctx_Field = recorderClass.getDeclaredField("ifmt_ctx");
            ifmt_ctx_Field.setAccessible(true);
            AVFormatContext ifmt_ctx = (AVFormatContext) ifmt_ctx_Field.get(this);

            synchronized (oc) {
                /* flush all the buffers */
                while (video_st != null && ifmt_ctx == null && recordImage(0, 0, 0, 0, 0, AV_PIX_FMT_NONE, (Buffer[])null));
                while (audio_st != null && ifmt_ctx == null && recordSamples(0, 0, (Buffer[])null));

                if (interleaved && (video_st != null || audio_st != null)) {
                    av_interleaved_write_frame(oc, null);
                } else {
                    av_write_frame(oc, null);
                }

                /* write the trailer, if any */
                av_write_trailer(oc);
            }

        }
        catch (java.lang.Exception e){
            e.printStackTrace();
        }


    }
}
