package ru.practicum.shareit.request.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private final User user1 = User.builder()
            .name("name")
            .email("email@email.com")
            .build();

    private final User user2 = User.builder()
            .name("name2")
            .email("email2@email.com")
            .build();

    private final Item item = Item.builder()
            .name("name")
            .description("description")
            .available(true)
            .owner(user1)
            .build();

    private final Request request1 = Request.builder()
            .items(List.of(item))
            .description("request description")
            .created(LocalDateTime.now())
            .requestor(user1)
            .build();

    private final Request request2 = Request.builder()
            .items(List.of(item))
            .description("request2 description")
            .created(LocalDateTime.now())
            .requestor(user2)
            .build();

    @BeforeEach
    private void init() {
        testEntityManager.persist(user1);
        testEntityManager.persist(user2);
        testEntityManager.persist(item);
        testEntityManager.flush();
        requestRepository.save(request1);
        requestRepository.save(request2);
    }

    @Test
    void findAllByRequestorIdOrderByCreated() {
        List<Request> requests = requestRepository.findAllByRequestorIdOrderByCreated(1L);

        assertEquals(requests.size(), 1);
        assertEquals(requests.get(0).getDescription(), "request description");
    }

    @Test
    void findAllByRequestorIdNot() {
        List<Request> requests = requestRepository.findAllByRequestorIdNot(2L, PageRequest.of(0, 1));

        assertEquals(requests.size(), 1);
        assertEquals(requests.get(0).getDescription(), "request description");
    }
}