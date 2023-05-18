package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto createUser(@Validated({Create.class}) @RequestBody UserDto userDto) {
        return userService.addNewUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@Validated({Update.class}) @RequestBody UserDto userDto,
                          @PathVariable Long userId) {
        return userService.updateUser(userDto, userId);
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
