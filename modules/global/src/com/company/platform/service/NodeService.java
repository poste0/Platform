package com.company.platform.service;

import com.company.platform.entity.Node;

import java.util.List;

public interface NodeService {
    String NAME = "platform_NodeService";

    String getCpu(Node node);

    String getGpu(Node node);

    String getStatus(Node node);

    List<Node> getNodes();
}