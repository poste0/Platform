package com.company.platform.web.screens.node;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Node;

@UiController("platform_Node.edit")
@UiDescriptor("node-edit.xml")
@EditedEntityContainer("nodeDc")
@LoadDataBeforeShow
public class NodeEdit extends StandardEditor<Node> {
    public void onOkButton(){
        Node node = getEditedEntity();
        node.setUser(AppBeans.get(UserSessionSource.class).getUserSession().getUser());

        close(WINDOW_COMMIT_AND_CLOSE_ACTION);
    }
}