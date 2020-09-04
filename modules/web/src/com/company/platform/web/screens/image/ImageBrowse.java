package com.company.platform.web.screens.image;

import com.company.platform.entity.Image;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.FileDescriptorResource;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;

import javax.inject.Inject;
import java.util.UUID;

@UiController("platform_Image.browse")
@UiDescriptor("image-browse.xml")
@LookupComponent("imagesTable")
@LoadDataBeforeShow
public class ImageBrowse extends StandardLookup<Image> {
    @Inject
    private DataLoader imagesDl;

    @Inject
    private UiComponents components;

    @Inject
    private Table<Image> imagesTable;

    @Subscribe
    private void onInit(InitEvent event){
        ScreenOptions options = event.getOptions();
        if(options instanceof MapScreenOptions){
            UUID imageProcessingId = (UUID) ((MapScreenOptions) options).getParams().get("imageProcessingId");
            imagesDl.setParameter("imageProcessingId", imageProcessingId);
        }

        imagesTable.addGeneratedColumn("Image", this::renderImage);

    }

    private Component renderImage(Image image){
        FileDescriptor imageFileDescriptor = image.getFileDescriptor();

        com.haulmont.cuba.gui.components.Image img = components.create(com.haulmont.cuba.gui.components.Image.NAME);
        img.setScaleMode(com.haulmont.cuba.gui.components.Image.ScaleMode.CONTAIN);
        img.setSource(FileDescriptorResource.class).setFileDescriptor(imageFileDescriptor);
        img.setWidth("500");
        img.setHeight("500");
        return img;
    }

}