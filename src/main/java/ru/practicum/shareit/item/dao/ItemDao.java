package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDao {
    List<Item> getAllItems(Long userId);

    Item createItem(Item item);

    Optional<Item> getItemById(Long userId, Long itemId);

    List<Item> searchItems(String text);

    Item updateItem(Item item);
}
