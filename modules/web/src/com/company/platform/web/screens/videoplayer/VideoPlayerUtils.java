package com.company.platform.web.screens.videoplayer;

import com.company.platform.entity.Video;
import com.haulmont.cuba.core.global.FileLoader;
import com.haulmont.cuba.core.global.FileStorageException;
import com.vaadin.server.ConnectorResource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.vaadin.gwtav.ContentLengthConnectorResource;
import org.vaadin.gwtav.GwtVideo;

import java.io.InputStream;

public class VideoPlayerUtils {
    private static final Logger log = LoggerFactory.getLogger(VideoPlayerUtils.class);

    public static void renderVideoPlayer(FileLoader loader, Layout layout, Video video, Component... components){
        GwtVideo videoPlayer = new GwtVideo();
        StreamResource videoStreamResource = new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    log.info("File {} is loaded", video.getFileDescriptor().getName());
                    return loader.openStream(video.getFileDescriptor());
                } catch (FileStorageException e) {
                    log.error("File {} is not loaded", video.getFileDescriptor().getName());
                    e.printStackTrace();
                }
                return null;
            }
        }, video.getName() + ".mp4");
        videoPlayer.setSource(new ContentLengthConnectorResource(videoStreamResource, video.getFileDescriptor().getSize()));
        videoPlayer.setStyleName("video/mp4");
        videoPlayer.setId("streamVideo");
        videoPlayer.addStyleName("video-js");
        videoPlayer.setAutoplay(false);
        final String attributeJs = "var player = document.getElementById('streamVideo'); player.setAttribute('data-setup', '{}')";
        layout.getUI().getPage().addDependency(new Dependency(Dependency.Type.JAVASCRIPT, "https://vjs.zencdn.net/7.8.2/video.js"));
        layout.getUI().getPage().addDependency(new Dependency(Dependency.Type.STYLESHEET, "https://vjs.zencdn.net/7.8.2/video-js.css"));
        layout.getUI().getPage().getJavaScript().execute(attributeJs);

        layout.removeAllComponents();
        layout.addComponent(videoPlayer);
        for(Component component: components){
            layout.addComponent(component);
        }
    }

    public static void renderVideoPlayer(FileLoader loader, Layout layout, Video video){
        com.vaadin.ui.Button stopButton = new com.vaadin.ui.Button();
        stopButton.setCaption("Stop");
        stopButton.addClickListener(stopEvent -> {
            layout.removeAllComponents();
        });

        renderVideoPlayer(loader, layout, video, stopButton);
    }

}
