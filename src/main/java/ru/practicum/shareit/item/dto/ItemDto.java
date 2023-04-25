package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.booking.dto.BookingItemDto;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;

    @NotBlank(groups = {Create.class}, message = "Name cannot be empty")
    @Size(groups = {Create.class, Update.class}, min = 1, message = "Name cannot be empty")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Description cannot be empty")
    @Size(groups = {Create.class, Update.class}, min = 1, message = "Description cannot be empty")
    private String description;

    @NotNull(groups = {Create.class})
    private Boolean available;
    private BookingItemDto lastBooking;
    private BookingItemDto nextBooking;
}
