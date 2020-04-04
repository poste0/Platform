package com.company.platform.web.screens.video;

import com.company.platform.entity.Camera;
import com.company.platform.entity.Node;
import com.company.platform.service.NodeService;
import com.company.platform.web.screens.ConfirmScreen;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.gui.components.WebOptionsList;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Inject
    private Screens screens;

    @Inject
    private NodeService nodeService;

    private class Renderer{




        private Label videoname;

        private Button watchButton;

        private Button deleteButton;

        private Button perform;

        private List<Node> nodes;

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
            User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
            UUID userId = user.getId();
            //nodes = dataManager.loadValue("SELECT n FROM Node n WHERE n.user.id = :user", Node.class).setParameters(Collections.singletonMap("user", userId)).list();
            nodes = dataManager.loadList(LoadContext.create(Node.class).setQuery(LoadContext.createQuery("SELECT n FROM platform_Node n WHERE n.user.id = :user").setParameter("user", userId)));
            for(Camera camera: cameras){
                List<FileDescriptor> paths = getPaths(camera, ".mp4");
                List<FileDescriptor> tempPaths = getPaths(camera, ".mp4");
                if(paths.size() == 0){
                    continue;
                }
                FileLoader loader = AppBeans.get(FileLoader.class);
                setUpLayout(5, paths.size());
                List<com.company.platform.entity.Video> videos = dataManager.loadList(LoadContext.create(com.company.platform.entity.Video.class).setQuery(LoadContext.createQuery("SELECT v FROm platform_Video v WHERE v.camera.id = :camera").setParameter("camera", camera.getId())));
                videos.forEach(video1 -> {
                    System.out.println(video1.getName() + "video");
                });
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
                        video.setId("streamVideo");
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

                    WebOptionsList<String, String> nodeList = components.create(OptionsList.NAME);
                    nodeList.setOptionsMap(nodes.stream()
                                                    .filter(node -> {
                                                        return nodeService.getStatus(node).equals("false");
                                                    })
                                                    .collect(Collectors.toMap(new Function<Node, String>() {
                        @Override
                        public String apply(Node node) {
                            return node.getName();
                        }
                    }, new Function<Node, String>() {
                        @Override
                        public String apply(Node node) {
                            return node.getAddress();
                        }
                    })));
                    nodeList.setHeight("40");

                    perform = components.create(Button.NAME);
                    perform.setCaption("Go darkflow");
                    perform.addClickListener((clickEvent -> {
                        AtomicReference<String> login = new AtomicReference<>("");
                        AtomicReference<String> password = new AtomicReference<>("");

                        Map<String, Object> confirmScreenOptionsMap = new HashMap<>();
                        confirmScreenOptionsMap.put("user", user);
                        confirmScreenOptionsMap.put("login", login);
                        confirmScreenOptionsMap.put("password", password);

                        AtomicBoolean isConfirmed = new AtomicBoolean(false);

                        MapScreenOptions confirmScreenOptions = new MapScreenOptions(confirmScreenOptionsMap);

                        ConfirmScreen confirmScreen = screens.create(ConfirmScreen.class, OpenMode.DIALOG, confirmScreenOptions);
                        confirmScreen.show();
                        confirmScreen.addAfterCloseListener(event -> {
                            login.set((String) confirmScreenOptions.getParams().get("login"));
                            password.set((String) confirmScreenOptions.getParams().get("password"));
                            if(StringUtils.isEmpty(login) || StringUtils.isEmpty(password)){
                                throw new AuthenticationCredentialsNotFoundException("login and password are not confirmed");
                            }

                            PasswordEncryption encryption = AppBeans.get(PasswordEncryption.NAME);

                            if(encryption.checkPassword(user, password.get()) && login.get().equals(user.getLogin())){
                                isConfirmed.set(true);
                            }

                            final SecurityContext context = AppContext.getSecurityContext();
                            if(isConfirmed.get()) {
                                Executor executor = new ConcurrentTaskExecutor();
                                executor.execute(() -> {
                                    AppContext.setSecurityContext(context);
                                    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                                    File file = new File(path.getName());
                                    try {
                                        FileUtils.copyInputStreamToFile(loader.openStream(path), file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (FileStorageException e) {
                                        e.printStackTrace();
                                    }
                                    FileSystemResource value = new FileSystemResource(file);
                                    System.out.println(value.getFile().length());
                                    map.add("file", value);
                                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                                    factory.setBufferRequestBody(false);
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                                    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
                                    RestTemplate restTemplate = new RestTemplate(factory);
                                    restTemplate.exchange(nodeList.getValue() + "/file?login=" + user.getLogin() + "&password=" + password.get() , HttpMethod.POST, requestEntity, String.class);
                                });
                            }
                        });



                    }));

                    layout.add(videoname, 0, paths.indexOf(path));
                    layout.add(watchButton, 1, paths.indexOf(path));
                    layout.add(deleteButton, 2,  paths.indexOf(path));
                    layout.add(perform, 3, paths.indexOf(path));
                    layout.add(nodeList, 4, paths.indexOf(path));
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