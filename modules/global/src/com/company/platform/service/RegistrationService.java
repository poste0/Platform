package com.company.platform.service;

import com.haulmont.cuba.security.entity.User;

public interface RegistrationService {
    String NAME = "platform_RegistrationService";

    User register(String login, String password) throws IllegalArgumentException;
}
