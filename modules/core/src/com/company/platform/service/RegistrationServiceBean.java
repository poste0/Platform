package com.company.platform.service;

import com.haulmont.cuba.core.entity.contracts.Id;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;
@Service(RegistrationService.NAME)
public class RegistrationServiceBean implements RegistrationService{

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Inject
    private PasswordEncryption passwordEncryption;

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceBean.class);

    private static final String GROUP_ID = "0fa2b1a5-1d68-4d69-9fbd-dff348347f93";

    private static final String ROLE_ID = "cd541dd4-eeb7-cd5b-847e-d32236552fa9";

    private static final String QUERY = "SELECT u from sec$User u WHERE u.loginLowerCase = :login";
    @Override
    public User register(String login, String password, String name) throws IllegalArgumentException {
        if(dataManager.getCount(LoadContext.create(User.class)
                .setQuery(LoadContext.createQuery(QUERY)
                        .setParameter("login", login.toLowerCase()))) >= 1) {
            log.error("This user exists");
            throw new IllegalArgumentException("This user already exists");
        }

        User user = createUser(login, password);
        user.setName(name);

        Group group = dataManager.load(LoadContext.create(Group.class).setId(UUID.fromString(GROUP_ID)));
        Role role = dataManager.load(LoadContext.create(Role.class).setId(UUID.fromString(ROLE_ID)));
        user.setGroup(group);

        UserRole userRole = createUserRole(role, user);

        dataManager.commit(new CommitContext(userRole, user));

        log.info("User has been regstered");

        return user;

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
