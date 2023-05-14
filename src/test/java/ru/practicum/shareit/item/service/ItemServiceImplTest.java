package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.booking.dao.BookingRepository;
import ru.practicum.shareit.item.booking.model.Booking;
import ru.practicum.shareit.item.booking.model.BookingStatus;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.item.comment.CommentMapper.toCommentDto;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1L)
            .name("item name")
            .description("description")
            .available(true)
            .owner(user)
            .build();

    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("item name")
            .description("description")
            .available(true)
            .comments(Collections.emptyList())
            .build();

    private final Comment comment = Comment.builder()
            .id(1L)
            .text("comment")
            .created(LocalDateTime.now())
            .author(user)
            .build();

    private final Booking booking = Booking.builder()
            .id(1L)
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().minusDays(1L))
            .end(LocalDateTime.now().plusDays(1L))
            .build();

    @Test
    @DisplayName("Тестирование добавления вещи")
    void addNewItem_whenInvoked_returnItemDto() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(itemRepository.save(item)).thenReturn(item);

        ItemDto actualItemDto = itemService.addNewItem(userDto.getId(), itemDto);

        assertEquals(actualItemDto.getId(), 1L);
        assertEquals(actualItemDto.getName(), "item name");
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    @DisplayName("Тестирование обновления вещи")
    void updateItem() {
        Item updatedItem = Item.builder()
                .id(1L)
                .name("updated name")
                .description("updated description")
                .available(false)
                .owner(user)
                .requestId(1L)
                .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(updatedItem));
        when(itemRepository.save(updatedItem)).thenReturn(updatedItem);

        itemService.updateItem(user.getId(), itemDto.getId(), ItemMapper.toItemDto(updatedItem));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item savedItem = itemArgumentCaptor.getValue();

        assertEquals("updated name", savedItem.getName());
        assertEquals("updated description", savedItem.getDescription());
    }

    @Test
    @DisplayName("Тестирование получения вещи по Id")
    void getItemById() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemDto actualItemDto = itemService.getItemById(user.getId(), item.getId());

        assertEquals(itemDto, actualItemDto);
    }

    @Test
    @DisplayName("Тестирование добавления вещи по несуществующему Id")
    void getItemById_whenItemIdIsInvalid_thenThrowNotFoundException() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        ObjectNotFoundException itemNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> itemService.getItemById(user.getId(), itemDto.getId()));

        assertEquals(itemNotFoundException.getMessage(), String.format("User with id: %s " +
                "have not item with id: %s.", user.getId(), item.getId()));
    }

    @Test
    @DisplayName("Тестирование получения всех вещей")
    void getAllItems() {
        List<ItemDto> expectedItemsDto = List.of(itemDto);

        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(user.getId(), PageRequest.of(0, 10)))
                .thenReturn(List.of(item));
        when(itemService.getAllComments(item.getId())).thenReturn(Collections.emptyList());

        List<ItemDto> actualItemsDto = itemService.getAllItems(user.getId(), 0, 10);

        assertEquals(actualItemsDto.size(), 1);
        assertEquals(actualItemsDto, expectedItemsDto);
    }

    @Test
    @DisplayName("Тестирование получения всех комментариев")
    void getAllComments() {
        List<CommentDto> expectedCommentsDto = List.of(toCommentDto(comment));
        when(commentRepository.findAllByItemId(item.getId())).thenReturn(List.of(comment));

        List<CommentDto> actualComments = itemService.getAllComments(item.getId());

        assertEquals(actualComments.size(), 1);
        assertEquals(actualComments, expectedCommentsDto);
    }

    @Test
    @DisplayName("Тестирование поиска вещи")
    void searchItems() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        Page<Item> items = new PageImpl<>(List.of(item));
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(items);

        List<ItemDto> actualItemsDto = itemService.searchItems(user.getId(), "item", 0, 10);

        assertEquals(1, actualItemsDto.size());
        assertEquals(1, actualItemsDto.get(0).getId());
        assertEquals("item name", actualItemsDto.get(0).getName());
    }

    @Test
    @DisplayName("Тестирование добавления комментария")
    void createComment() {
        CommentDto expectedCommentDto = toCommentDto(comment);
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByUserBookings(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto actualCommentDto = itemService.createComment(user.getId(), toCommentDto(comment), item.getId());

        assertEquals(expectedCommentDto, actualCommentDto);
    }
}
