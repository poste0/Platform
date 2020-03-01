package com.company.platform.service;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;

public interface StreamService {
    String NAME = "platform_StreamService";

    void startStream(Camera camera);

    void stopStream(Camera camera);

    void init();
}