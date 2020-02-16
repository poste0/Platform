package com.company.platform.service;

import com.company.platform.entity.Node;

public interface NodeService {
    String NAME = "platform_NodeService";

    String getCpu(Node node);

    String getGpu(Node node);
}