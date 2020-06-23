package com.company.platform.web.screens.imageprocessings;

import com.company.platform.web.screens.imageprocessing.ImageProcessingBrowse;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.ImageProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.UUID;

@UiController("platform_ImageProcessings.browse")
@UiDescriptor("image-processings-browse.xml")
@LookupComponent("imageProcessingsTable")
@LoadDataBeforeShow
public class ImageProcessingsBrowse extends StandardLookup<ImageProcessing> {
    @Inject
    private DataLoader imageProcessingsDl;

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingsBrowse.class);

    @Inject
    private UiComponents components;

    @Inject
    private Screens screens;

    @Inject
    private GroupTable<ImageProcessing> imageProcessingsTable;

    @Subscribe
    private void onInit(InitEvent event){
        log.info("On init event has started");

        ScreenOptions options = event.getOptions();
        if(options instanceof MapScreenOptions){
            UUID nodeId = (UUID) ((MapScreenOptions) options).getParams().get("nodeId");
            imageProcessingsDl.setParameter("nodeId", nodeId);
        }

        imageProcessingsTable.addGeneratedColumn("openImageProcessing", this::renderImageProcessing);

        log.info("On init event has finished");
    }

    private Component renderImageProcessing(ImageProcessing imageProcessing){
        Button button = components.create(Button.NAME);
        button.setCaption("Open image processing");
        button.addClickListener(event -> {
            ImageProcessingBrowse imageProcessingScreen = screens.create(ImageProcessingBrowse.class, OpenMode.NEW_TAB, new MapScreenOptions(Collections.singletonMap("imageProcessingId", imageProcessing.getId())));
            imageProcessingScreen.show();
        });

        return button;
    }
}