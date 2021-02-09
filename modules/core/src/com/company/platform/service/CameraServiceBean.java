package com.company.platform.service;

import com.company.platform.core.CameraStatusBean;
import com.company.platform.core.Capture;
import com.company.platform.core.FFMpegCapture;
import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.app.UserSessions;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import org.bytedeco.javacv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;
import sun.net.util.IPAddressUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service(CameraService.NAME)
public class CameraServiceBean implements CameraService {
    private Map<User, List<Capture>> ffMpegs;

    private static final Logger log = LoggerFactory.getLogger(CameraServiceBean.class);

    public void init(){
        if(ffMpegs == null) {
            log.info("Capture map is null. Creating a map");
            ffMpegs = new HashMap<>();
        }

        List<UserSession> userSessions = getSessions();
        processSessions(userSessions);
    }

    private List<UserSession> getSessions(){
        UserSessions tempUserSessions = AppBeans.get(UserSessions.NAME);
        return tempUserSessions.getUserSessionsStream().collect(Collectors.toList());
    }

    private void processSessions(List<UserSession> userSessions){
        DataManager dataManager = AppBeans.get(DataManager.NAME);
        userSessions.stream()
                .filter(userSession -> !ffMpegs.containsKey(userSession.getUser()))
                .forEach(userSession -> {
                    List<Camera> cameras = dataManager.loadValue(
                            "SELECT c FROM platform_Camera c " +
                            "WHERE c.user.id = :user", Camera.class
                    ).setParameters(Collections.singletonMap("user", userSession.getUser().getId())).list();
                    List<Capture> wrappers = new ArrayList<>();
                    cameras.forEach(camera -> wrappers.add(AppBeans.getPrototype(FFMpegCapture.NAME, camera)));
                    ffMpegs.put(userSession.getUser(), wrappers);
                });
    }

    @Override
    public void write(Camera camera) throws IllegalStateException {
        if(!this.getStatus(camera).equals(Status.CONNECTED)){
            throw new IllegalStateException("Camera status is not connected.");
        }

        Capture capture = getWrapper(camera);
        try {
            capture.process();
        } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
            log.error("Error on recording for camera {}", camera.getId(), e);
        }
    }

    private Capture getWrapper(Camera camera){
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        List<Capture> wrappers = ffMpegs.get(session.getUser());
        Capture capture;
        for(Capture w: wrappers){
            if(w.getCamera().getId().equals(camera.getId())){
                capture = w;
                return capture;
            }
        }

        final String errorMessage = String.format("There is no wrapper for camera with id %s", camera.getId());
        log.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }

    public void stop(Camera camera) throws IllegalStateException {
        if(!this.getStatus(camera).equals(Status.RECORDING)){
            throw new IllegalStateException("Camera status is not recording");
        }

        Capture capture = getWrapper(camera);
        capture.stop();
    }

    public boolean isRecording(Camera camera){
        Capture capture = getWrapper(camera);
        return capture.isRecording();
    }

    public void update(Camera camera){
        UserSession userSession = AppBeans.get(UserSessionSource.class).getUserSession();
        List<Capture> wrappers = ffMpegs.get(userSession.getUser());
        wrappers.add(AppBeans.getPrototype(FFMpegCapture.NAME, camera));


        ffMpegs.put(userSession.getUser(), wrappers);
    }

    public boolean testConnection(Camera camera){
        try(Socket socket = new Socket()) {
            Pattern pattern = Pattern.compile("([0-9]{1,3}[\\.]){3}[0-9]{1,3}");
            Matcher matcher = pattern.matcher(camera.getAddress());
            matcher.find();
            final String address = matcher.group(0);

            int port = 5000;
            final String[] splitAddress = camera.getAddress().split("([0-9]{1,3}[\\.]){3}[0-9]{1,3}");
            if(splitAddress.length == 2){
                if(splitAddress[1].charAt(0) == ':'){
                    if(splitAddress[1].contains("/")){
                        port = Integer.valueOf(splitAddress[1].substring(1).split("/")[0]);
                    }
                    else {
                        port = Integer.valueOf(splitAddress[1].substring(1));
                    }
                }

            }

            log.info(address);
            log.info(String.valueOf(port));

            socket.connect(new InetSocketAddress(address, port), 500);
            boolean result = socket.isConnected();
            socket.close();
            log.info(String.valueOf(result));
            return result;
        } catch (IOException e) {
            log.error("Test is failed");
            return false;
        }

    }

    public Status getStatus(Camera camera){
        CameraStatusBean statusBean = AppBeans.get(CameraStatusBean.NAME);

        return statusBean.getCameraStatus(testConnection(camera), isRecording(camera));
    }

    @Override
    public List<Camera> getCameras() throws IllegalStateException {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        DataManager dataManager = AppBeans.get(DataManager.NAME);

        return dataManager.loadList(
                LoadContext.create(Camera.class).setQuery(
                        LoadContext.createQuery(
                                "SELECT c FROM platform_Camera c WHERE c.user.id = :id"
                        )
                        .setParameter("id", user.getId())
                )
        );
    }


}