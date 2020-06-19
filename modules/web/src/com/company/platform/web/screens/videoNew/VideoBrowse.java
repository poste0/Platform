package com.company.platform.web.screens.videoNew;

import com.company.platform.entity.Node;
import com.company.platform.service.NodeService;
import com.company.platform.web.screens.ConfirmScreen;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataLoader;
import com.haulmont.cuba.gui.screen.*;
import com.company.platform.entity.Video;
import com.haulmont.cuba.gui.screen.LookupComponent;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.AppUI;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.Layout;
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
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@UiController("platform_Video.browse")
@UiDescriptor("video-browse.xml")
@LookupComponent("videosTable")
@LoadDataBeforeShow
public class VideoBrowse extends StandardLookup<Video> {
    @Inject
    private UiComponents components;

    @Inject
    private FileLoader loader;

    @Inject
    private GroupTable videosTable;

    @Inject
    private BoxLayout playerBox;

    private Layout layout;

    @Inject
    private DataManager dataManager;

    @Inject
    private DataLoader videosDl;

    @Inject
    private Screens screens;

    private List<Node> nodes;

    private Map<Video, LookupField<String>> nodeMap;

    @Inject
    private NodeService nodeService;

    private final Table.ColumnGenerator WATCH = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            Video video = (Video) entity;

            Button button = components.create(Button.NAME);
            button.setCaption("Watch");
            button.addClickListener(event -> {
                layout = playerBox.unwrap(Layout.class);
                com.vaadin.ui.Video videoPlayer = new com.vaadin.ui.Video();
                videoPlayer.setSource(new StreamResource(new StreamResource.StreamSource() {
                    @Override
                    public InputStream getStream() {
                        try {
                            return loader.openStream(video.getFileDescriptor());
                        } catch (FileStorageException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }, video.getName() + ".mp4"));
                videoPlayer.setStyleName("video/mp4");
                videoPlayer.setId("streamVideo");
                videoPlayer.addStyleName("video-js");
                final String attributeJs = "var player = document.getElementById('streamVideo'); player.setAttribute('data-setup', '{}')";
                layout.getUI().getPage().addDependency(new Dependency(Dependency.Type.JAVASCRIPT, "https://vjs.zencdn.net/7.8.2/video.js"));
                layout.getUI().getPage().addDependency(new Dependency(Dependency.Type.STYLESHEET, "https://vjs.zencdn.net/7.8.2/video-js.css"));
                layout.getUI().getPage().getJavaScript().execute(attributeJs);

                com.vaadin.ui.Button stopButton = new com.vaadin.ui.Button();
                stopButton.setCaption("Stop");
                stopButton.addClickListener(stopEvent -> {
                    layout.removeAllComponents();
                });

                layout.removeAllComponents();
                layout.addComponent(videoPlayer);
                layout.addComponent(stopButton);
            });

            return button;
        }
    };

    private final Table.ColumnGenerator DELETE = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            Video video = (Video) entity;

            Button button = components.create(Button.NAME);
            button.setCaption("Delete");
            button.addClickListener(event -> {
                try {
                    loader.removeFile(video.getFileDescriptor());
                    video.getFileDescriptor().setDeleteTs(new Date());
                    dataManager.commit(video.getFileDescriptor());
                    Files.delete(new File(video.getFileDescriptor().getName() + ".mp4").toPath());
                } catch (FileStorageException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                onInit(null);
                videosTable.getAction("remove").actionPerform(videosTable);
            });

            return button;
        }
    };

    private final Table.ColumnGenerator PROCESS = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            Video video = (Video) entity;

            User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();

            Button button = components.create(Button.NAME);
            button.setCaption("Process");
            button.addClickListener(event -> {
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
                confirmScreen.addAfterCloseListener(event1 -> {
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
                        AppUI ui = AppUI.getCurrent();
                        Executor executor = new ConcurrentTaskExecutor();
                        executor.execute(() -> {
                            AppContext.setSecurityContext(context);
                            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                            File file = new File(video.getFileDescriptor().getName());
                            try {
                                FileUtils.copyInputStreamToFile(loader.openStream(video.getFileDescriptor()), file);
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
                            LookupField<String> nodeList = nodeMap.get(video);
                            restTemplate.exchange(nodeList.getValue() + "/file?login=" + user.getLogin() + "&password=" + password.get() + "&cameraId=" + video.getCamera().getId().toString() + "&videoId=" + video.getId() , HttpMethod.POST, requestEntity, String.class);

                            ui.access(() -> {
                                video.setStatus("processing");
                                dataManager.commit(video);
                                System.out.println("done");

                            });

                        });
                    }
                });

            });

            if(!video.getStatus().equals("ready")){
                button.setEnabled(false);
            }

            return button;
        }
    };

    private final Table.ColumnGenerator NODELIST = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            Video video = (Video) entity;

            LookupField<String> nodeList = components.create(LookupField.NAME);
            Map<String, String> nodeListMap = nodes.stream()
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
                    }));
            nodeList.setOptionsMap(nodeListMap);

            nodeMap.put(video, nodeList);

            return nodeList;
        }
    };

    @Subscribe
    public void onInit(InitEvent event){
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        this.nodes = dataManager.loadList(LoadContext.create(Node.class)
                                            .setQuery(LoadContext.createQuery("SELECT n FROM platform_Node n WHERE n.user.id = :user").setParameter("user", user.getId())));
        videosDl.setParameter("deleteTime", null);
        this.nodeMap = new HashMap<>();

    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        videosTable.addGeneratedColumn("watchButton", WATCH);
        videosTable.addGeneratedColumn("deleteButton", DELETE);
        videosTable.addGeneratedColumn("processButton", PROCESS);
        videosTable.addGeneratedColumn("processNode", NODELIST);
    }
}