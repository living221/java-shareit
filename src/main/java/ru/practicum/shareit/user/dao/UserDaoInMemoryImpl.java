package ru.practicum.shareit.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDaoInMemoryImpl implements UserDao {

    private final Map<Long, User> users = new HashMap<>();
    private Long userId;

    @Override
    public User createUser(User user) {
        if (!users.containsKey(user.getId())) {
            user.setId(generateId());
            users.put(user.getId(), user);
            return user;
        }
        return null;
    }

    private Long generateId() {
        return userId++;
    }

    @Override
    public User updateUser(User user, Long userId) {
        return null;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<User> getAllUsers() {
        return null;
    }

    @Override
    public void deleteUser(Long id) {

    }
}
