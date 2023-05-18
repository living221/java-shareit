package ru.practicum.shareit.item.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.booking.BookingMapper;
import ru.practicum.shareit.item.booking.dao.BookingRepository;
import ru.practicum.shareit.item.booking.dto.BookingDto;
import ru.practicum.shareit.item.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.booking.model.Booking;
import ru.practicum.shareit.item.booking.model.BookingState;
import ru.practicum.shareit.item.booking.model.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.booking.BookingMapper.toBooking;
import static ru.practicum.shareit.item.booking.BookingMapper.toBookingDtoOut;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDtoOut create(Long userId, BookingDto bookingDto) {
        User user = toUser(userService.getUserById(userId));
        Optional<Item> itemById = itemRepository.findById(bookingDto.getItemId());

        if (itemById.isEmpty()) {
            log.debug("Item with id {} have not found.", bookingDto.getItemId());
            throw new ObjectNotFoundException(String.format("Item with id %s have not found.", bookingDto.getItemId()));
        }
        Item item = itemById.get();

        bookingValidation(bookingDto, user, item);
        Booking booking = toBooking(bookingDto, item, user);

        return BookingMapper.toBookingDtoOut(bookingRepository.save(booking));
    }


    @Override
    @Transactional
    public BookingDtoOut update(Long userId, Long bookingId, Boolean approved) {
        Optional<Booking> bookingById = bookingRepository.findById(bookingId);

        if (bookingById.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Booking with id %s was not found.", bookingId));
        }

        Booking booking = bookingById.get();

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException(String.format("User with id %s is not owner to item with id %s",
                    booking.getItem().getOwner().getId(), booking.getItem().getId()));
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException(String.format("Booking with id %s already changed status",
                    booking.getId()));
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return toBookingDtoOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoOut getById(Long userId, Long bookingId) {
        Optional<Booking> bookingById = bookingRepository.findById(bookingId);

        if (bookingById.isEmpty()) {
            throw new ObjectNotFoundException(String.format("Booking with id %s was not found.", bookingId));
        }

        Booking booking = bookingById.get();

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException(String.format("User with id %s is not owner or booker.", userId));
        }

        return toBookingDtoOut(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoOut> getAllByBooker(Long userId, String bookingState, Integer from, Integer size) {
        userService.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        switch (BookingState.valueOf(bookingState)) {
            case ALL:
                return bookingRepository.findAllByBookerId(userId, pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllCurrentBookingsByBookerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.findAllPastBookingsByBookerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.findAllFutureBookingsByBookerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.findAllWaitingBookingsByBookerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.findAllRejectedBookingsByBookerId(userId, pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoOut> getAllByOwner(Long userId, String bookingState, Integer from, Integer size) {
        userService.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        switch (BookingState.valueOf(bookingState)) {
            case ALL:
                return bookingRepository.findAllByOwnerId(userId, pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository.findAllCurrentBookingsByOwnerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case PAST:
                return bookingRepository.findAllPastBookingsByOwnerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case FUTURE:
                return bookingRepository.findAllFutureBookingsByOwnerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case WAITING:
                return bookingRepository.findAllWaitingBookingsByOwnerId(userId, LocalDateTime.now(), pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());

            case REJECTED:
                return bookingRepository.findAllRejectedBookingsByOwnerId(userId, pageable).stream()
                        .map(BookingMapper::toBookingDtoOut)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        }
    }

    private void bookingValidation(BookingDto bookingDto, User user, Item item) {

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                    String.format("Start date: %s cannot be before current time.",
                            bookingDto.getStart()));
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException(
                    String.format("End date: %s cannot be before start date: %s.",
                            bookingDto.getEnd(), bookingDto.getStart()));
        }
        if (bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException(
                    String.format("End date: %s cannot be equals start date: %s.",
                            bookingDto.getEnd(), bookingDto.getStart()));
        }
        if (!item.getAvailable()) {
            throw new ValidationException(
                    String.format("Item with id %s is not available.",
                            item.getId())
            );
        }
        if (user.getId().equals(item.getOwner().getId())) {
            throw new ObjectNotFoundException(
                    String.format("Item with id %s is not found.",
                            item.getId())
            );
        }
    }
}
