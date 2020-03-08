package com.company.platform.web.screens.live;

import com.company.platform.entity.Camera;
import com.company.platform.service.StreamService;
import com.haulmont.cuba.gui.components.BoxLayout;
import com.haulmont.cuba.gui.components.PopupView;
import com.haulmont.cuba.gui.screen.*;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Video;

import javax.inject.Inject;

@UiController("platform_LiveScreen")
@UiDescriptor("live-screen.xml")
public class LiveScreen extends Screen {
    @Inject
    private BoxLayout liveBox;

    private ScreenOptions options;

    @Inject
    private StreamService service;

    @Subscribe
    public void onInit(InitEvent event){
        this.options = event.getOptions();
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        Camera camera = null;
        if(options instanceof MapScreenOptions){
            camera = (Camera) ((MapScreenOptions) options).getParams().get("camera");
        }
        else{
            throw new IllegalArgumentException();
        }

        service.startStream(camera);

        Layout layout = liveBox.unwrap(Layout.class);
        Video video = new Video();
        video.setId("streamVideo");
        layout.addComponent(video);
        liveBox.setVisible(true);
        layout.getUI().getPage().addDependency(new Dependency(Dependency.Type.JAVASCRIPT, "https://cdn.jsdelivr.net/npm/hls.js@latest"));
        layout.getUI().getPage().getJavaScript().execute(" function stream(){ var video = document.getElementById('streamVideo');" +
                "  if(Hls.isSupported()) {\n" +
                "    var hls = new Hls();\n" +
                "    hls.loadSource('http://127.0.0.1:80" + "/" + camera.getName() + ".m3u8" + "');\n" +
                "    hls.attachMedia(video);\n" +
                "    hls.on(Hls.Events.MANIFEST_PARSED,function() {\n" +
                "      video.play();\n" +
                "  });\n" +
                "    hls.on(Hls.Events.ERROR,function() {\n" +
                "      setTimeout(stream, 5000);\n" +
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
    }

    @Subscribe
    public void afterClose(AfterCloseEvent event){
        Camera camera = null;
        if(options instanceof MapScreenOptions){
            camera = (Camera) ((MapScreenOptions) options).getParams().get("camera");
        }
        else{
            throw new IllegalArgumentException();
        }

        service.stopStream(camera);
    }
}