package com.company.platform.web.screens.camera;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.DataGridItems;
import com.haulmont.cuba.gui.components.data.TableItems;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Camera;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.gui.components.CompositeComponent;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebLabel;
import com.haulmont.cuba.web.gui.components.WebTextField;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
                temp.setEditable(false);
                temp.setSizeFull();
                return temp;
        }
    };

    @Subscribe
    public void onInit(InitEvent event){
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
        service.init();
        camerasTable.addGeneratedColumn("d", recordStatus);
        writeButton.addClickListener(new Consumer<Button.ClickEvent>() {
            @Override
            public void accept(Button.ClickEvent clickEvent) {
                camerasTable.addGeneratedColumn("d", recordStatus);
            }
        });
        stopButton.addClickListener(new Consumer<Button.ClickEvent>() {
            @Override
            public void accept(Button.ClickEvent clickEvent) {
                camerasTable.addGeneratedColumn("d", recordStatus);
            }
        });
        /*camerasTable.addSelectionListener(new Consumer<Table.SelectionEvent<Camera>>() {
            @Override
            public void accept(Table.SelectionEvent<Camera> event) {
                Camera item = event.getSelected().iterator().next();
                CameraService cameraService = null;
                services.forEach(cameraService1 -> {
                    System.out.println(cameraService1.getCamera().getAddress());
                });
                for(CameraService service: services){
                    if(service.getCamera().getId().equals(item.getId())){
                        cameraService = service;
                    }
                }
                boolean isStopActive = false;
                if(cameraService.isRecording()){
                    isStopActive = true;
                }
                stopButton.setEnabled(isStopActive);
                writeButton.setEnabled(!isStopActive);
            }
        });*/
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

    public void write() throws FrameGrabber.Exception, FrameRecorder.Exception {
        Camera item = camerasTable.getSingleSelected();
        if(Objects.isNull(item)){
            throw new IllegalArgumentException();
        }
        service.write(item);
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