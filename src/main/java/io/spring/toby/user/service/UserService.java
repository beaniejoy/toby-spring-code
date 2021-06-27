package io.spring.toby.user.service;

import io.spring.toby.user.domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    void add(User user);

    @Transactional(readOnly = true)
    User get(String id);
    @Transactional(readOnly = true)
    List<User> getAll();

    void deleteAll();
    void update(User user);

    void upgradeLevels();
}
