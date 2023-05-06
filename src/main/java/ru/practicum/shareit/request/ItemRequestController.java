package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.util.Create;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final RequestService requestService;

    @PostMapping
    public RequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @Validated({Create.class}) @RequestBody RequestDto requestDto) {
        return requestService.addNewRequest(userId, requestDto);
    }

    @GetMapping
    public List<RequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<RequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
                                           @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return requestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestDto get(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}
