package com.company.platform.listeners;

import com.company.platform.entity.Camera;
import com.company.platform.service.CameraService;
import com.company.platform.service.StreamService;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.events.EntityChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;
import java.util.UUID;

@Component("platform_CameraChangedListener")
public class CameraChangedListener {
    private static final Logger logger = LoggerFactory.getLogger(CameraChangedListener.class);

    private final CameraService cameraService;

    private final StreamService streamService;

    private final Persistence persistence;

    public CameraChangedListener(CameraService cameraService, StreamService streamService, Persistence persistence) {
        this.cameraService = cameraService;
        this.streamService = streamService;
        this.persistence = persistence;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(EntityChangedEvent<Camera, UUID> event) {
        logger.info("Camera after commit event");
        logger.info(event.getEntityId().toString());

        Camera camera;
        try(Transaction transaction = persistence.createTransaction()){
            EntityManager entityManager = persistence.getEntityManager();
            camera = entityManager.find(Camera.class, event.getEntityId().getValue());
            transaction.commit();
        }

        if(Objects.nonNull(camera)) {
            cameraService.update(camera);
            streamService.update(camera);
        }
    }
}