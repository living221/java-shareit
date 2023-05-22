package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {
    public static Request toRequest(RequestDto requestDto) {
        return Request.builder()
                .id(requestDto.getId())
                .description(requestDto.getDescription())

                .build();
    }

    public static RequestDto toRequestDto(Request request) {

        List<ItemDto> itemsDto = new ArrayList<>();

        if (!Objects.isNull(request.getItems())) {
            itemsDto = request.getItems().stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }

        return RequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemsDto)
                .build();
    }
}
