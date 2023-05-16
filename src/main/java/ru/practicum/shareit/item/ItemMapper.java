package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.booking.model.Booking;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.booking.BookingMapper.toBookingItemDto;

@UtilityClass
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public static ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<Comment> comments) {

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .lastBooking(!Objects.isNull(lastBooking) ? toBookingItemDto(lastBooking) : null)
                .nextBooking(!Objects.isNull(nextBooking) ? toBookingItemDto(nextBooking) : null)
                .comments(!comments.isEmpty() ? comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .requestId(itemDto.getRequestId())
                .build();
    }
}
