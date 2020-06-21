package com.company.platform.web.screens;

import com.haulmont.cuba.gui.components.PasswordField;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@UiController("platform_ConfirmScreen")
@UiDescriptor("confirm-screen.xml")
public class ConfirmScreen extends Screen {
    @Inject
    private TextField<String> loginTextField;

    @Inject
    private PasswordField passwordTextField;


    private ScreenOptions screenOptions;

    private static final Logger log = LoggerFactory.getLogger(ConfirmScreen.class);

    @Subscribe
    private void onInit(InitEvent event){
        log.info("On init event has started");

        this.screenOptions = event.getOptions();

        log.info("On inti event has finished");
    }

    public void onConfirmButtonClicked() {
        if(screenOptions instanceof MapScreenOptions) {
            ((MapScreenOptions) screenOptions).getParams().put("login", loginTextField.getRawValue());
            ((MapScreenOptions) screenOptions).getParams().put("password", passwordTextField.getValue());
        }

        this.close(WINDOW_COMMIT_AND_CLOSE_ACTION);
    }
}