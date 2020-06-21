package com.company.platform.web.screens.live;

import com.company.platform.entity.Camera;
import com.company.platform.service.StreamService;
import com.haulmont.cuba.gui.components.BoxLayout;
import com.haulmont.cuba.gui.components.PopupView;
import com.haulmont.cuba.gui.screen.*;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@UiController("platform_LiveScreen")
@UiDescriptor("live-screen.xml")
public class LiveScreen extends Screen {
    @Inject
    private BoxLayout liveBox;

    private ScreenOptions options;

    @Inject
    private StreamService service;

    private static final Logger log = LoggerFactory.getLogger(LiveScreen.class);

    @Subscribe
    public void onInit(InitEvent event){
        log.info("On init event has started");

        this.options = event.getOptions();

        log.info("On init event has finished");
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        log.info("On after show event has started");

        Camera camera = null;
        if(options instanceof MapScreenOptions){
            log.info("Camera is received");
            camera = (Camera) ((MapScreenOptions) options).getParams().get("camera");
        }
        else{
            log.error("Camera is not received");
            throw new IllegalArgumentException();
        }

        service.startStream(camera);
        log.info("Stream has started");

        Layout layout = liveBox.unwrap(Layout.class);
        Video video = new Video();
        video.setId("streamVideo");
        video.setShowControls(false);
        layout.addComponent(video);
        liveBox.setVisible(true);
        layout.getUI().getPage().addDependency(new Dependency(Dependency.Type.JAVASCRIPT, "https://cdn.jsdelivr.net/npm/hls.js@latest"));
        layout.getUI().getPage().getJavaScript().execute(" function stream(){ var video = document.getElementById('streamVideo');" +
                "  if(Hls.isSupported()) {\n" +
                "    var hls = new Hls();\n" +
                "    var url = document.location.href.split('/');\n" +
                "    url = url[0].concat('//').concat(url[2]).concat('/" + camera.getName() + ".m3u8');\n" +
                "    hls.loadSource(url);\n" +
                "    hls.attachMedia(video); var isLoaded = false;\n" +
                "    hls.on(Hls.Events.MANIFEST_PARSED,function() {\n" +
                "     video.play(); isLoaded = true;\n" +
                "  });\n" +
                "    hls.on(Hls.Events.ERROR,function() {\n" +
                "      setTimeout(()=>{if(!isLoaded){hls.loadSource(url);}\n" +
                "      }, 5000)\n" +
                "  });\n" +
                " }\n" +
                " // hls.js is not supported on platforms that do not have Media Source Extensions (MSE) enabled.\n" +
                " // When the browser has built-in HLS support (check using `canPlayType`), we can provide an HLS manifest (i.e. .m3u8 URL) directly to the video element through the `src` property.\n" +
                " // This is using the built-in support of the plain video element, without using hls.js.\n" +
                " // Note: it would be more normal to wait on the 'canplay' event below however on Safari (where you are most likely to find built-in HLS support) the video.src URL must be on the user-driven\n" +
                " // white-list before a 'canplay' event will be emitted; the last video event that can be reliably listened-for when the URL is not on the white-list is 'loadedmetadata'.\n" +
                "  else if (video.canPlayType('application/vnd.apple.mpegurl')) {\n" +
                "    video.src = 'https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8';\n" +
                "    video.addEventListener('loadedmetadata',function() {\n" +
                "      video.play();\n" +
                "    });\n" +
                "  }} stream();");

        log.info("On after show event has finished");
    }

    @Subscribe
    public void afterClose(AfterCloseEvent event){
        log.info("After close event has started");

        Camera camera = null;
        if(options instanceof MapScreenOptions){
            log.info("Camera is received");
            camera = (Camera) ((MapScreenOptions) options).getParams().get("camera");
        }
        else{
            log.error("Camera is not received");
            throw new IllegalArgumentException();
        }

        service.stopStream(camera);
        log.info("Stream has finished");
    }
}