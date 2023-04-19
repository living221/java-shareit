package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserDao {
    User createUser(User user);

    User updateUser(User user, Long userId);

    User getUserById(Long id);

    List<User> getAllUsers();

    void deleteUser(Long id);

    boolean userNotExists(Long userId);

    List<String> getAllEmails();
}
