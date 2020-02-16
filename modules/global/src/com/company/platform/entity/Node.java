package com.company.platform.entity;

import com.esotericsoftware.kryo.NotNull;
import com.haulmont.cuba.core.entity.StandardEntity;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.util.Objects;

@Table(name = "PLATFORM_NODE")
@Entity(name = "platform_Node")
public class Node extends StandardEntity {
    private static final long serialVersionUID = -7259575376254531300L;

    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @Column(name = "ADDRESS", nullable = false)
    @NotNull
    private String address;

    @Column(name = "GPU", nullable = false)
    @NotNull
    private String gpu;

    @Column(name = "CPU", nullable = false)
    @NotNull
    private String cpu;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGpu() {
        return gpu;
    }

    public void setGpu(String gpu) {
        this.gpu = gpu;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    @PrePersist
    private void prePersist(){
        if(Objects.isNull(this.cpu)){
            this.cpu = "";
        }

        if(Objects.isNull(this.gpu)){
            this.gpu = "";
        }
    }
}