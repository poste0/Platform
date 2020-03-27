package com.company.platform.web.screens.camera;

import com.company.platform.service.CameraService;
import com.company.platform.service.StreamService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.PasswordField;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Camera;
import com.haulmont.cuba.security.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
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
    private StreamService streamService;

    @Inject
    private TextField<String> nameField;

    @Inject
    private PasswordField passwordField;

    @Inject
    private TextField<String> addressField;

    @Inject
    private TextField<String> portField;

    @Inject
    private TextField<String> frameRateField;

    @Inject
    private TextField<String> heightField;

    @Inject
    private TextField<String> widthField;

    @Inject
    private TextField<String> cameraNameField;

    private Logger logger = LoggerFactory.getLogger(CameraEdit.class);

    private static final Consumer<String> FIELD_VALIDATOR = s -> {
        if(Objects.isNull(s)){
            throw new ValidationException("Enter all fields");
        }
    };

    private static final String PROTOCOL = "rtsp://";

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        logger.info("After show event start");
        nameField.addValidator(FIELD_VALIDATOR);
        passwordField.addValidator(FIELD_VALIDATOR);
        addressField.addValidator(FIELD_VALIDATOR);
        //setFields();
        logger.info("After show event end");
    }

    private void setFields(){
        logger.info("setFields start");
        final Camera camera = getEditedEntity();
        final String address = getAddress(camera);
        final String name = getName(camera);
        final String password = getPassword(camera);
        final String port = getPort(camera);

        this.cameraNameField.setValue(camera.getName());
        this.addressField.setValue(address);
        this.nameField.setValue(name);
        this.passwordField.setValue(password);
        this.portField.setValue(port);
        this.frameRateField.setValue(camera.getFrameRate().toString());
        this.heightField.setValue(camera.getHeight().toString());
        this.widthField.setValue(camera.getWeight().toString());
        logger.info("setFields end");
    }

    private String getAddress(Camera camera){
        return camera.getAddress().split("@")[1];
    }

    private String getName(Camera camera){
        return camera.getAddress().substring(PROTOCOL.length()).split(":")[0];
    }

    private String getPassword(Camera camera){
        return camera.getAddress().substring(PROTOCOL.length()).split(":")[1].split("@")[0];
    }

    private String getPort(Camera camera){
        System.out.println(camera.getAddress());
        Arrays.stream(camera.getAddress().split(":")).forEach(System.out::println);
        return camera.getAddress().split(":").length == 4 ? camera.getAddress().split(":")[3] : "";
    }

    public void onOkButton(){
        logger.info("on ok button start");
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
        camera.setFrameRate(Integer.valueOf(frameRateField.getRawValue()));
        camera.setHeight(Integer.valueOf(heightField.getRawValue()));
        camera.setWeight(Integer.valueOf(widthField.getRawValue()));
        camera.setName(cameraNameField.getRawValue());

        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        cameraService.update(user, camera);
        streamService.update(user, camera);

        close(WINDOW_COMMIT_AND_CLOSE_ACTION);
        logger.info("on ok button end");
    }
}