package ru.practicum.shareit.item.booking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.booking.dao.BookingRepository;
import ru.practicum.shareit.item.booking.dto.BookingDto;
import ru.practicum.shareit.item.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.booking.model.Booking;
import ru.practicum.shareit.item.booking.model.BookingStatus;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.item.booking.BookingMapper.toBooking;
import static ru.practicum.shareit.item.booking.BookingMapper.toBookingDtoOut;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final User owner = User.builder()
            .id(2L)
            .name("username2")
            .email("email2@email.com")
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
            .owner(owner)
            .build();

    private final Booking booking = Booking.builder()
            .id(1L)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .status(BookingStatus.APPROVED)
            .item(item)
            .booker(user)
            .build();

    private final Booking bookingWaiting = Booking.builder()
            .id(1L)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .status(BookingStatus.WAITING)
            .item(item)
            .booker(user)
            .build();

    private final BookingDto bookingDto = BookingDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .build();

    private final BookingDto bookingDtoStartBeforeNow = BookingDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().minusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .build();

    private final BookingDto bookingDtoEndBeforeStart = BookingDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().minusDays(1L))
            .build();

    private final BookingDto bookingDtoEndEqualsStart = BookingDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusMinutes(1L))
            .end(LocalDateTime.now().plusMinutes(1L))
            .build();

    @Test
    @DisplayName("Тестирование добавления бронирования")
    void create() {
        BookingDtoOut expectedBookingDtoOut = toBookingDtoOut(toBooking(bookingDto, item, user));
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(toBooking(bookingDto, item, user));

        BookingDtoOut actualBookingDtoOut = bookingService.create(userDto.getId(), bookingDto);

        assertEquals(expectedBookingDtoOut, actualBookingDtoOut);
    }

    @Test
    @DisplayName("Тестирование добавления бронирования с датой начала до текущей")
    void create_whenStartIsBeforeNow_thenThrowValidationException() {
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ValidationException bookingValidationException = assertThrows(ValidationException.class,
                () -> bookingService.create(userDto.getId(), bookingDtoStartBeforeNow));

        assertEquals(bookingValidationException.getMessage(), String.format("Start date: %s cannot be before current time.",
                bookingDtoStartBeforeNow.getStart()));
    }

    @Test
    @DisplayName("Тестирование добавления бронирования с датой конца до даты начала")
    void create_whenEndIsBeforeStart_thenThrowValidationException() {
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ValidationException bookingValidationException = assertThrows(ValidationException.class,
                () -> bookingService.create(userDto.getId(), bookingDtoEndBeforeStart));

        assertEquals(bookingValidationException.getMessage(), String.format("End date: %s cannot be before start date: %s.",
                bookingDtoEndBeforeStart.getEnd(), bookingDtoEndBeforeStart.getStart()));
    }

    @Test
    @DisplayName("Тестирование добавления бронирования с недоступной вещью")
    void create_whenItemIsNotAvailable_thenThrowValidationException() {
        item.setAvailable(false);
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ValidationException bookingValidationException = assertThrows(ValidationException.class,
                () -> bookingService.create(userDto.getId(), bookingDto));

        assertEquals(bookingValidationException.getMessage(), String.format("Item with id %s is not available.",
                item.getId()));
    }

    @Test
    @DisplayName("Тестирование добавления бронирования с совпадающим пользователем и владельцем вещи")
    void create_whenItemOwnerEqualsBooker_thenThrowValidationException() {
        item.setOwner(user);
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        ObjectNotFoundException bookingNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.create(userDto.getId(), bookingDto));

        assertEquals(bookingNotFoundException.getMessage(), String.format("Item with id %s is not found.",
                item.getId()));
    }

    @Test
    @DisplayName("Тестирование одобрения бронирования")
    void update() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingWaiting));
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingWaiting);

        BookingDtoOut actualBookingDtoOut = bookingService.update(owner.getId(), bookingWaiting.getId(), true);

        assertEquals(BookingStatus.APPROVED, actualBookingDtoOut.getStatus());
    }

    @Test
    @DisplayName("Тестирование запрета бронирования")
    void update_whenStatusNotApproved() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingWaiting));
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingWaiting);

        BookingDtoOut actualBookingDtoOut = bookingService.update(owner.getId(), bookingWaiting.getId(), false);

        assertEquals(BookingStatus.REJECTED, actualBookingDtoOut.getStatus());
    }

    @Test
    @DisplayName("Тестирование одобрения бронирования когда статус не WAITING")
    void update_whenStatusNotWaiting() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ValidationException bookingValidationException = assertThrows(ValidationException.class,
                () -> bookingService.update(owner.getId(), booking.getId(), false));

        assertEquals(bookingValidationException.getMessage(), String.format("Booking with id %s already changed status",
                booking.getId()));
    }

    @Test
    @DisplayName("Тестирование одобрения бронирования не владельцем вещи")
    void update_whenUserIsNotItemOwner_thenThrowNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ObjectNotFoundException bookingNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.update(userDto.getId(), booking.getId(), true));

        assertEquals(bookingNotFoundException.getMessage(), String.format("User with id %s is not owner to item with id %s",
                booking.getItem().getOwner().getId(), booking.getItem().getId()));
    }

    @Test
    @DisplayName("Тестирование получения бронирования по Id")
    void getById() {
        BookingDtoOut expectedBookingDtoOut = toBookingDtoOut(booking);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDtoOut actualBookingDtoOut = bookingService.getById(user.getId(), booking.getId());

        assertEquals(expectedBookingDtoOut, actualBookingDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения бронирования по несуществующему Id")
    void getById_whenBookingIdIsNotValid_thenThrowObjectNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        ObjectNotFoundException bookingNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getById(1L, booking.getId()));

        assertEquals(bookingNotFoundException.getMessage(), String.format("Booking with id %s was not found.", booking.getId()));
    }

    @Test
    @DisplayName("Тестирование получения бронирования не владельцем")
    void getById_whenUserIsNotItemOwner_thenThrowObjectNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ObjectNotFoundException bookingNotFoundException = assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getById(3L, booking.getId()));

        assertEquals(bookingNotFoundException.getMessage(), String.format("User with id %s is not owner or booker.", 3L));
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований")
    void getAllByBooker_whenBookingStateAll() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllByBookerId(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByBooker(user.getId(), "ALL", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований со статусом CURRENT")
    void getAllByBooker_whenBookingStateCURRENT() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllCurrentBookingsByBookerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByBooker(user.getId(), "CURRENT", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований со статусом PAST")
    void getAllByBooker_whenBookingStatePAST() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllPastBookingsByBookerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByBooker(user.getId(), "PAST", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований со статусом FUTURE")
    void getAllByBooker_whenBookingStateFUTURE() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllFutureBookingsByBookerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByBooker(user.getId(), "FUTURE", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований со статусом WAITING")
    void getAllByBooker_whenBookingStateWAITING() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllWaitingBookingsByBookerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByBooker(user.getId(), "WAITING", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований со статусом REJECTED")
    void getAllByBooker_whenBookingStateREJECTED() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllRejectedBookingsByBookerId(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByBooker(user.getId(), "REJECTED", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований с несуществующим статусом")
    void getAllByBooker_whenBookingStateIsNotValid_thenThrowIllegalArgumentException() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByBooker(user.getId(), "ERROR", 0, 10));
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований владельцем")
    void getAllByOwner_whenBookingStateAll() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByOwner(user.getId(), "ALL", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований владельцем со статусом CURRENT")
    void getAllByOwner_whenBookingStateCURRENT() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllCurrentBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByOwner(user.getId(), "CURRENT", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований владельцем со статусом PAST")
    void getAllByOwner_whenBookingStatePAST() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllPastBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByOwner(user.getId(), "PAST", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований владельцем со статусом FUTURE")
    void getAllByOwner_whenBookingStateFUTURE() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllFutureBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByOwner(user.getId(), "FUTURE", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований владельцем со статусом WAITING")
    void getAllByOwner_whenBookingStateWAITING() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllWaitingBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByOwner(user.getId(), "WAITING", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований владельцем со статусом REJECTED")
    void getAllByOwner_whenBookingStateREJECTED() {
        List<BookingDtoOut> expectedBookingsDtoOut = List.of(toBookingDtoOut(booking));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(bookingRepository.findAllRejectedBookingsByOwnerId(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingDtoOut> actualBookingsDtoOut = bookingService.getAllByOwner(user.getId(), "REJECTED", 0, 10);

        assertEquals(expectedBookingsDtoOut, actualBookingsDtoOut);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований владельцем со несуществующим статусом")
    void getAllByOwner_whenBookingStateIsNotValid_thenThrowIllegalArgumentException() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllByBooker(user.getId(), "ERROR", 0, 10));
    }
}
