package com.company.platform.web.screens;

import com.haulmont.cuba.gui.components.PasswordField;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.*;

import javax.inject.Inject;

@UiController("platform_ConfirmScreen")
@UiDescriptor("confirm-screen.xml")
public class ConfirmScreen extends Screen {
    @Inject
    private TextField<String> loginTextField;

    @Inject
    private PasswordField passwordTextField;


    private ScreenOptions screenOptions;

    @Subscribe
    private void onInit(InitEvent event){
        this.screenOptions = event.getOptions();
    }

    public void onConfirmButtonClicked() {
        if(screenOptions instanceof MapScreenOptions) {
            ((MapScreenOptions) screenOptions).getParams().put("login", loginTextField.getRawValue());
            ((MapScreenOptions) screenOptions).getParams().put("password", passwordTextField.getValue());
        }

        this.close(WINDOW_COMMIT_AND_CLOSE_ACTION);
    }
}