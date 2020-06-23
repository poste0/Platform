package com.company.platform.web.screens.imageprocessing;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.ImageProcessing;
import com.haulmont.cuba.gui.screen.LookupComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@UiController("platform_ImageProcessing.browse")
@UiDescriptor("image-processing-browse.xml")
@LookupComponent("imageProcessingsTable")
@LoadDataBeforeShow
public class ImageProcessingBrowse extends StandardLookup<ImageProcessing> {
    @Inject
    private UiComponents components;

    @Inject
    private GroupTable<ImageProcessing> imageProcessingsTable;

    @Inject
    private DataLoader imageProcessingsDl;

    @Inject
    private Label<String> createTs;

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingBrowse.class);

    private Component renderImage(ImageProcessing imageProcessing){
        VBoxLayout layout = components.create(VBoxLayout.NAME);

        imageProcessing.getImages().forEach(image -> {
            FileDescriptor imageFileDescriptor = image.getFileDescriptor();

            Image img = components.create(Image.NAME);
            img.setScaleMode(Image.ScaleMode.CONTAIN);
            img.setSource(FileDescriptorResource.class).setFileDescriptor(imageFileDescriptor);

            layout.add(img);
        });

        return layout;
    }

    @Subscribe
    private void onInit(InitEvent event){
        log.info("On init event has started");

        imageProcessingsTable.addGeneratedColumn("image", this::renderImage);
        ScreenOptions options = event.getOptions();
        if(options instanceof MapScreenOptions){
            UUID imageProcessingId = (UUID) ((MapScreenOptions) options).getParams().get("imageProcessingId");
            imageProcessingsDl.setParameter("imageProcessingId", imageProcessingId);
        }

        log.info("On init event has finished");
    }

    @Subscribe
    private void onBeforeShow(AfterShowEvent event){
        log.info("On after show event has started");

        if(imageProcessingsTable.getItems().getItems().size() != 1){
            log.error("Image processings size {}", imageProcessingsTable.getItems().getItems().size());
            throw new IllegalStateException("1 item must be on the page");
        }

        String createTime = String.format("Created at %s", imageProcessingsTable.getItems().getItems().stream().findFirst().get().getCreateTs().toString());
        createTs.setValue(createTime);

        log.info("On after show event has finished");
    }
}