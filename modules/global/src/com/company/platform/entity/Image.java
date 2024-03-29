package com.company.platform.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Table(name = "PLATFORM_IMAGE")
@Entity(name = "platform_Image")
@NamePattern("%s|name")
public class Image extends StandardEntity {
    private static final long serialVersionUID = 718762005463730156L;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fileDescriptorId")
    private FileDescriptor fileDescriptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentVideoId")
    private Video parentVideo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imageProcessingId")
    private ImageProcessing imageProcessing;

    @Column(name = "message")
    private String message;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(FileDescriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public void setParentVideo(Video parentVideo) {
        this.parentVideo = parentVideo;
    }

    public Video getParentVideo(){
        return parentVideo;
    }

    public void setImageProcessing(ImageProcessing imageProcessing) {
        this.imageProcessing = imageProcessing;
    }

    public ImageProcessing getImageProcessing(){
        return imageProcessing;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}