package com.company.platform.web.screens.login;


import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.company.platform.service.StreamService;
import com.company.platform.web.screens.registration.RegisterScreen;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.auth.LoginPasswordCredentials;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.security.events.AfterLoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

@UiController("login")
@UiDescriptor("login.xml")
public class LoginScreen extends com.haulmont.cuba.web.app.login.LoginScreen {


    @Inject
    private CameraService service;

    private static final Logger log = LoggerFactory.getLogger(LoginScreen.class);

    public void onRegisterButtonClick(){
        log.info("Register button has been clicked");

        TextField loginField = this.loginField;
        RegisterScreen screen = screens.create(RegisterScreen.class, OpenMode.DIALOG);
        screen.addAfterCloseListener(new Consumer<AfterCloseEvent>() {
            @Override
            public void accept(AfterCloseEvent afterCloseEvent) {
                if(afterCloseEvent.getCloseAction().equals(WINDOW_COMMIT_AND_CLOSE_ACTION)){
                    log.info("Login has started");
                    doLogin(new LoginPasswordCredentials(screen.getLogin(), screen.getPassword()));
                }
                else if(afterCloseEvent.getCloseAction().equals(WINDOW_CLOSE_ACTION)){
                    log.warn("THis user already exists");
                    loginField.setValue("This user already exists");
                }
            }
        });
        screen.show();
    }

    public void logins(){
        super.login();
        StreamService streamService = AppBeans.get(StreamService.NAME);
        streamService.init();
        DataManager dataManager = AppBeans.get(DataManager.NAME);
        User user = app.getConnection().getSession().getUser();

        List<Camera> cameras = dataManager.loadList(LoadContext.create(Camera.class).setQuery(LoadContext.createQuery("SELECT c FROM platform_Camera c WHERE c.user.id = :user").setParameter("user", user.getId())));

        cameras.forEach(camera -> {
            streamService.startStream(camera);
        });
    }

    @Override
    protected void initDefaultCredentials(){

    }

}
