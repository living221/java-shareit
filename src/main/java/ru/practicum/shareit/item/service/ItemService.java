package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    Item getItemById(Long userId, Long itemId);

    List<ItemDto> getAllItems(Long userId);

    List<ItemDto> searchItems(Long userId, String text);
}
