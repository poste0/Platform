package com.company.platform.web.screens.registration;

import com.company.platform.service.RegistrationService;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.screen.CloseAction;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.security.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@UiController("register")
@UiDescriptor("register.xml")
public class RegisterScreen extends Screen {

    @Inject
    private RegistrationService service;

    @Inject
    private TextField loginTextField;

    @Inject
    private TextField passwordTextField;

    @Inject
    private Label errorLabel;

    @Inject
    private TextField nameTextField;

    private static final Logger log = LoggerFactory.getLogger(RegisterScreen.class);

    public void onOkButton(){
        try {
            if(loginTextField.isEmpty() || passwordTextField.isEmpty()){
                log.warn("Login or password has not been entered");
                errorLabel.setValue("Enter password and login");
                return;
            }

            User user = service.register(loginTextField.getRawValue(), passwordTextField.getRawValue());

            close(WINDOW_COMMIT_AND_CLOSE_ACTION);
        }
        catch (IllegalArgumentException e){
            log.error("Error on register of users");
            close(WINDOW_CLOSE_ACTION);
        }
    }

    public void onCancelButton(){
        close(WINDOW_DISCARD_AND_CLOSE_ACTION);
    }

    public String getLogin(){
        return loginTextField.getRawValue();
    }

    public String getPassword(){
        return passwordTextField.getRawValue();
    }
}
