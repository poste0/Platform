package com.company.platform.web.screens.videoNew;

import com.company.platform.entity.Node;
import com.company.platform.service.NodeService;
import com.company.platform.web.screens.ConfirmScreen;
import com.company.platform.web.screens.videoplayer.VideoPlayerUtils;
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
import com.vaadin.server.*;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.communication.URLReference;
import com.vaadin.ui.Dependency;
import com.vaadin.ui.Layout;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.vaadin.gwtav.ContentLengthConnectorResource;
import org.vaadin.gwtav.GwtVideo;
import org.vaadin.gwtav.IOUtil;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private Map<Video, LookupField<Node>> nodeMap;

    @Inject
    private NodeService nodeService;

    private static final Logger log = LoggerFactory.getLogger(VideoBrowse.class);

    private final Table.ColumnGenerator WATCH = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            Video video = (Video) entity;

            Button button = components.create(Button.NAME);
            button.setCaption("Watch");
            button.addClickListener(event -> {
                layout = playerBox.unwrap(Layout.class);
                VideoPlayerUtils.renderVideoPlayer(loader, layout, video);
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
                    log.info("Video {} has been deleted", video.getName());
                } catch (FileStorageException | IOException e) {
                    log.error("Video {} has not been deleted", video.getName());
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
                        log.error("Login or password are empty");
                        throw new AuthenticationCredentialsNotFoundException("login and password are not confirmed");
                    }

                    PasswordEncryption encryption = AppBeans.get(PasswordEncryption.NAME);

                    if(encryption.checkPassword(user, password.get()) && login.get().equals(user.getLogin())){
                        log.info("User is confirmed");
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
                                log.info("File {} has been copied", video.getFileDescriptor().getName());
                            } catch (IOException | FileStorageException e) {
                                log.error("File {} has not been copied", video.getFileDescriptor().getName());
                                e.printStackTrace();
                            }
                            FileSystemResource value = new FileSystemResource(file);
                            map.add("file", value);
                            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                            factory.setBufferRequestBody(false);
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
                            RestTemplate restTemplate = new RestTemplate(factory);
                            LookupField<Node> nodeList = nodeMap.get(video);
                            Node node = nodeList.getValue();
                            restTemplate.exchange(node.getAddress() + "/file?login=" + user.getLogin() + "&password=" + password.get() + "&cameraId=" + video.getCamera().getId().toString() + "&videoId=" + video.getId() + "&nodeId=" + node.getId(), HttpMethod.POST, requestEntity, String.class);

                            ui.access(() -> {
                                video.setStatus("processing");
                                dataManager.commit(video);
                                log.info("Video {} is processed", video.getName());

                            });

                        });
                    }
                });

            });

            if(!video.getStatus().equals("ready") && !video.getStatus().equals("error")){
                log.info("Video is not ready");
                button.setEnabled(false);
            }

            return button;
        }
    };

    private final Table.ColumnGenerator NODELIST = new Table.ColumnGenerator() {
        @Override
        public Component generateCell(Entity entity) {
            Video video = (Video) entity;

            LookupField<Node> nodeList = components.create(LookupField.NAME);
            Map<String, Node> nodeListMap = nodes.stream()
                    .filter(node -> {
                        return nodeService.getStatus(node).equals("false");
                    })
                    .collect(Collectors.toMap(new Function<Node, String>() {
                        @Override
                        public String apply(Node node) {
                            return node.getName();
                        }
                    }, new Function<Node, Node>() {
                        @Override
                        public Node apply(Node node) {
                            return node;
                        }
                    }));

            log.info("All nodes {}", nodes);
            log.info("Free nodes {}", nodeListMap);

            nodeList.setOptionsMap(nodeListMap);

            nodeMap.put(video, nodeList);

            return nodeList;
        }
    };

    @Subscribe
    public void onInit(InitEvent event){
        log.info("On init event has started");

        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        this.nodes = dataManager.loadList(LoadContext.create(Node.class)
                                            .setQuery(LoadContext.createQuery("SELECT n FROM platform_Node n WHERE n.user.id = :user").setParameter("user", user.getId())));
        videosDl.setParameter("deleteTime", null);
        this.nodeMap = new HashMap<>();

        log.info("On inti event has stopped");
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event){
        log.info("On after show event has started");

        videosTable.addGeneratedColumn("watchButton", WATCH);
        videosTable.addGeneratedColumn("deleteButton", DELETE);
        videosTable.addGeneratedColumn("processButton", PROCESS);
        videosTable.addGeneratedColumn("processNode", NODELIST);

        log.info("On after show event has finished");
    }


}