package com.company.platform.web.controllers;

import com.company.platform.entity.Video;
import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.FileLoader;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.app.TrustedClientService;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.auth.WebAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/myapi")
public class VideoController {
    private final FileLoader loader;

    private final DataManager dataManager;

    private final Configuration configuration;

    private final TrustedClientService clientService;

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    public VideoController(FileLoader loader, DataManager dataManager, Configuration configuration, TrustedClientService clientService) {
        this.loader = loader;
        this.dataManager = dataManager;
        this.configuration = configuration;
        this.clientService = clientService;
    }

    @RequestMapping(value = "/video", produces = "video/mp4")
    public void getVideo(HttpServletRequest request, HttpServletResponse response, @RequestParam("videoId") String videoId){
        String password = configuration.getConfig(WebAuthConfig.class).getTrustedClientPassword();
        UserSession session = clientService.getSystemSession(password);
        SecurityContext context = new SecurityContext(session);
        AppContext.setSecurityContext(context);

        Video video = dataManager.load(Id.of(UUID.fromString(videoId), Video.class)).view("video-view").one();
        MultiPartFileSender sender = new MultiPartFileSender(video.getFileDescriptor(), loader)
                .with(request)
                .with(response);
        try {
            sender.serveResource();
        } catch (Exception e) {
            try {
                response.getWriter().println(e.getMessage());
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }

    }

    @RequestMapping("/hello")
    public void hello(HttpServletResponse response){
        try {
            response.getWriter().println("hello");
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
