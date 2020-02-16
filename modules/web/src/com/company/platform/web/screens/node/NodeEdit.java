package com.company.platform.web.screens.node;

import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Node;

@UiController("platform_Node.edit")
@UiDescriptor("node-edit.xml")
@EditedEntityContainer("nodeDc")
@LoadDataBeforeShow
public class NodeEdit extends StandardEditor<Node> {
}