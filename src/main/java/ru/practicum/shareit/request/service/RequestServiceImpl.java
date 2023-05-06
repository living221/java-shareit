package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static ru.practicum.shareit.request.RequestMapper.toRequest;
import static ru.practicum.shareit.request.RequestMapper.toRequestDto;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;

    @Override
    @Transactional
    public RequestDto addNewRequest(Long userId, RequestDto requestDto) {
        Request request = toRequest(requestDto);
        User user = toUser(userService.getUserById(userId));

        request.setRequestor(user);

        return toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId) {
        return null;
    }

    @Override
    public List<RequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        return null;
    }

    @Override
    public RequestDto getRequestById(Long userId, Long requestId) {
        return null;
    }
}
