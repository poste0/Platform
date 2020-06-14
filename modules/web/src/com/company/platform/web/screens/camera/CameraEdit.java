package com.company.platform.web.screens.camera;

import com.company.platform.service.CameraService;
import com.company.platform.service.StreamService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Camera;
import com.haulmont.cuba.security.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

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

    @Inject
    private TextField<String> pathField;

    @Inject
    private TextArea<String> optionArea;

    private StringBuilder address = new StringBuilder();

    @Inject
    private Label<String> urlLabelValue;

    private Logger logger = LoggerFactory.getLogger(CameraEdit.class);

    private static final Consumer<String> FIELD_VALIDATOR = s -> {
        if(Objects.isNull(s)){
            throw new ValidationException("Enter all fields");
        }
    };

    private final Consumer<HasValue.ValueChangeEvent<String>> VALUEEVENT = valueEvent -> {
        generateUrl();
        this.urlLabelValue.setValue(this.address.toString());
    };

    private static final String PROTOCOL = "rtsp://";

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        logger.info("After show event start");
        addressField.addValidator(FIELD_VALIDATOR);
        nameField.addValueChangeListener(VALUEEVENT);
        passwordField.addValueChangeListener(VALUEEVENT);
        addressField.addValueChangeListener(VALUEEVENT);
        portField.addValueChangeListener(VALUEEVENT);
        pathField.addValueChangeListener(VALUEEVENT);
        optionArea.addValueChangeListener(VALUEEVENT);

        if(!Objects.isNull(getEditedEntity().getAddress())) {
            setFields();
        }
        logger.info("After show event end");
    }

    private void setFields(){
        logger.info("setFields start");
        final Camera camera = getEditedEntity();
        final String password = getPassword(camera);
        final String name = getName(camera);

        this.cameraNameField.setValue(camera.getName());
        this.addressField.setValue(camera.getUrlAddress());
        this.nameField.setValue(name);
        this.passwordField.setValue(password);
        this.portField.setValue(String.valueOf(camera.getPort()));
        this.frameRateField.setValue(camera.getFrameRate().toString());
        this.heightField.setValue(camera.getHeight().toString());
        this.widthField.setValue(camera.getWeight().toString());
        logger.info("setFields end");
    }

    private String getPassword(Camera camera){
        return camera.getAddress().substring(PROTOCOL.length()).split(":")[1].split("@")[0];
    }

    private String getName(Camera camera){
        return camera.getAddress().substring(PROTOCOL.length()).split(":")[0];
    }

    public void onOkButton(){
        logger.info("on ok button start");

        Camera camera = getEditedEntity();
        boolean isStreamStarted = camera.getAddress() != null;
        camera.setAddress(address.toString());
        camera.setPort(Integer.valueOf(portField.getRawValue()));
        camera.setUrlAddress(addressField.getRawValue());
        camera.setFrameRate(Integer.valueOf(frameRateField.getRawValue()));
        camera.setHeight(Integer.valueOf(heightField.getRawValue()));
        camera.setWeight(Integer.valueOf(widthField.getRawValue()));
        camera.setName(cameraNameField.getRawValue());

        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        camera.setUser(user);
        cameraService.update(user, camera);
        streamService.update(user, camera);

        //if(!isStreamStarted){
        //    streamService.startStream(camera);
        //}

        close(WINDOW_COMMIT_AND_CLOSE_ACTION);
        logger.info("on ok button end");
    }

    private void generateUrl(){
        address.delete(0, address.length());

        address.append(PROTOCOL);
        appendIfNotEmpty(nameField.getRawValue(), nameField.getRawValue() + ":");
        appendIfNotEmpty(passwordField.getValue(), passwordField.getValue() + "@");
        appendIfNotEmpty(addressField.getRawValue());
        if(!portField.getRawValue().isEmpty()){
            address.append(":")
                    .append(portField.getRawValue());
        }

        address.append("/")
                .append(pathField.getRawValue());

        if(!StringUtils.isEmpty(optionArea.getRawValue())) {
            address.append("?");
            Arrays.asList(optionArea.getRawValue().split("\n")).forEach(option -> {
                address.append(option)
                        .append("&");
            });
            address.deleteCharAt(address.length() - 1);
        }
    }

    private void appendIfNotEmpty(String value, String addedValue){
        if(!StringUtils.isEmpty(value)){
            this.address.append(addedValue);
        }
    }

    private void appendIfNotEmpty(String value){
        appendIfNotEmpty(value, value);
    }
}