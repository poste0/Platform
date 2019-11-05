package com.company.platform.web.screens.video;

import com.company.platform.entity.Camera;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.chile.core.model.impl.MetaClassImpl;
import com.haulmont.chile.core.model.impl.MetaModelImpl;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Layout;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

    private class Renderer{




        private Label videoname;

        private Button watchButton;

        private GridLayout layout;

        private List<Path> paths;

        private List<Camera> cameras;
        public Renderer(List<Camera> cameras){
            this.cameras = cameras;
        }

        private List<Path> getPaths(Camera camera) throws IOException {
            File path = new File(camera.getId().toString());
            Stream<Path> stream = Files.walk(Paths.get(path.toString()), FileVisitOption.FOLLOW_LINKS);
            List<Path> result = stream.filter((value)->{
                return value.toFile().getName().contains(".mp4") ? true : false;
            }).collect(Collectors.toList());
            return result;
        }

        public void render() throws IOException {
            for(Camera camera: cameras){
                List<Path> paths = getPaths(camera);
                layout = components.create(GridLayout.NAME);
                layout.setColumns(2);
                layout.setRows(paths.size());


                for(Path path: paths){
                    videoname = components.create(Label.NAME);
                    watchButton = components.create(Button.NAME);
                    videoname.setValue(camera.getAddress());
                    watchButton.setCaption("Watch");
                    watchButton.addClickListener((clickEvent->{
                        com.vaadin.ui.Video video = new com.vaadin.ui.Video();
                        video.setSource(new FileResource(new File(path.toString())));
                        video.setStyleName("video/mp4");
                        Layout videoLayout = playerBox.unwrap(Layout.class);
                        videoLayout.addComponent(video);
                    }));
                    layout.add(videoname, 0, paths.indexOf(path));
                    layout.add(watchButton, 1, paths.indexOf(path));
                }

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