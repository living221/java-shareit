package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Test
    @DisplayName("Тестирование добавления пользователя")
    void addNewUser_whenInvoked_returnUserDto() {
        User userToSave = new User();
        when(userRepository.save(userToSave)).thenReturn(userToSave);

        UserDto userDtoToSave = new UserDto();
        UserDto actualUserDto = userService.addNewUser(userDtoToSave);

        assertEquals(userDtoToSave, actualUserDto);
        verify(userRepository).save(userToSave);
    }

    @Test
    @DisplayName("Тестирование обновления всех полей пользователя")
    void updateUser_whenEmailAndNamePresent_returnUpdatedUserDTo() {
        Long userId = 0L;
        User oldUser = User.builder().build();
        oldUser.setId(userId);
        oldUser.setEmail("email");
        oldUser.setName("name");

        UserDto newUserDto = UserDto.builder().build();
        newUserDto.setEmail("email1");
        newUserDto.setName("name1");

        User newUser = User.builder().build();
        newUser.setId(userId);
        newUser.setEmail("email1");
        newUser.setName("name1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(newUser));
        when(userRepository.save(newUser)).thenReturn(newUser);

        userService.updateUser(newUserDto, userId);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals("email1", savedUser.getEmail());
        assertEquals("name1", savedUser.getName());
    }

    @Test
    @DisplayName("Тестирование обновления пользователя со старыми полями")
    void updateUser_whenEmailAndNameIsNull_returnOldUserDTo() {
        Long userId = 0L;
        User oldUser = User.builder().build();
        oldUser.setId(userId);
        oldUser.setEmail("email");
        oldUser.setName("name");

        UserDto newUserDto = UserDto.builder().build();
        newUserDto.setEmail(null);
        newUserDto.setName(null);

        User newUser = User.builder().build();
        newUser.setId(userId);
        newUser.setEmail("email");
        newUser.setName("name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(newUser));
        when(userRepository.save(newUser)).thenReturn(newUser);

        userService.updateUser(newUserDto, userId);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertEquals("email", savedUser.getEmail());
        assertEquals("name", savedUser.getName());
    }

    @Test
    @DisplayName("Тестирование получения пользователя по Id")
    void getUserById_whenUserFound_thenReturnUserDto() {
        long userId = 0L;
        User expectedUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        UserDto expectedUserDto = UserMapper.toUserDto(expectedUser);

        UserDto actualUserDto = userService.getUserById(userId);

        assertEquals(expectedUserDto, actualUserDto);
    }

    @Test
    @DisplayName("Тестирование получения пользователя по несуществующему Id")
    void getUserById_whenUserNotFound_thenThrowObjectNotFoundException() {
        long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ObjectNotFoundException userNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> userService.getUserById(userId));

        assertEquals(userNotFoundException.getMessage(), String.format("User with id: %s was not found!", userId));
    }

    @Test
    @DisplayName("Тестирование получения всех пользователей")
    void getAllUsers_whenInvoked_thenReturnListOfUserDto() {
        List<User> expectedUsers = List.of(new User());
        List<UserDto> expectedUserDto = expectedUsers.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());

        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<UserDto> actualUsersDto = userService.getAllUsers();

        assertEquals(actualUsersDto.size(), 1);
        assertEquals(actualUsersDto, expectedUserDto);
    }

    @Test
    @DisplayName("Тестирование удаления пользователя по Id")
    void deleteUser_whenUserExist_userRepositoryDeleteInvoked() {
        long userId = 0L;
        User expectedUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Тестирование удаления пользователя по несуществующему Id")
    void deleteUser_whenUserNotExist_ThrowObjectNotFoundException() {
        long userId = 0L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ObjectNotFoundException userNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> userService.deleteUser(userId));

        assertEquals(userNotFoundException.getMessage(), String.format("User with id: %s was not found!", userId));
    }
}
