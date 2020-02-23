package com.company.platform.web.screens.camera;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.company.platform.service.StreamService;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.CubaXmlWebApplicationContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebTextField;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Video;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@UiController("platform_Camera.browse")
@UiDescriptor("camera-browse.xml")
@LookupComponent("camerasTable")
@LoadDataBeforeShow
public class CameraBrowse extends StandardLookup<Camera> {
    @Inject
    private DataLoader camerasDl;

    @Inject
    private CollectionContainer camerasDc;

    @Inject
    private GroupTable<Camera> camerasTable;

    @Inject
    private TextField<String> isVideo;

    @Inject
    private Button check;

    @Inject
    private Button writeButton;

    @Inject
    private Button stopButton;

    @Inject
    private CameraService service;

    private boolean isStop = false;

    private List<CameraService> services;

    private final SecurityContext context = AppContext.getSecurityContext();

    private Executor executor;

    private boolean isRecording;

    private final Screen screen = this;

    @Inject
    private DataManager dataManager;

    @Inject
    private StreamService streamService;

    @Inject
    private PopupView livePopup;

    @Inject
    private BoxLayout liveBox;



    private final Table.ColumnGenerator recordStatus = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {

            if (Objects.isNull(entity)) {
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;
            TextField temp = new WebTextField();
            temp.setEnabled(false);
            System.out.println(service.getStatus(camera).toString());
            temp.setValue(service.getStatus(camera).toString());
            temp.setStyleName("c-camera-record-status-field");
            return temp;
        }
    };

    private final Table.ColumnGenerator recordButton = new Table.ColumnGenerator(){

        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)) {
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            Button temp = new WebButton();
            temp.setCaption("Record");

            if(service.getStatus(camera).equals(CameraService.Status.RECORDING) || service.getStatus(camera).equals(CameraService.Status.NOT_CONNECTED)){
                temp.setEnabled(false);
                return temp;
            }
            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    write();
                    onInit(null);

                }
            });
            return temp;
        }
    };

    private final Table.ColumnGenerator stoppButton = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)){
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            Button temp = new WebButton();
            temp.setCaption("Stop");
            if(!service.getStatus(camera).equals(CameraService.Status.RECORDING)){
                temp.setEnabled(false);
                return temp;
            }
            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    stop();
                   onInit(null);

                }
            });
            return temp;
        }
    };

    private final Table.ColumnGenerator testButton = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)){
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            Button temp = new WebButton();
            temp.setCaption("Test");

            if(!service.getStatus(camera).equals(CameraService.Status.NOT_CONNECTED)){
                temp.setEnabled(false);
                return temp;
            }
            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {

                }
            });
            return temp;
        }
    };

    private final Table.ColumnGenerator liveButton = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)){
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;
            System.out.println("    hls.loadSource('http://127.0.0.1:80" + "/" + camera.getName() + ".m3u8" + "');\n");
            Button temp = new WebButton();
            temp.setCaption("Live");
            temp.addClickListener(event ->{
                live();
                Layout layout = liveBox.unwrap(Layout.class);
                Video video = new Video();
                video.setId("streamVideo");
                layout.addComponent(video);
                liveBox.setVisible(true);
                livePopup.setPopupVisible(true);
                livePopup.setHideOnMouseOut(false);
                layout.getUI().getPage().addDependency(new Dependency(Dependency.Type.JAVASCRIPT, "https://cdn.jsdelivr.net/npm/hls.js@latest"));
                layout.getUI().getPage().getJavaScript().execute(" var video = document.getElementById('streamVideo');" +
                        "  if(Hls.isSupported()) {\n" +
                        "    var hls = new Hls();\n" +
                        "    hls.loadSource('http://127.0.0.1:80" + "/" + camera.getName() + ".m3u8" + "');\n" +
                        "    hls.attachMedia(video);\n" +
                        "    hls.on(Hls.Events.MANIFEST_PARSED,function() {\n" +
                        "      video.play();\n" +
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
                        "  }");
            });

            return temp;
        }
    };



    private void addGeneratedColumns(){
        camerasTable.addGeneratedColumn("recordButton", recordButton);
        camerasTable.addGeneratedColumn("stoppButton", stoppButton);
        camerasTable.addGeneratedColumn("testButton", testButton);
        camerasTable.addGeneratedColumn("recordStatus", recordStatus);
        camerasTable.addGeneratedColumn("liveStreamButton", liveButton);
    }

    @Subscribe
    public void onInit(InitEvent event){
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
        service.init();
        streamService.init();
        addGeneratedColumns();

    }

    @Subscribe
    public void onClose(AfterCloseEvent event){

    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        camerasTable.getItems().getItems().forEach(camera -> {
            System.out.println(camera.getId());
        });
    }

    public void checkConnection() {
        Camera item = camerasTable.getSingleSelected();
        if(Objects.isNull(item)){
            return;
        }
        try {
            if (isConnected(item)) {
                isVideo.setValue("Ok");
            } else {
                isVideo.setValue("Bad");
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isConnected(Camera item) throws FrameGrabber.Exception {
        /*String address = item.getAddress();
        FFmpegFrameGrabber grabber = service.getGrabber(address);
        grabber.start();
        boolean result = grabber.hasVideo();
        grabber.stop();
        grabber = null;
        return result;

         */
        return true;
    }

    public void write() {
        Camera item = camerasTable.getSingleSelected();
        if(Objects.isNull(item)){
            throw new IllegalArgumentException();
        }
        try {
            service.write(item);
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            isVideo.setValue(e.getMessage());
        }
    }



    private void record(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder) throws FrameGrabber.Exception, FrameRecorder.Exception {
        while(isRecording) {
            Frame frame = grabber.grab();
            recorder.record(frame);
        }
    }

    public void stop() {
        Camera item = camerasTable.getSingleSelected();
        if(Objects.isNull(item)){
            throw new IllegalArgumentException();
        }
        service.stop(item);
    }

    public void live(){
        Camera item = camerasTable.getSingleSelected();
        if(Objects.isNull(item)){
            throw new IllegalArgumentException();
        }

        streamService.startStream(item);
    }

    public void stopLive(){
        Camera item = camerasTable.getSingleSelected();
        if(Objects.isNull(item)){
            throw new IllegalArgumentException();
        }

        streamService.stopStream(item);
    }




}