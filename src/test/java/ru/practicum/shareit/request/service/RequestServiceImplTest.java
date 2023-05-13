package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.request.RequestMapper.toRequestDto;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RequestServiceImpl requestService;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1L)
            .name("item name")
            .description("description")
            .available(true)
            .owner(user)
            .build();

    private final Request request = Request.builder()
            .id(1L)
            .description("request description")
            .items(List.of(item))
            .build();

    @Test
    void addNewRequest() {
        RequestDto expectedRequestDto = toRequestDto(request);
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        RequestDto actualRequestDto = requestService.addNewRequest(user.getId(), expectedRequestDto);

        assertEquals(expectedRequestDto, actualRequestDto);
    }

    @Test
    void getUserRequests() {
        List<RequestDto> expectedRequestsDto = List.of(toRequestDto(request));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findAllByRequestorIdOrderByCreated(userDto.getId())).thenReturn(List.of(request));

        List<RequestDto> actualRequestsDto = requestService.getUserRequests(userDto.getId());

        assertEquals(expectedRequestsDto, actualRequestsDto);
    }

    @Test
    void getAllRequests() {
        List<RequestDto> expectedRequestsDto = List.of(toRequestDto(request));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findAllByRequestorIdNot(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(request));

        List<RequestDto> actualRequestsDto = requestService.getAllRequests(userDto.getId(), 0, 10);

        assertEquals(expectedRequestsDto, actualRequestsDto);
    }

    @Test
    void getRequestById() {
        RequestDto expectedRequestDto = toRequestDto(request);
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        RequestDto actualRequestDto = requestService.getRequestById(userDto.getId(), request.getId());

        assertEquals(expectedRequestDto, actualRequestDto);
    }

    @Test
    void getRequestById_whenRequestIdIsNotValid_thenThrowObjectNotFoundException() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.empty());

        ObjectNotFoundException requestNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> requestService.getRequestById(userDto.getId(), request.getId()));

        assertEquals(requestNotFoundException.getMessage(), String.format("Request with id: %s " +
                "have not been found.", request.getId()));
    }
}