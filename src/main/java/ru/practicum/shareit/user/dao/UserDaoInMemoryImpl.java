package ru.practicum.shareit.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserDaoInMemoryImpl implements UserDao {

    private final Map<Long, User> users = new HashMap<>();
    private Long userId = 1L;

    @Override
    public User createUser(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user, Long userId) {
        users.put(userId, user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    @Override
    public boolean userNotExists(Long userId) {
        return !users.containsKey(userId);
    }

    @Override
    public List<String> getAllEmails() {
        return getAllUsers().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    private Long generateId() {
        return userId++;
    }
}
