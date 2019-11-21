package com.company.platform.web.screens.login;


import com.company.platform.web.screens.registration.RegisterScreen;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.security.auth.LoginPasswordCredentials;

import java.awt.*;
import java.util.function.Consumer;

@UiController("login")
@UiDescriptor("login.xml")
public class LoginScreen extends com.haulmont.cuba.web.app.login.LoginScreen {

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
