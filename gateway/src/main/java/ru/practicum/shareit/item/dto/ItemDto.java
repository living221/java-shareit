package ru.practicum.shareit.item.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

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

    @NotNull(groups = {Create.class}, message = "Available cannot be null.")
    private Boolean available;

    private BookingItemDto lastBooking;
    private BookingItemDto nextBooking;

    private List<CommentDto> comments;

    private Long requestId;
}
