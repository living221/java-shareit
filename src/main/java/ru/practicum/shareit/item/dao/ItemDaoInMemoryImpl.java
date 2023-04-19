package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemDaoInMemoryImpl implements ItemDao {

    private final Map<Long, List<Item>> items = new HashMap<>();
    private Long itemId = 1L;

    @Override
    public List<Item> getAllItems(Long userId) {

        if (!items.containsKey(userId)) {
            return Collections.emptyList();
        }

        return items.get(userId);
    }

    @Override
    public Item createItem(Item item) {

        item.setId(generateId());

        items.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>()).add(item);

        return item;
    }

    @Override
    public Optional<Item> getItemById(Long userId, Long itemId) {
        return items.values().stream().flatMap(Collection::stream).filter(i -> i.getId().equals(itemId)).findAny();
    }

    @Override
    public List<Item> searchItems(String text) {
        return items.values().stream()
                .flatMap(Collection::stream)
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public Item updateItem(Item item) {

        List<Item> userItems = items.get(item.getOwner().getId());
        List<Item> toRemove = new ArrayList<>();
        for (Item userItem : userItems) {
            if (userItem.getId().equals(item.getId())) {
                toRemove.add(userItem);
            }
        }
        userItems.removeAll(toRemove);
        userItems.add(item);

        return item;
    }

    private Long generateId() {
        return itemId++;
    }
}
