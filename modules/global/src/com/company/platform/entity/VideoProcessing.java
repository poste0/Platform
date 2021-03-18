package com.company.platform.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Table(name = "PLATFORM_VIDEO_PROCESSING")
@Entity(name = "platform_VideoProcessing")
@NamePattern("%s|id")
public class VideoProcessing extends StandardEntity {
    private static final long serialVersionUID = 5964882789394513453L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    private Node node;

    @OneToOne(fetch = FetchType.LAZY)
    private Video video;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "videoProcessing")
    private Video resultVideo;

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode(){
        return node;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Video getResultVideo() {
        return resultVideo;
    }

    public void setResultVideo(Video resultVideo) {
        this.resultVideo = resultVideo;
    }
}