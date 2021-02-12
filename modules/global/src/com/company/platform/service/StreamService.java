package com.company.platform.service;

import com.company.platform.entity.Camera;

public interface StreamService {
    String NAME = "platform_StreamService";

    void startStream(Camera camera);

    void stopStream(Camera camera);

    void init();

    void update(Camera camera);
}