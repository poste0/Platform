package com.company.platform.entity;

import com.esotericsoftware.kryo.NotNull;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;

@Table(name = "PLATFORM_CAMERA")
@Entity(name = "platform_Camera")
public class Camera extends StandardEntity {
    private static final long serialVersionUID = -8832880328485561860L;
    @Column(name = "ADDRESS", nullable = false)
    @NotNull
    private String address;

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
}