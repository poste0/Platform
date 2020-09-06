package com.company.platform.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "PLATFORM_IMAGE_PROCESSING")
@Entity(name = "platform_ImageProcessing")
public class ImageProcessing extends StandardEntity {
    private static final long serialVersionUID = 362899307054057812L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    private Node node;

    @OneToMany(mappedBy = "imageProcessing")
    private List<Image> images;


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
}