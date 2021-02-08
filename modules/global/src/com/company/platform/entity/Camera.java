package com.company.platform.entity;

import com.company.platform.service.CameraService;
import com.esotericsoftware.kryo.NotNull;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.entity.annotation.PublishEntityChangedEvents;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DeletePolicy;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import java.util.List;

@PublishEntityChangedEvents
@Table(name = "PLATFORM_CAMERA")
@Entity(name = "platform_Camera")
public class Camera extends StandardEntity {
    private static final long serialVersionUID = -8832880328485561860L;
    @Column(name = "ADDRESS", nullable = false)
    @NotNull
    private String address;

    @Column(name = "URL_ADDRESS", nullable = false)
    @NotNull
    private String urlAddress;

    @Column(name = "PORT")
    private Integer port;


    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "height", nullable = false)
    @NotNull
    private Integer height;

    @Column(name = "weight", nullable = false)
    @NotNull
    private Integer weight;

    @Column(name = "frameRate", nullable = false)
    @NotNull
    private Integer frameRate;

    @Column(name = "path")
    private String path;

    @OneToMany(mappedBy = "camera")
    @OnDelete(DeletePolicy.CASCADE)
    private List<Video> videos;

    @Transient
    @MetaProperty
    private CameraService.Status status;

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser(){
        return AppBeans.get(UserSessionSource.class).getUserSession().getUser();
    }


    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress(){
        return this.address;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(Integer frameRate) {
        this.frameRate = frameRate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath(){
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    public String getUrlAddress() {
        return urlAddress;
    }

    public void setUrlAddress(String urlAddress) {
        this.urlAddress = urlAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public CameraService.Status getStatus() {
        return status;
    }

    public void setStatus(CameraService.Status status) {
        this.status = status;
    }
}