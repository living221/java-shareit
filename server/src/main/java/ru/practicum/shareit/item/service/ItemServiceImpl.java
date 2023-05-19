package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.booking.dao.BookingRepository;
import ru.practicum.shareit.item.booking.model.Booking;
import ru.practicum.shareit.item.booking.model.BookingStatus;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.item.booking.BookingMapper.toBookingItemDto;
import static ru.practicum.shareit.item.comment.CommentMapper.toComment;
import static ru.practicum.shareit.item.comment.CommentMapper.toCommentDto;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

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
        item.setRequestId(itemFromStorage.getRequestId());
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
        itemDto.setComments(getAllComments(itemId));
        if (!item.getOwner().getId().equals(userId)) {
            return itemDto;
        }

        getLastBooking(itemDto);
        getNextBooking(itemDto);

        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Item> items = itemRepository.findByOwner_IdOrderByIdAsc(userId, pageable);

        Map<Item, List<Comment>> comments =
                commentRepository.findByItemIn(items.getContent(), Sort.by(DESC, "created")).stream()
                        .collect(groupingBy(Comment::getItem, toList()));

        Map<Item, List<Booking>> approvedBookings =
                bookingRepository.findAllByItemInAndStatus(items.getContent(), BookingStatus.APPROVED,
                                Sort.by(DESC, "start")).stream()
                        .collect(groupingBy(Booking::getItem, toList()));

        List<ItemDto> results = new ArrayList<>();
        for (Item item : items) {
            ItemDto itemDto = toItemDto(
                    item,
                    approvedBookings.containsKey(item) ? approvedBookings.get(item)
                            .stream().filter(booking -> (booking.getStart().isBefore(LocalDateTime.now())))
                            .max(comparing(Booking::getStart)).orElse(null) : null,
                    approvedBookings.containsKey(item) ? approvedBookings.get(item)
                            .stream().filter(booking -> (booking.getStart().isAfter(LocalDateTime.now())))
                            .min(comparing(Booking::getStart)).orElse(null) : null,
                    comments.getOrDefault(item, Collections.emptyList())
            );
            results.add(itemDto);
        }

        return results;
    }

    @Transactional
    public List<CommentDto> getAllComments(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(Long userId, String text, Integer from, Integer size) {
        userService.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.findAll(pageable).stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, CommentDto commentDto, Long itemId) {
        User user = toUser(userService.getUserById(userId));

        Optional<Item> itemById = itemRepository.findById(itemId);

        if (itemById.isEmpty()) {
            log.debug("User with id {} have not item with id {}.", userId, itemId);
            throw new ObjectNotFoundException(String.format("User with id: %s " +
                    "have not item with id: %s.", userId, itemId));
        }
        Item item = itemById.get();

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            log.debug("User with id {} should have a least one booking of item with id {}.", userId, itemId);
            throw new ValidationException(String.format("User with id %s should have a least one booking " +
                    "of item with id %s.", userId, itemId));
        }

        return toCommentDto(commentRepository.save(toComment(commentDto, item, user)));
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
