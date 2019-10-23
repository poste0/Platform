package com.company.platform.web.screens.camera;

import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Camera;

@UiController("platform_Camera.edit")
@UiDescriptor("camera-edit.xml")
@EditedEntityContainer("cameraDc")
@LoadDataBeforeShow
public class CameraEdit extends StandardEditor<Camera> {
}