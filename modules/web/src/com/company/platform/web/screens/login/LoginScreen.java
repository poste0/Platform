package com.company.platform.web.screens.login;


import com.company.platform.web.screens.registration.RegisterScreen;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

@UiController("login")
@UiDescriptor("login.xml")
public class LoginScreen extends com.haulmont.cuba.web.app.login.LoginScreen {

    public void onRegisterButtonClick(){
        Screen screen = screens.create(RegisterScreen.class, OpenMode.DIALOG);
        screen.show();
    }
}
