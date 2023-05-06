package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;

@UtilityClass
public class RequestMapper {
    public static Request toRequest(RequestDto requestDto) {
        return Request.builder()
                .description(requestDto.getDescription())
                .build();
    }

    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

}
