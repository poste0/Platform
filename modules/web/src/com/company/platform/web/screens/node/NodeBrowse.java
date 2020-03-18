package com.company.platform.web.screens.node;

import com.company.platform.service.NodeService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Node;
import com.haulmont.cuba.web.gui.components.WebButton;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Consumer;

@UiController("platform_Node.browse")
@UiDescriptor("node-browse.xml")
@LookupComponent("nodesTable")
@LoadDataBeforeShow
public class NodeBrowse extends StandardLookup<Node> {
    @Inject
    private NodeService nodeService;

    @Inject
    private GroupTable nodesTable;

    @Inject
    private DataLoader nodesDl;

    private final Table.ColumnGenerator HARDWARE = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)){
                throw new IllegalArgumentException();
            }

            Button button = new WebButton();
            button.setCaption("hardware");
            button.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    Node node = (Node) entity;

                    String cpu = nodeService.getCpu(node);
                    String gpu = nodeService.getGpu(node);

                    node.setCpu(cpu);
                    node.setGpu(gpu);

                    DataManager dataManager = AppBeans.get(DataManager.class);
                    dataManager.commit(node);
                }
            });

            return button;
        }
    };

    @Subscribe
    public void onInit(InitEvent event){
        nodesTable.addGeneratedColumn("hardwareButton", HARDWARE);
        nodesDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
    }
}