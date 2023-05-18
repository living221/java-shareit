package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.request.RequestMapper;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Transactional(readOnly = true)
    public List<RequestDto> getUserRequests(Long userId) {
        userService.getUserById(userId);

        List<Request> requests = requestRepository.findAllByRequestorIdOrderByCreated(userId);

        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);

        List<Request> requests = requestRepository.findAllByRequestorIdNot(userId, PageRequest.of(from, size));

        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDto getRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);

        Optional<Request> requestById = requestRepository.findById(requestId);

        if (requestById.isEmpty()) {
            log.debug("Request with id {} have not been found.", requestId);
            throw new ObjectNotFoundException(String.format("Request with id: %s " +
                    "have not been found.", requestId));
        }

        return toRequestDto(requestById.get());
    }
}
