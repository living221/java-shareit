package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {

        return ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .isAvailable(item.getIsAvailable())
                .build();

//        return new ItemDto(
//                item.getName(),
//                item.getDescription(),
//                item.getIsAvailable(),
//                item.getRequest() != null ? item.getRequest().getId() : null
//        );
    }
}
