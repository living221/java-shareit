package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto addNewRequest(Long userId, RequestDto requestDto);

    List<RequestDto> getUserRequests(Long userId);

    List<RequestDto> getAllRequests(Long userId, Integer from, Integer size);

    RequestDto getRequestById(Long userId, Long requestId);
}
