package com.company.platform.web.screens.camera;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebTextField;
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
                   onInit(null);
                }
            });
            return temp;
        }
    };



    private void addGeneratedColumns(){
        camerasTable.addGeneratedColumn("recordButton", recordButton);
        camerasTable.addGeneratedColumn("stoppButton", stoppButton);
        camerasTable.addGeneratedColumn("testButton", testButton);
        camerasTable.addGeneratedColumn("recordStatus", recordStatus);
    }

    @Subscribe
    public void onInit(InitEvent event){
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
        service.init();
        addGeneratedColumns();

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
      File path = new File(item.getId().toString());
        try {
            service.stop(item);
            String post = String.valueOf(Files.walk(Paths.get(path.toString()))
                    .filter(path1 -> path1.toFile().getName().contains(".avi"))
                    .count());
            Files.walk(Paths.get(path.toString()))
                    .filter(path1 -> path1.toFile().getName().contains(".avi"))
                    .collect(Collectors.toList()).forEach(path12 -> {
                        try {
                            Runtime.getRuntime().exec("ffmpeg -i " + path12.toString() + " " + path12.toString().substring(0, path12.toString().length() - 5) + post + ".mp4");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

      
    }




}