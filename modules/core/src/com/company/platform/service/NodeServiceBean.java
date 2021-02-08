package com.company.platform.service;

import com.company.platform.entity.Node;
import com.company.platform.entity.NodeStatus;
import com.company.platform.entity.Video;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.entity.User;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service(NodeService.NAME)
public class NodeServiceBean implements NodeService {
    private static final Logger log = LoggerFactory.getLogger(NodeServiceBean.class);

    @Inject
    private DataManager dataManager;

    @Inject
    private FileLoader loader;

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
        Map<String, Object> confirmScreenOptionsMap = new HashMap<>();
        confirmScreenOptionsMap.put("user", user);
        confirmScreenOptionsMap.put("login", login);
        confirmScreenOptionsMap.put("password", password);

        AtomicBoolean isConfirmed = new AtomicBoolean(false);

        PasswordEncryption encryption = AppBeans.get(PasswordEncryption.NAME);

        if (encryption.checkPassword(user, password) && login.equals(user.getLogin())) {
            log.info("User is confirmed");
            isConfirmed.set(true);
        } else {
            throw new IllegalArgumentException("User is not confirmed");
        }

        Video video = dataManager.load(Id.of(videoId, Video.class)).view("video-view").one();

        if (isConfirmed.get()) {
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            File file = new File(video.getFileDescriptor().getName());
            try {
                FileUtils.copyInputStreamToFile(loader.openStream(video.getFileDescriptor()), file);
                log.info("File {} has been copied", video.getFileDescriptor().getName());
            } catch (IOException | FileStorageException e) {
                log.error("File {} has not been copied", video.getFileDescriptor().getName());
                e.printStackTrace();
            }
            FileSystemResource value = new FileSystemResource(file);
            map.add("file", value);
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setBufferRequestBody(false);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate(factory);
            Node node = dataManager.load(Id.of(nodeId, Node.class)).one();
            restTemplate.exchange(node.getAddress() + "/file?login=" + user.getLogin() + "&password=" + password + "&cameraId=" + video.getCamera().getId().toString() + "&videoId=" + video.getId() + "&nodeId=" + node.getId(), HttpMethod.POST, requestEntity, String.class);

            video.setStatus("processing");
            dataManager.commit(video);
    }

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
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                RestTemplate template = new RestTemplate(factory);
                HttpHeaders headers = new HttpHeaders();
                LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                HttpEntity entity = new HttpEntity(map, headers);
                result = template.exchange(node.getAddress() + "/" + path, HttpMethod.GET, entity, String.class).getBody();
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