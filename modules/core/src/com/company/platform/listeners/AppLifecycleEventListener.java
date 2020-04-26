package com.company.platform.listeners;

import com.company.platform.entity.Camera;
import com.company.platform.service.StreamService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.cuba.security.entity.User;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component("platform_AppLifecycleEventListener_ApplicationContextListener")
public class AppLifecycleEventListener {
    @Inject
    private DataManager dataManager;

    @EventListener
    @Authenticated
    public void applicationContextStarted(AppContextStartedEvent event) {
        StreamService streamService = AppBeans.get(StreamService.NAME);
        List<User> users = dataManager.loadList(LoadContext.create(User.class).setQuery(LoadContext.createQuery("SELECT u FROM sec$User u")));

        streamService.init();

        users.forEach(user -> {
            List<Camera> cameras = dataManager.loadList(LoadContext.create(Camera.class).setQuery(LoadContext.createQuery("SELECT c FROM platform_Camera c WHERE c.user.id = :user").setParameter("user", user.getId())));
            cameras.forEach(camera -> {
                streamService.update(user, camera);
                streamService.startStream(camera);
            });
        });

    }
}