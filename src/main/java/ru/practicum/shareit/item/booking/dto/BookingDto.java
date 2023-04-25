package ru.practicum.shareit.item.booking.dto;

import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.util.Create;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
public class BookingDto {
    @NotNull(groups = {Create.class}, message = "itemId cannot be empty.")
    Long itemId;

    @NotNull(groups = {Create.class})
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    LocalDateTime start;

    @NotNull(groups = {Create.class})
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    LocalDateTime end;
}
