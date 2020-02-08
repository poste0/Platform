package com.company.platform.web.screens.video;

import com.company.platform.entity.Camera;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.chile.core.model.impl.MetaClassImpl;
import com.haulmont.chile.core.model.impl.MetaModelImpl;
import com.haulmont.cuba.core.app.FileStorageService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Layout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UiController("platform_Video")
@UiDescriptor("video.xml")
public class Video extends Screen {
    @Inject
    private DataLoader camerasDl;




    @Inject
    private CollectionContainer<Camera> camerasDc;

    @Inject
    TabSheet video;

    @Inject
    private UiComponents components;

    @Inject
    private BoxLayout playerBox;

    @Inject
    private DataManager dataManager;
    private class Renderer{




        private Label videoname;

        private Button watchButton;

        private Button deleteButton;

        private Button perform;

        private GridLayout layout;

        private List<Path> paths;

        private List<Camera> cameras;


        public Renderer(List<Camera> cameras){
            this.cameras = cameras;
        }

        private List<FileDescriptor> getPaths(Camera camera, String format) throws IOException {
            /*if(Objects.isNull(camera)){
                throw new IllegalArgumentException();
            }
            System.out.println(camera.getId());
            File path = new File(camera.getName().toString());
            if(!path.exists()){
                path.createNewFile();
            }
            Stream<Path> stream = Files.walk(Paths.get(path.toString()), FileVisitOption.FOLLOW_LINKS);
            List<Path> result = stream.filter((value)->{
                return value.toFile().getName().contains(format) ? true : false;
            }).collect(Collectors.toList());
            return result;

             */
            List<FileDescriptor> result = new ArrayList<>();
            LoadContext<FileDescriptor> context = LoadContext.create(FileDescriptor.class).setQuery(LoadContext.createQuery("SELECT f FROM sys$FileDescriptor f"));
            DataManager manager = AppBeans.get(DataManager.class);
            FileLoader loader = AppBeans.get(FileLoader.class);
            manager.loadValue("SELECT f FROM sys$FileDescriptor f", FileDescriptor.class).list().stream().filter(new Predicate<FileDescriptor>() {
                @Override
                public boolean test(FileDescriptor descriptor) {
                    return descriptor.getCreatedBy().equals(AppBeans.get(UserSessionSource.class).getUserSession().getUser().getLogin()) && !descriptor.isDeleted();
                }
            }).forEach(new Consumer<FileDescriptor>() {
                @Override
                public void accept(FileDescriptor descriptor) {
                    File file = new File(descriptor.getName() + ".mp4");
                    result.add(descriptor);
                }
            });
            return result;
        }

        private void setUpLayout(int columnSize, int rowSize){
            if(columnSize <= 0 || rowSize <= 0){
                throw new IllegalArgumentException();
            }
            layout = components.create(GridLayout.NAME);
            layout.setColumns(columnSize);
            layout.setRows(rowSize);
        }

        public void render() throws IOException {
            for(Camera camera: cameras){
                List<FileDescriptor> paths = getPaths(camera, ".mp4");
                List<FileDescriptor> tempPaths = getPaths(camera, ".mp4");
                if(paths.size() == 0){
                    continue;
                }
                FileLoader loader = AppBeans.get(FileLoader.class);
                setUpLayout(4, paths.size());
                for(FileDescriptor path: paths){
                    videoname = components.create(Label.NAME);
                    watchButton = components.create(Button.NAME);
                    videoname.setValue(path.getName());
                    watchButton.setCaption("Watch");
                    System.out.println(path.getName());
                    watchButton.addClickListener((clickEvent->{
                        com.vaadin.ui.Video video = new com.vaadin.ui.Video();
                        video.setSource(new StreamResource(new StreamResource.StreamSource() {
                            @Override
                            public InputStream getStream() {
                                try {
                                    return loader.openStream(path);
                                } catch (FileStorageException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        }, path.getName() + ".mp4"));
                        video.setStyleName("video/mp4");
                        Layout videoLayout = playerBox.unwrap(Layout.class);
                        videoLayout.removeAllComponents();
                        videoLayout.addComponent(video);

                    }));
                    deleteButton = components.create(Button.NAME);
                    deleteButton.setCaption("Delete");
                    deleteButton.addClickListener((clickEvent -> {
                        //Files.delete(path);
                        //Files.delete(tempPaths.get(paths.indexOf(path)));
                        try {
                            loader.removeFile(path);
                            path.setDeleteTs(new Date());
                            dataManager.commit(path);
                            Files.delete(new File(path.getName() + ".mp4").toPath());
                        } catch (FileStorageException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        layout.remove(layout.getComponent(0, paths.indexOf(path)));
                        layout.remove(layout.getComponent(1, paths.indexOf(path)));
                        layout.remove(layout.getComponent(2, paths.indexOf(path)));
                        layout.remove(layout.getComponent(3, paths.indexOf(path)));
                        video.addTab(camera.getAddress(), layout);
                    }));
                    perform = components.create(Button.NAME);
                    perform.setCaption("Go darkflow");
                    perform.addClickListener((clickEvent -> {
                        Executor executor = new ConcurrentTaskExecutor();
                        executor.execute(() -> {
                            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                            FileSystemResource value = new FileSystemResource(new File(path.getName() + ".mp4"));
                            System.out.println(value.getFile().length());
                            map.add("file", value);
                            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                            factory.setBufferRequestBody(false);
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
                            RestTemplate restTemplate = new RestTemplate(factory);
                            restTemplate.exchange("http://localhost:8080/file", HttpMethod.POST, requestEntity, String.class);
                        });


                    }));
                    layout.add(videoname, 0, paths.indexOf(path));
                    layout.add(watchButton, 1, paths.indexOf(path));
                    layout.add(deleteButton, 2,  paths.indexOf(path));
                    layout.add(perform, 3, paths.indexOf(path));
                }
                video.addTab(camera.getAddress(), layout);
            }
        }


    }

    @Subscribe
    public void onInit(InitEvent event){
        camerasDl.setParameter("user", AppBeans.get(UserSessionSource.class).getUserSession().getUser().getId());
        camerasDl.load();
        List<Camera> cameras = camerasDc.getItems();
        Renderer renderer = new Renderer(cameras);
        try {
            renderer.render();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*int j = 1;
        for(Camera camera: cameras){
            File path = new File(camera.getId().toString());
            grid = components.create(GridLayout.NAME);
            grid.setColumns(2);
            Label label = components.create(Label.NAME);
            grid.add(label, 0, 0);
            try {
                Stream<Path> stream = Files.walk(Paths.get(path.toString()), FileVisitOption.FOLLOW_LINKS);
                List<Path> paths = stream.filter(new Predicate<Path>() {
                    @Override
                    public boolean test(Path path) {
                        return path.toFile().getName().contains(".avi") ? false : true;
                    }
                }).collect(Collectors.toList());
                grid.setRows(paths.size());
                for(int i = 1; i < paths.size(); i++){
                    Label cameraL = components.create(Label.NAME);
                    cameraL.setValue(camera.getAddress());
                    Label videoL = components.create(Label.NAME);
                    String[] names = paths.get(i).toString().split("/");
                    videoL.setValue(names[names.length - 1]);
                    paths.forEach(System.out::println);
                    grid.add(videoL, 0, j);
                    Button button = components.create(Button.NAME);
                    button.setCaption("Watch it");
                    int p = i;
                    button.addClickListener(new Consumer<Button.ClickEvent>() {
                        @Override
                        public void accept(Button.ClickEvent clickEvent) {
                            label.setValue(grid.getComponent(0, p));
                            Layout layout = playerBox.unwrap(Layout.class);
                            System.out.println(paths.get(p));
                            com.vaadin.ui.Video video = new com.vaadin.ui.Video();
                            video.setSource(new FileResource(new File(paths.get(p).toString())));
                            video.setStyleName("video/mp4");
                            layout.addComponent(video);
                            video.play();
                        }
                    });
                    grid.add(button, 1, j);
                    j++;
                }
                video.addTab(camera.getAddress(), grid);
            } catch (IOException e) {
                e.printStackTrace();
            }



        }

         */


    }
}