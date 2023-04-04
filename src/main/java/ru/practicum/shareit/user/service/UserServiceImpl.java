package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.UserMapper.toUser;
import static ru.practicum.shareit.user.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public UserDto addNewUser(UserDto userDto) {
        userEmailValidation(userDto);
        User user = toUser(userDto);
        return toUserDto(userDao.createUser(user));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        if (userDao.userNotExists(userId)) {
            log.debug("User with id {} was not found.", userId);
            throw new ObjectNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        User user = toUser(userDto);
        UserDto userFromStorage = getUserById(userId);

        if (Objects.isNull(userDto.getName())) {
            user.setName(userFromStorage.getName());
        }

        if (Objects.isNull(userDto.getEmail())) {
            user.setEmail(userFromStorage.getEmail());
        } else {
            String email = userFromStorage.getEmail();
            boolean isEmailNotChanged = email.equals(userDto.getEmail());

            if (!isEmailNotChanged) {
                userEmailValidation(userDto);
            }
        }

        user.setId(userId);
        return toUserDto(userDao.updateUser(user, userId));
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userDao.getUserById(userId);

        if (Objects.isNull(user)) {
            log.debug("User with id {} was not found.", userId);
            throw new ObjectNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        return toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userDao.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (userDao.userNotExists(userId)) {
            log.debug("User with id {} was not found.", userId);
            throw new ObjectNotFoundException(String.format("User with id: %s was not found!", userId));
        }

        userDao.deleteUser(userId);
    }

    private void userEmailValidation(UserDto user) {
        if (userDao.getAllEmails().contains(user.getEmail())) {
            log.debug("User email {} already exist.", user.getEmail());
            throw new ConflictException(String.format("User email %s already exist.", user.getEmail()));
        }
    }
}
