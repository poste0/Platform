package com.company.platform.service;

import com.company.platform.entity.Node;
import com.company.platform.entity.NodeStatus;

import java.util.List;
import java.util.UUID;

public interface NodeService {
    String NAME = "platform_NodeService";

    String getCpu(Node node);

    String getGpu(Node node);

    NodeStatus getStatus(Node node);

    List<Node> getNodes();

    List<Node> getConnectedNodes();

    void processVideo(UUID nodeId, UUID videoId, String login, String password);
}