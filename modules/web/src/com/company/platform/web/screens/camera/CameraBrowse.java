package com.company.platform.web.screens.camera;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.executors.BackgroundTask;
import com.haulmont.cuba.gui.executors.BackgroundTaskHandler;
import com.haulmont.cuba.gui.executors.BackgroundWorker;
import com.haulmont.cuba.gui.executors.TaskLifeCycle;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebTextField;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

    private final Table.ColumnGenerator recordStatus = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {

            if(Objects.isNull(entity)){
                throw new IllegalArgumentException();
            }
                TextField temp = new WebTextField();
                if (service.isRecording((Camera) entity)) {
                    temp.setValue("Recording");
                } else {
                    temp.setValue("Waiting");
                }
            if(!service.testConnection((Camera) entity)){
                temp.setValue("Not connected");
            }
            temp.setEditable(false);
            temp.setSizeFull();
                return temp;
        }
    };

    private final Table.ColumnGenerator recordButton = new Table.ColumnGenerator(){

        @Override
        public Component generateCell(Entity entity) {
            if(Objects.isNull(entity)) {
                throw new IllegalArgumentException();
            }

            Button temp = new WebButton();
            temp.setCaption("Record");
            if(service.isRecording((Camera) entity)){
                temp.setEnabled(false);
                return temp;
            }
            if(!Objects.isNull(((Camera) entity).getStatus()) && ((Camera) entity).getStatus().equals(Camera.Status.NOT_CONNECTED)){
                temp.setEnabled(false);
                return temp;
            }
            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    try {
                        service.write((Camera) entity);
                        ((Camera) entity).setStatus(Camera.Status.RECORDING);
                    } catch (FrameGrabber.Exception e) {
                        e.printStackTrace();
                    } catch (FrameRecorder.Exception e) {
                        e.printStackTrace();
                    }
                    fireEvent(InitEvent.class, new InitEvent(screen, new MapScreenOptions(new HashMap<>())));

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

            Button temp = new WebButton();
            temp.setCaption("Stop");
            if(!service.isRecording((Camera) entity)){
                temp.setEnabled(false);
                return temp;
            }
            else{
                temp.setEnabled(true);
            }
            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    stop();
                    ((Camera) entity).setStatus(Camera.Status.CONNECTED);
                    fireEvent(InitEvent.class, new InitEvent(screen, new MapScreenOptions(new HashMap<>())));

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

            Button temp = new WebButton();
            temp.setCaption("Test");
            temp.addClickListener(new Consumer<Button.ClickEvent>() {
                @Override
                public void accept(Button.ClickEvent clickEvent) {
                    if(service.testConnection((Camera) entity)){
                        camerasTable.getSingleSelected().setStatus(Camera.Status.CONNECTED);
                    }
                    else{
                        camerasTable.getSingleSelected().setStatus(Camera.Status.NOT_CONNECTED);
                    }
                    fireEvent(InitEvent.class, new InitEvent(screen, new MapScreenOptions(new HashMap<>())));
                }
            });
            return temp;
        }
    };



    private void addGeneratedColumns(){
        camerasTable.addGeneratedColumn("recordButton", recordButton);
        camerasTable.addGeneratedColumn("stoppButton", stoppButton);
        camerasTable.addGeneratedColumn("testButton", testButton);
    }
    @Subscribe
    public void onInit(InitEvent event){
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
        service.init();
        addGeneratedColumns();
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
            Files.walk(Paths.get(path.toString()))
                    .filter(path1 -> path1.toFile().getName().contains(".avi"))
                    .collect(Collectors.toList()).forEach(path12 -> {
                        try {
                            Runtime.getRuntime().exec("ffmpeg -i " + path12.toString() + " " + path12.toString().substring(0, path12.toString().length() - 5) + ".mp4");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

      
    }




}