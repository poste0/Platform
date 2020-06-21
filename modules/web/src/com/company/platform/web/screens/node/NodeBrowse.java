package com.company.platform.web.screens.node;

import com.company.platform.service.NodeService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Node;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Inject
    private UiComponents components;

    private static final Logger log = LoggerFactory.getLogger(NodeBrowse.class);

    private final Table.ColumnGenerator HARDWARE = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)){
                log.error("Node is null. Hardware generator");
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

    private final Table.ColumnGenerator STATUS = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)){
                log.error("Node is null. Status generator");
                throw new IllegalArgumentException();
            }

            Node node = (Node) entity;
            String status = nodeService.getStatus(node);

            Label<String> label = components.create(Label.class);
            label.setValue(status);

            return label;
        }
    };

    @Subscribe
    public void onInit(InitEvent event){
        log.info("On init event has started");

        nodesTable.addGeneratedColumn("hardwareButton", HARDWARE);
        nodesDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());

        log.info("On init event has finished");
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        log.info("On after show event has started");

        nodesTable.addGeneratedColumn("statusLabel", STATUS);

        log.info("On after show event has finished");
    }
}