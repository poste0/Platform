package com.company.platform.web.screens.videoprocessing;

import com.company.platform.web.screens.EmptyScreen;
import com.company.platform.web.screens.videoplayer.VideoPlayerUtils;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileLoader;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.BoxLayout;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.VideoProcessing;
import com.haulmont.cuba.web.gui.components.WebHBoxLayout;
import com.vaadin.ui.Layout;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

@UiController("platform_Processing.browse")
@UiDescriptor("video-processing-browse.xml")
@LookupComponent("videoProcessingsTable")
@LoadDataBeforeShow
public class VideoProcessingBrowse extends StandardLookup<VideoProcessing> {
    /**
     * Loader for video processing objects
     */
    @Inject
    private DataLoader videoProcessingsDl;

    /**
     * Group table of video processing objects
     */
    @Inject
    private GroupTable<VideoProcessing> videoProcessingsTable;

    /**
     * Object to create components of UI
     */
    @Inject
    private UiComponents components;

    /**
     * Object for creating of screens
     */
    @Inject
    private Screens screens;

    /**
     * File loader
     */
    @Inject
    private FileLoader fileLoader;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(VideoProcessingBrowse.class);

    /**
     * Handler for init event
     * @see com.haulmont.cuba.gui.screen.Screen.InitEvent
     * @see Subscribe
     * @param event event object
     */
    @Subscribe
    private void onInit(InitEvent event){
        log.info("On init event has started");

        ScreenOptions options = event.getOptions();
        if(options instanceof MapScreenOptions){
            UUID nodeId = (UUID) ((MapScreenOptions) options).getParams().get("nodeId");
            videoProcessingsDl.setParameter("nodeId", nodeId);
        }

        videoProcessingsTable.addGeneratedColumn("openVideoProcessing", this::playVideo);

        log.info("On init event has finished");
    }

    /**
     * Creates a button to play a video
     * @see com.company.platform.entity.Video
     * @see VideoProcessing
     * @param videoProcessing video processing object from which the video is taken and played
     * @return Button
     */
    private Component playVideo(VideoProcessing videoProcessing){
        Button playVideoButton = components.create(Button.NAME);
        playVideoButton.setCaption("Play video");
        playVideoButton.addClickListener((event) -> {
            EmptyScreen screen = screens.create(EmptyScreen.class, OpenMode.NEW_TAB);
            Layout playerLayout = screen.getPageLayout().unwrap(Layout.class);
            screen.addAfterShowListener(event1 -> {
                VideoPlayerUtils.renderVideoPlayer(fileLoader, playerLayout, videoProcessing.getVideo());
            });
            screen.show();
        });

        return playVideoButton;
    }
}