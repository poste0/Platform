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

    private boolean isStop = false;

    private List<CameraService> services;

    private final SecurityContext context = AppContext.getSecurityContext();

    private Executor executor;

    @Subscribe
    public void onInit(InitEvent event){
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
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


    @Subscribe
    public void onShowInit(AfterShowEvent event){
        services = new ArrayList<>();
        createServices();
    }

    private void createServices(){
        List t = camerasDc.getItems();
        t.forEach(new Consumer<Camera>() {
            @Override
            public void accept(Camera camera) {
                System.out.println(AppContext.getApplicationContext().isSingleton(CameraService.NAME));
                CameraService service = AppBeans.getPrototype(CameraService.NAME);
                service.setCamera(camera);
                services.add(service);
                System.out.println(124124);
            }
        });
    }



    public void checkConnection() {
        Camera item = camerasTable.getSingleSelected();
        CameraService service = null;
        for(CameraService temp: services){
            if(temp.getCamera().getAddress().equals(item.getAddress())){
                service = temp;
                break;
            }
        }
        try {
            boolean test = service.testConnection();
            isVideo.setValue(test ? "Yes" : "Not");
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    public void write() throws FrameGrabber.Exception, FrameRecorder.Exception {
        Camera item = camerasTable.getSingleSelected();
        File file;
        File path = new File(item.getId().toString());
        if(!path.exists()) {
            path.mkdir();
        }
        file = new File(path.getAbsolutePath() + "/" + LocalDateTime.now() + ".avi");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CameraService service = null;
        for(CameraService temp: services){
            if(temp.getCamera().getAddress().equals(item.getAddress())){
                service = temp;
                break;
            }
        }
        Thread thread;

        final CameraService temp = service;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    temp.start(file);
                    temp.write(file);
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.setDaemon(true);
        //thread.run();
        executor = new ConcurrentTaskExecutor();
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AppContext.setSecurityContext(context);
                        temp.start(file);
                        temp.write(file);
                    } catch (FrameRecorder.Exception e) {
                        e.printStackTrace();
                    } catch (FrameGrabber.Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            isVideo.setValue("recording");
        }
        catch (Exception e){
            isVideo.setValue("Error");
        }
        System.out.println("qwrqwrqwr");
        /*Camera item = camerasTable.getSingleSelected();
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(item.getAddress());
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("asdasd.avi", grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.start();
        int e = 0;
        while(e < 100){
            Frame f = grabber.grabImage();
            recorder.record(f);
            e++;
        }*/

    }

    public void stop() {
        executor = new ConcurrentTaskExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                AppContext.setSecurityContext(context);
                Camera item = camerasTable.getSingleSelected();
                CameraService service = null;
                for(CameraService temp: services){
                    if(temp.getCamera().getAddress().equals(item.getAddress())){
                        service = temp;
                        break;
                    }
                }
                try {
                    service.stop();
                } catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }




}