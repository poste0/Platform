package com.company.platform.web.screens.camera;

import com.company.platform.service.CameraService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.PasswordField;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Camera;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Consumer;

@UiController("platform_Camera.edit")
@UiDescriptor("camera-edit.xml")
@EditedEntityContainer("cameraDc")
@LoadDataBeforeShow
public class CameraEdit extends StandardEditor<Camera> {

    @Inject
    private CameraService cameraService;

    @Inject
    private TextField<String> nameField;

    @Inject
    private PasswordField passwordField;

    @Inject
    private TextField<String> addressField;

    @Inject
    private TextField<Integer> portField;

    private static final Consumer<String> FIELD_VALIDATOR = s -> {
        if(Objects.isNull(s)){
            throw new ValidationException("Enter all fields");
        }
    };

    private static final String PROTOCOL = "rtsp://";

    @Subscribe
    public void onInit(InitEvent event){
        nameField.addValidator(FIELD_VALIDATOR);
        passwordField.addValidator(FIELD_VALIDATOR);

        addressField.addValidator(FIELD_VALIDATOR);
    }

    public void onOkButton(){

        StringBuilder address = new StringBuilder();
        address.append(PROTOCOL)
                .append(nameField.getRawValue())
                .append(":")
                .append(passwordField.getValue())
                .append("@")
                .append(addressField.getRawValue());

        if(!portField.getRawValue().isEmpty()){
            address.append(":")
                    .append(portField.getRawValue());
        }

        Camera camera = getEditedEntity();
        camera.setAddress(address.toString());
        camera.setStatus(Camera.Status.NOT_CONNECTED);
        cameraService.update(AppBeans.get(UserSessionSource.class).getUserSession().getUser(), camera);

        close(WINDOW_COMMIT_AND_CLOSE_ACTION);
    }
}