package com.company.platform.service;

import com.company.platform.entity.Node;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

@Service(NodeService.NAME)
public class NodeServiceBean implements NodeService {
    private static final Logger log = LoggerFactory.getLogger(NodeServiceBean.class);

    @Override
    public String getCpu(Node node) {
        return getFromNode(node, "cpu");
    }

    @Override
    public String getGpu(Node node) {
        return getFromNode(node, "gpu");
    }

    @Override
    public String getStatus(Node node) {
        return getFromNode(node, "status");
    }

    @Override
    public List<Node> getNodes() {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        DataManager dataManager = AppBeans.get(DataManager.NAME);

        return dataManager.loadList(
                LoadContext.create(Node.class)
                .setQuery(LoadContext.createQuery(
                        "SELECT n FROM platform_Node n WHERE n.user.id = :id"
                ).setParameter("id", user.getId()))
        );
    }

    private boolean isConnected(Node node){
        try {
            final String[] address = node.getAddress().split(":");
            final int port = address.length == 3 ? Integer.parseInt(address[2]) : 5000;
            Socket socket = new Socket(address[1].substring(2), port);
            boolean result = socket.isConnected();
            socket.close();
            log.info(String.valueOf(result));
            return result;
        }
        catch (IOException e){
            log.error("Node is not connected");
            return false;
        }
    }

    private String getFromNode(Node node, String path){
        String result = "";
        if(isConnected(node)) {
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
        }
        else {
            log.error("No connection");
            result = "No node";
        }
        return result;
    }
}