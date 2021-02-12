package com.company.platform.service;

import com.company.platform.core.RestBean;
import com.company.platform.entity.Node;
import com.company.platform.entity.NodeStatus;
import com.company.platform.entity.Video;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service(NodeService.NAME)
public class NodeServiceBean implements NodeService {
    private static final Logger log = LoggerFactory.getLogger(NodeServiceBean.class);

    private final DataManager dataManager;

    private final FileLoader loader;

    public NodeServiceBean(DataManager dataManager, FileLoader loader) {
        this.dataManager = dataManager;
        this.loader = loader;
    }

    @Override
    public String getCpu(Node node) {
        return getFromNode(node, "cpu");
    }

    @Override
    public String getGpu(Node node) {
        return getFromNode(node, "gpu");
    }

    @Override
    public NodeStatus getStatus(Node node) {
        return getFromNode(node, "status").equals("No node") ? NodeStatus.NOT_CONNECTED : NodeStatus.CONNECTED;
    }

    @Override
    public List<Node> getNodes() {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();

        return dataManager.loadList(
                LoadContext.create(Node.class)
                        .setQuery(LoadContext.createQuery(
                                "SELECT n FROM platform_Node n WHERE n.user.id = :id"
                        ).setParameter("id", user.getId()))
        );
    }

    @Override
    public List<Node> getConnectedNodes() {
        return getNodes().stream().filter(node -> getStatus(node).equals(NodeStatus.CONNECTED)).collect(Collectors.toList());
    }

    @Override
    public void processVideo(UUID nodeId, UUID videoId, String login, String password) {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        boolean isConfirmed = isUserConfirmed(user, login, password);

        if (isConfirmed) {
            Video video = dataManager.load(Id.of(videoId, Video.class)).view("video-view").one();
            FileSystemResource value = getFile(video);
            Node node = dataManager.load(Id.of(nodeId, Node.class)).one();

            RestBean restBean = AppBeans.get(RestBean.NAME);
            restBean.setMethod(HttpMethod.POST);
            restBean.setContentType(MediaType.MULTIPART_FORM_DATA);
            restBean.addToBody("file", value);
            String url = node.getAddress() + "/file?login=" + user.getLogin() + "&password=" + password + "&cameraId=" + video.getCamera().getId().toString() + "&videoId=" + video.getId() + "&nodeId=" + node.getId();
            restBean.setUrl(url);
            ResponseEntity<String> response = restBean.process();

            if (response.getStatusCode() == HttpStatus.OK) {
                video.setStatus("processing");
                dataManager.commit(video);
            }
        }

    }

    private boolean isUserConfirmed(User user, String login, String password){
        PasswordEncryption encryption = AppBeans.get(PasswordEncryption.NAME);

        if (encryption.checkPassword(user, password) && login.equals(user.getLogin())) {
            log.info("User is confirmed");
            return true;
        } else {
            return false;
        }
    }

    private FileSystemResource getFile(Video video){
        File file = new File(video.getFileDescriptor().getName());
        try {
            FileUtils.copyInputStreamToFile(loader.openStream(video.getFileDescriptor()), file);
            log.info("File {} has been copied", video.getFileDescriptor().getName());
        } catch (IOException | FileStorageException e) {
            log.error("File {} has not been copied", video.getFileDescriptor().getName());
        }
        return new FileSystemResource(file);
    }

    private boolean isConnected(Node node) {
        try {
            final String[] address = node.getAddress().split(":");
            final int port = address.length == 3 ? Integer.parseInt(address[2]) : 5000;
            Socket socket = new Socket(address[1].substring(2), port);
            boolean result = socket.isConnected();
            socket.close();
            log.info(String.valueOf(result));
            return result;
        } catch (IOException e) {
            log.error("Node is not connected");
            return false;
        }
    }

    private String getFromNode(Node node, String path) {
        String result = "";
        if (isConnected(node)) {
            try {
                RestBean restBean = AppBeans.get(RestBean.NAME);
                restBean.setUrl(node.getAddress() + "/" + path);
                restBean.setMethod(HttpMethod.GET);
                ResponseEntity<String> response = restBean.process();
                if(response.getStatusCode() == HttpStatus.OK){
                    result = response.getBody();
                }
            } catch (RestClientException e) {
                log.error("Rest error");
                result = "No node";
            }
        } else {
            log.error("No connection");
            result = "No node";
        }
        return result;
    }
}