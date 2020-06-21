package com.company.platform.web.screens.camera;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.company.platform.service.StreamService;
import com.company.platform.web.screens.live.LiveScreen;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.CubaXmlWebApplicationContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.Screens;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
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
    private Screens screens;

    private static final Logger log = LoggerFactory.getLogger(CameraBrowse.class);



    private final Table.ColumnGenerator recordStatus = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {

            if (Objects.isNull(entity)) {
                log.error("Camera is null. Record status column generator");
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            log.info("Camera recording status {}", service.getStatus(camera).toString());

            TextField temp = new WebTextField();
            temp.setEnabled(false);
            temp.setValue(service.getStatus(camera).toString());
            temp.setStyleName("c-camera-record-status-field");
            return temp;
        }
    };

    private final Table.ColumnGenerator recordButton = new Table.ColumnGenerator(){

        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)) {
                log.error("Camera is null. Record button column generator");
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            Button temp = new WebButton();
            temp.setCaption("Record");

            if(service.getStatus(camera).equals(CameraService.Status.RECORDING) || service.getStatus(camera).equals(CameraService.Status.NOT_CONNECTED)){
                log.info("Camera with id {}, name {} can not be recorded", camera.getId(), camera.getName());
                temp.setEnabled(false);
                return temp;
            }

            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    log.info("Recording of camera with id {}, name {} has started", camera.getId(), camera.getName());
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
                log.error("Camera is null. Stop button column generator");
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            Button temp = new WebButton();
            temp.setCaption("Stop");

            if(!service.getStatus(camera).equals(CameraService.Status.RECORDING)){
                log.info("Camera with id {}, name {} is not recorder", camera.getId(), camera.getName());
                temp.setEnabled(false);
                return temp;
            }

            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    log.info("Camera with id {}, name {} has stopped recording", camera.getId(), camera.getName());
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
                log.error("Camera is null. Connection test button generator");
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            Button temp = new WebButton();
            temp.setCaption("Test");

            if(!service.getStatus(camera).equals(CameraService.Status.NOT_CONNECTED)){
                log.info("Camera with id {}, name {} is connected", camera.getId(), camera.getName());
                temp.setEnabled(false);
                return temp;
            }
            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    if(service.testConnection(camera)){
                        log.info("Camera with id {}, name {} is connected", camera.getId(), camera.getName());
                        onInit(null);
                    }
                }
            });
            return temp;
        }
    };

    public void createVideo(){
        File file = new File(String.valueOf(System.currentTimeMillis()));
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileDescriptor fileDescriptor = AppBeans.get(Metadata.class).create(FileDescriptor.class);

        fileDescriptor.setName(file.getName());
        fileDescriptor.setExtension("mp4");
        fileDescriptor.setCreateDate(new Date());
        fileDescriptor.setSize(file.getTotalSpace());

        FileLoader fileLoader = AppBeans.get(FileLoader.class);
        try {
            fileLoader.saveStream(fileDescriptor, ()->{
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    return null;
                }
            });
        } catch (FileStorageException e) {
            e.printStackTrace();
        }

        dataManager.commit(fileDescriptor);
    }
  
    private final Table.ColumnGenerator liveButton = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)){
                log.error("Camera is null. Live button generator");
                throw new IllegalArgumentException();
            }

            Camera camera = (Camera) entity;

            Button temp = new WebButton();
            temp.setCaption("Live");
            if(!service.testConnection(camera)){
                log.info("Camera with id {}, name {} is connected", camera.getId(), camera.getName());
                temp.setEnabled(false);
            }
            temp.addClickListener(event ->{
                log.info("Show live video has started");
                LiveScreen screen = screens.create(LiveScreen.class, OpenMode.DIALOG, new MapScreenOptions(Collections.singletonMap("camera", camera)));
                screen.show();
            });

            return temp;
        }
    };



    private void addGeneratedColumns(){
        log.info("Adding of generated columns has started");

        camerasTable.addGeneratedColumn("recordButton", recordButton);
        camerasTable.addGeneratedColumn("stoppButton", stoppButton);
        camerasTable.addGeneratedColumn("testButton", testButton);
        camerasTable.addGeneratedColumn("recordStatus", recordStatus);
        camerasTable.addGeneratedColumn("liveStreamButton", liveButton);

        log.info("Adding of generated columns has finished");
    }

    @Subscribe
    public void onInit(InitEvent event){
        log.info("On init event has started");
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
        service.init();
        streamService.init();
        addGeneratedColumns();

        log.info("On init event has finished");
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        log.info("On after show event has started");

        camerasTable.getItems().getItems().forEach(camera -> {
            log.info("Camera id {}", camera.getId());
        });

        log.info("On after show event has finished");
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
            log.error("Camera is null");
            throw new IllegalArgumentException();
        }

        try {
            log.info("Recording has started");
            service.write(item);
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            log.error("Recording error");
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
            log.error("Camera is null");
            throw new IllegalArgumentException();
        }
        log.info("Recording has stopped");
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