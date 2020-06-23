package com.company.platform.web.screens.videoprocessing;

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.VideoProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

@UiController("platform_Processing.browse")
@UiDescriptor("video-processing-browse.xml")
@LookupComponent("videoProcessingsTable")
@LoadDataBeforeShow
public class VideoProcessingBrowse extends StandardLookup<VideoProcessing> {
    @Inject
    private DataLoader videoProcessingsDl;

    private static final Logger log = LoggerFactory.getLogger(VideoProcessingBrowse.class);

    @Subscribe
    private void onInit(InitEvent event){
        log.info("On init event has started");

        ScreenOptions options = event.getOptions();
        if(options instanceof MapScreenOptions){
            UUID nodeId = (UUID) ((MapScreenOptions) options).getParams().get("nodeId");
            videoProcessingsDl.setParameter("nodeId", nodeId);
        }

        log.info("On init event has finished");
    }
}