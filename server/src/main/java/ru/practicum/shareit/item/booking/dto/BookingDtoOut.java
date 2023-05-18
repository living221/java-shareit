package ru.practicum.shareit.item.booking.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.Create;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDtoOut {
    public static final String DATE_FORMAT = "yyyy-MM-ddTHH:mm:ss";
    private Long id;

    @NotNull(groups = {Create.class}, message = "itemId cannot be empty.")
    @FutureOrPresent
    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDateTime start;

    @NotNull(groups = {Create.class})
    @Future
    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDateTime end;

    private BookingStatus status;

    private User booker;

    private Item item;
}
