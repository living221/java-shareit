package ru.practicum.shareit.item.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long itemId;

    private LocalDateTime start;

    private LocalDateTime end;
}
