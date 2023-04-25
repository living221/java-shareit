package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.booking.dao.BookingRepository;
import ru.practicum.shareit.item.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.item.booking.BookingMapper.toBookingItemDto;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        Item item = toItem(itemDto);

        User user = toUser(userService.getUserById(userId));
        item.setOwner(user);

        return toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
//        Item itemFromStorage = toItem(getItemById(userId, itemId));
        Optional<Item> itemById = itemRepository.findById(itemId);

        if (itemById.isEmpty()) {
            throw new ObjectNotFoundException(
                    String.format("Item with id %s was not found.", itemId));
        }
        Item itemFromStorage = itemById.get();

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
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long userId, Long itemId) {
        userService.getUserById(userId);

        Optional<Item> itemById = itemRepository.findById(itemId);

        if (itemById.isEmpty()) {
            log.debug("User with id {} have not item with id {}.", userId, itemId);
            throw new ObjectNotFoundException(String.format("User with id: %s " +
                    "have not item with id: %s.", userId, itemId));
        }
        Item item = itemById.get();
        ItemDto itemDto = toItemDto(item);

        if (!item.getOwner().getId().equals(userId)) {
            return itemDto;
        }

        getLastBooking(itemDto);
        getNextBooking(itemDto);
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems(Long userId) {
        userService.getUserById(userId);

        List<ItemDto> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        items.forEach(i -> {
            getLastBooking(i);
            getNextBooking(i);
        });

        return items;
    }

    @Override
    @Transactional(readOnly = true)
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

    private void getLastBooking(ItemDto itemDto) {
        Optional<Booking> lastBooking = bookingRepository.getLastBooking(itemDto.getId(), LocalDateTime.now());
        if (lastBooking.isPresent()) {
            itemDto.setLastBooking(toBookingItemDto(lastBooking.get()));
        } else {
            itemDto.setLastBooking(null);
        }
    }

    private void getNextBooking(ItemDto itemDto) {
        Optional<Booking> nextBooking = bookingRepository.getNextBooking(itemDto.getId(), LocalDateTime.now());
        if (nextBooking.isPresent()) {
            itemDto.setNextBooking(toBookingItemDto(nextBooking.get()));
        } else {
            itemDto.setNextBooking(null);
        }
    }
}
