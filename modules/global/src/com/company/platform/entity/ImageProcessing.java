package com.company.platform.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;
import java.util.List;

@Table(name = "PLATFORM_IMAGE_PROCESSING")
@Entity(name = "platform_ImageProcessing")
@NamePattern("%s|id")
public class ImageProcessing extends StandardEntity {
    private static final long serialVersionUID = 362899307054057812L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    private Node node;

    @OneToMany(mappedBy = "imageProcessing")
    private List<Image> images;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoId")
    private Video video;


    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode(){
        return node;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }
}