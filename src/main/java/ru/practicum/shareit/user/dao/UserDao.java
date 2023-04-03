package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User createUser(User user);

    User updateUser(User user, Long userId);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    void deleteUser(Long id);
}
