package com.company.platform.entity;

import com.esotericsoftware.kryo.NotNull;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;
import java.util.Date;

@Table(name = "PLATFORM_VIDEO_PROCESSING")
@Entity(name = "platform_VideoProcessing")
public class VideoProcessing extends StandardEntity {
    private static final long serialVersionUID = 5964882789394513453L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    private Node node;

    @OneToOne
    @JoinColumn(name = "videoId")
    private Video video;

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
}