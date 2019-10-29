package com.company.platform.web.screens.camera;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
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
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

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

    @Subscribe
    public void onInit(InitEvent event){
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
        service.init();
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
        String address = item.getAddress();
        FFmpegFrameGrabber grabber = null; //service.getGrabber(address);
        grabber.start();
        boolean result = grabber.hasVideo();
        grabber.stop();
        grabber = null;
        return result;

    }

    public void write() throws FrameGrabber.Exception, FrameRecorder.Exception {
        Camera item = camerasTable.getSingleSelected();
        UserSession session = AppBeans.get(UserSessionSource.class).getUserSession();
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
        service.stop(item);
    }




}