package io.spring.toby.user.service;

import io.spring.toby.user.domain.User;

public interface UserService {
    void add(User user);
    void upgradeLevels();
}
