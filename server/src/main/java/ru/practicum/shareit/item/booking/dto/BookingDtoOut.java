package ru.practicum.shareit.item.booking.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDtoOut {
    public static final String DATE_FORMAT = "yyyy-MM-ddTHH:mm:ss";
    private Long id;

    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDateTime start;

    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDateTime end;

    private BookingStatus status;

    private User booker;

    private Item item;
}
