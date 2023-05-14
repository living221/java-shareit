package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RequestServiceIT {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;

    private final UserDto userDto = UserDto.builder()
            .name("name")
            .email("email@email.com")
            .build();

    private final RequestDto requestDto = RequestDto.builder()
            .id(1L)
            .description("request description")
            .build();

    @Test
    @DisplayName("Интеграционное тестирование добавления запроса")
    void addNewRequest() {
        UserDto addedUser = userService.addNewUser(userDto);
        requestService.addNewRequest(addedUser.getId(), requestDto);

        List<RequestDto> actualRequests = requestService.getUserRequests(addedUser.getId());

        assertEquals(1L, actualRequests.get(0).getId());
        assertEquals("request description", actualRequests.get(0).getDescription());
    }

    @Test
    @DisplayName("Интеграционное тестирование получение запроса по несуществующему Id")
    void getRequestById_whenRequestIdIsNotValid_thenThrowObjectNotFoundException() {
        Long requestId = 2L;

        Assertions
                .assertThrows(RuntimeException.class,
                        () -> requestService.getRequestById(userDto.getId(), requestId));
    }
}
