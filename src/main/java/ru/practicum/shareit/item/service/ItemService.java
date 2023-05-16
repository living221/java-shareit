package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> getAllItems(Long userId, Integer from, Integer size);

    List<ItemDto> searchItems(Long userId, String text, Integer from, Integer size);

    CommentDto createComment(Long userId, CommentDto commentDto, Long itemId);
}
