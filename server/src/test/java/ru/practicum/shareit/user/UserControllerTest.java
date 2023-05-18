package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @SneakyThrows
    @DisplayName("Тестирование добавления пользователя")
    void createUser_wheUserIsValid() {
        UserDto userDtoToCreate = UserDto.builder()
                .email("email@email.com")
                .name("name")
                .build();

        when(userService.addNewUser(userDtoToCreate)).thenReturn(userDtoToCreate);

        String result = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDtoToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDtoToCreate), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование добавления пользователя c некорректным Email")
    void createUser_wheUserEmailIsNotValid_thenReturnBadRequest() {
        UserDto userDtoToCreate = UserDto.builder()
                .email("email.com")
                .name("name")
                .build();

        when(userService.addNewUser(userDtoToCreate)).thenReturn(userDtoToCreate);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDtoToCreate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addNewUser(userDtoToCreate);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование добавления пользователя c некорректным Name")
    void createUser_wheNameIsNotValid_thenReturnBadRequest() {
        UserDto userDtoToCreate = UserDto.builder()
                .email("email@email.com")
                .name("     ")
                .build();

        when(userService.addNewUser(userDtoToCreate)).thenReturn(userDtoToCreate);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDtoToCreate)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).addNewUser(userDtoToCreate);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование обновления пользователя")
    void updateUser_WhenUserIsValid() {
        Long userId = 0L;
        UserDto userDtoToUpdate = UserDto.builder()
                .email("update@update.com")
                .name("update")
                .build();

        when(userService.updateUser(userDtoToUpdate, userId)).thenReturn(userDtoToUpdate);

        String result = mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDtoToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(userDtoToUpdate), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование обновления пользователя с некорректными полями")
    void updateUser_WhenUserIsNotValid_thenReturnBadRequest() {
        Long userId = 0L;
        UserDto userDtoToUpdate = UserDto.builder()
                .email("update.com")
                .name("    ")
                .build();

        when(userService.updateUser(userDtoToUpdate, userId)).thenReturn(userDtoToUpdate);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDtoToUpdate)))
                .andExpect(status().isBadRequest());


        verify(userService, never()).updateUser(userDtoToUpdate, userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование получения пользователя по Id")
    void get() {
        long userId = 0L;

        mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).getUserById(userId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование получения всех пользоваетлей")
    void getAll() {
        List<UserDto> usersDtoToExpect = List.of(UserDto.builder().name("name").email("email@email.com").build());

        when(userService.getAllUsers()).thenReturn(usersDtoToExpect);

        String result = mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(usersDtoToExpect), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование удаления пользователя по Id")
    void delete() {
        long userId = 0L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(userId);
    }
}
