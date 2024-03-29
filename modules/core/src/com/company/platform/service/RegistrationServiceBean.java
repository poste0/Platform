package com.company.platform.service;

import com.company.platform.entity.Camera;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service(RegistrationService.NAME)
public class RegistrationServiceBean implements RegistrationService{

    private final Metadata metadata;

    @javax.inject.Inject
    private final DataManager dataManager;

    private final PasswordEncryption passwordEncryption;

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceBean.class);

    private static final String GROUP_ID = "0fa2b1a5-1d68-4d69-9fbd-dff348347f93";

    private static final String ROLE_ID = "0c018061-b26f-4de2-a5be-dff348347f93";

    private static final String QUERY = "SELECT u from sec$User u WHERE u.loginLowerCase = :login";

    public RegistrationServiceBean(Metadata metadata, DataManager dataManager, PasswordEncryption passwordEncryption) {
        this.metadata = metadata;
        this.dataManager = dataManager;
        this.passwordEncryption = passwordEncryption;
    }

    @Override
    public User register(String login, String password) throws IllegalArgumentException {
        if(dataManager.getCount(LoadContext.create(User.class)
                .setQuery(LoadContext.createQuery(QUERY)
                        .setParameter("login", login.toLowerCase()))) >= 1) {
            log.error("This user exists");
            throw new IllegalArgumentException("This user already exists");
        }

        User user = createUser(login, password);
        user.setName(login);

        Group group = dataManager.load(LoadContext.create(Group.class).setId(UUID.fromString(GROUP_ID)));
        Role role = dataManager.load(LoadContext.create(Role.class).setId(UUID.fromString(ROLE_ID)));
        user.setGroup(group);

        UserRole userRole = createUserRole(role, user);

        Camera camera = createDefaultUserCamera(user);

        dataManager.commit(new CommitContext(userRole, user, camera));

        log.info("User has been registered");

        return user;

    }

    private Camera createDefaultUserCamera(User user) {
        Camera camera = dataManager.create(Camera.class);
        camera.setId(user.getId());
        String userId = user.getId().toString();
        camera.setUrlAddress(userId);
        camera.setAddress(userId);
        camera.setName(userId);
        camera.setWeight(-1);
        camera.setHeight(-1);
        camera.setFrameRate(-1);
        camera.setUser(user);
        return camera;
    }

    private User createUser(String login, String password){
        User user = metadata.create(User.class);
        user.setLogin(login);
        user.setPassword(passwordEncryption.getPasswordHash(user.getId(), password));
        return user;
    }

    private UserRole createUserRole(Role role, User user){
        UserRole userRole = metadata.create(UserRole.class);
        userRole.setRole(role);
        userRole.setUser(user);
        return userRole;
    }
}
