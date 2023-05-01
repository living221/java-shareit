package ru.practicum.shareit.item.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.booking.dto.BookingDto;
import ru.practicum.shareit.item.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.booking.model.BookingState;
import ru.practicum.shareit.item.booking.service.BookingService;
import ru.practicum.shareit.util.Create;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOut createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @Validated({Create.class}) @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @RequestParam("approved") Boolean approved,
                                @PathVariable("bookingId") Long bookingId) {

        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable("bookingId") Long bookingId) {

        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDtoOut> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @RequestParam(value = "state", defaultValue = "ALL") String bookingState) {
        BookingState state = BookingState.from(bookingState);
        if (Objects.isNull(state)) {
            throw new IllegalArgumentException(String.format("Unknown state: %s", bookingState));
        }

        return bookingService.getAllByBooker(userId, bookingState);
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(value = "state", defaultValue = "ALL") String bookingState) {
        BookingState state = BookingState.from(bookingState);
        if (Objects.isNull(state)) {
            throw new IllegalArgumentException(String.format("Unknown state: %s", bookingState));
        }

        return bookingService.getAllByOwner(userId, bookingState);
    }
}
