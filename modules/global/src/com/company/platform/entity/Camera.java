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

    @Column(name = "STATUS")
    private Status status;

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status{
        NOT_CONNECTED, CONNECTED, RECORDING;
    }

    public Status getStatus() {
        return status;
    }


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
}