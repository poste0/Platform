package com.company.platform.entity;

import com.esotericsoftware.kryo.NotNull;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Table(name = "PLATFORM_VIDEO")
@Entity(name = "platform_Video")
public class Video extends StandardEntity {
    private static final long serialVersionUID = 5007256048789074873L;

    @Column(name = "name", nullable = false)
    @NotNull
    private String name;

    @OneToOne
    @JoinColumn(name = "fileDescriptorId")
    private FileDescriptor fileDescriptor;

    @ManyToOne
    @JoinColumn(name = "cameraId")
    private Camera camera;

    @Column(name = "parentName")
    private String parentName;

    @Column(name = "status", nullable = false)
    @NotNull
    private String status;

    @ManyToOne
    @JoinColumn(name = "parentVideo")
    private Video parentVideo;

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

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Video getParentVideo() {
        return parentVideo;
    }

    public void setParentVideo(Video parentVideo) {
        this.parentVideo = parentVideo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if(!status.equals(Status.READY.getId()) && !status.equals(Status.PROCESSING.getId()) && !status.equals(Status.PROCESSED.getId())){
            throw new IllegalArgumentException("Status must be one of the status enum values");
        }

        this.status = status;
    }
}