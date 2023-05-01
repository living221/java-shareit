package ru.practicum.shareit.item.booking.service;

import ru.practicum.shareit.item.booking.dto.BookingDto;
import ru.practicum.shareit.item.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut create(Long userId, BookingDto bookingDto);

    BookingDtoOut update(Long userId, Long bookingId, Boolean approved);

    BookingDtoOut getById(Long userId, Long bookingId);

    List<BookingDtoOut> getAllByBooker(Long userId, String bookingState);

    List<BookingDtoOut> getAllByOwner(Long userId, String bookingState);
}
