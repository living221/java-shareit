package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        Item item = toItem(itemDto);

        User user = toUser(userService.getUserById(userId));
        item.setOwner(user);

        return toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item itemFromStorage = getItemById(userId, itemId);

        if (!itemFromStorage.getOwner().getId().equals(userId)) {
            log.debug("User with id {} is not owner of item with id {}.", userId, itemId);
            throw new ForbiddenException(String.format("User with id %s " +
                    "is not owner of item with id %s.", userId, itemId));
        }

        Item item = toItem(itemDto);

        if (Objects.isNull(item.getName())) {
            item.setName(itemFromStorage.getName());
        }

        if (Objects.isNull(item.getDescription())) {
            item.setDescription(itemFromStorage.getDescription());
        }

        if (Objects.isNull(item.getAvailable())) {
            item.setAvailable(itemFromStorage.getAvailable());
        }
        item.setOwner(itemFromStorage.getOwner());
        item.setRequest(itemFromStorage.getRequest());
        item.setId(itemFromStorage.getId());

        return toItemDto(itemRepository.save(item));
    }

    @Override
    public Item getItemById(Long userId, Long itemId) {
        userService.getUserById(userId);

        Optional<Item> itemById = itemRepository.findById(itemId);

        if (itemById.isEmpty()) {
            log.debug("User with id {} have not item with id {}.", userId, itemId);
            throw new ObjectNotFoundException(String.format("User with id: %s " +
                    "have not item with id: %s.", userId, itemId));
        } else {
            return itemById.get();
        }
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        userService.getUserById(userId);

        return itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(Long userId, String text) {
        userService.getUserById(userId);

        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
