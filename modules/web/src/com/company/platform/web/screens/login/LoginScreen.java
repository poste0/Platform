package com.company.platform.web.screens.login;


import com.company.platform.service.CameraService;
import com.company.platform.web.screens.registration.RegisterScreen;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.auth.LoginPasswordCredentials;
import com.haulmont.cuba.web.security.events.AfterLoginEvent;

import javax.inject.Inject;
import java.awt.*;
import java.util.function.Consumer;

@UiController("login")
@UiDescriptor("login.xml")
public class LoginScreen extends com.haulmont.cuba.web.app.login.LoginScreen {


    @Inject
    private CameraService service;
    public void onRegisterButtonClick(){
        TextField loginField = this.loginField;
        RegisterScreen screen = screens.create(RegisterScreen.class, OpenMode.DIALOG);
        screen.addAfterCloseListener(new Consumer<AfterCloseEvent>() {
            @Override
            public void accept(AfterCloseEvent afterCloseEvent) {
                if(afterCloseEvent.getCloseAction().equals(WINDOW_COMMIT_AND_CLOSE_ACTION)){
                    doLogin(new LoginPasswordCredentials(screen.getLogin(), screen.getPassword()));
                }
                else if(afterCloseEvent.getCloseAction().equals(WINDOW_CLOSE_ACTION)){
                    loginField.setValue("This user already exists");
                }
            }
        });
        screen.show();
    }

}
