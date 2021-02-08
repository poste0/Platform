package com.company.platform.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum NodeStatus implements EnumClass<String> {

    CONNECTED("CONNECTED"), NOT_CONNECTED("NOT_CONNECTED");

    private String id;

    NodeStatus(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static NodeStatus fromId(String id) {
        for (NodeStatus at : NodeStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}