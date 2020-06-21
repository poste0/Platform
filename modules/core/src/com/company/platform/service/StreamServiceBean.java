package com.company.platform.service;

import com.company.platform.core.Capture;
import com.company.platform.core.FFMpegCaptureStream;
import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.app.UserSessions;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.global.UserSession;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service(StreamService.NAME)
public class StreamServiceBean implements StreamService {
    private Map<User, List<Capture>> ffMpegs;

    private static final Logger log = LoggerFactory.getLogger(StreamServiceBean.class);

    public void init(){
        if(ffMpegs == null) {
            log.info("Capture map is null");
            ffMpegs = new HashMap<>();
        }

        List<UserSession> userSessions = getSessions();
        processSessions(userSessions);

        log.info(ffMpegs.toString());
    }

    @Override
    public void update(User user, Camera camera) {
        UserSession userSession = AppBeans.get(UserSessionSource.class).getUserSession();
        List<Capture> wrappers = ffMpegs.get(userSession.getUser());
        wrappers.add(AppBeans.getPrototype(FFMpegCaptureStream.NAME, camera));


        ffMpegs.put(userSession.getUser(), wrappers);

        log.info("Capture map updating");
    }

    private List<UserSession> getSessions(){
        UserSessions tempUserSessions = AppBeans.get(UserSessions.NAME);
        List<UserSession> userSessions = tempUserSessions.getUserSessionsStream().collect(Collectors.toList());
        return userSessions;
    }

    private void processSessions(List<UserSession> userSessions){
        DataManager dataManager = AppBeans.get(DataManager.NAME);
        userSessions.stream()
                .filter(userSession -> !ffMpegs.containsKey(userSession.getUser()))
                .forEach(userSession -> {
                    List<Camera> cameras = dataManager.loadValue("SELECT c FROM platform_Camera c " +
                            "WHERE c.user.id = :user", Camera.class).setParameters(Collections.singletonMap("user", userSession.getUser().getId())).list();
                    List<Capture> wrappers = new ArrayList<>();
                    cameras.forEach(camera -> {
                        wrappers.add(AppBeans.getPrototype(FFMpegCaptureStream.NAME, camera));
                    });
                    ffMpegs.put(userSession.getUser(), wrappers);
                });
    }

    private Capture getCapture(Camera camera){
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
        List<Capture> wrappers = ffMpegs.get(session.getUser());
        Capture capture = null;
        for(Capture w: wrappers){
            if(w.getCamera().getId().equals(camera.getId())){
                capture = w;
            }
        }
        return capture;
    }


    @Override
    public void startStream(Camera camera) {
        Capture capture = getCapture(camera);
        try {
            capture.process();
            log.info("Stream has started");
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            log.error("Stream has not been started");
            e.printStackTrace();
        }
    }

    @Override
    public void stopStream(Camera camera) {
        Capture capture = getCapture(camera);
        capture.stop();
        log.info("Stream has started");
    }
}