package server.service;

import commons.Card;
import commons.Status;
import commons.TaskList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.database.CardRepository;
import server.database.CardRepositoryTest;
import server.database.ListRepository;
import server.database.ListRepositoryTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CardServiceTest {

    private ListRepository listRepo;
    private CardRepository cardsRepo;
    private CardService cardService;

    @BeforeEach
    void setup() {
        listRepo = new ListRepositoryTest();
        cardsRepo = new CardRepositoryTest();
        cardService = new CardService(cardsRepo, listRepo, null);

        TaskList tl1 = listRepo.save(new TaskList("Task list 1", null));
        TaskList tl2 = listRepo.save(new TaskList("Task list 2", null));

        // using cardService to properly add cards and save them to cards rep
        cardService.addNewCard(new Card("Card 1", "-", tl1));
        cardService.addNewCard(new Card("Card 2", "-", tl1));
        cardService.addNewCard(new Card("Card 3", "-", tl1));
        cardService.addNewCard(new Card("Card 4", "-", tl2));
    }

    @Test
    void getAllCards() {
        List<Card> cards = cardService.getAllCards();
        assertEquals(4, cards.size());
        Set<Long> s = new HashSet<>();
        for (int i = 0; i < 4; ++ i) {
            Card c = cards.get(i);
            assertTrue(c.id >= 0 && c.id < 4);
            s.add(c.id);
            assertNotNull(c.taskList);
        }
        assertEquals(4, s.size());
    }

    @Test
    void getCardById() {
        assertEquals(1L, cardService.getCardById(1L).get(0).id);
        assertNull(cardService.getCardById(-1L));
        assertNull(cardService.getCardById(4L));
    }

    @Test
    void edit() {
        Card info = new Card("New title", "New description", null);
        info.id = 1L;
        info.color = "#00FF00";
        cardService.edit(info);
        Card updated = cardService.getCardById(1L).get(0);
        System.out.println(updated);
        assertEquals("New title", updated.title);
        assertEquals("New description", updated.description);
        assertEquals("#00FF00", updated.color);
        assertEquals(1L, updated.id);
        info.id = 5L;
        assertEquals(null, cardService.edit(info));
    }

    @Test
    void getAllActiveCardsByTaskList() {
        assertEquals(3, cardService.getAllCardsByTaskListAndStatus(0L, Status.ACTIVE).size());
        assertEquals(1, cardService.getAllCardsByTaskListAndStatus(1L, Status.ACTIVE).size());
        assertNull(cardService.getAllCardsByTaskListAndStatus(2L, Status.ACTIVE));
    }

    @Test
    void testDeleteCardAction() {
        Card info = new Card(null, null, null);
        info.id = 1L;
        info.status = Status.DELETED;
        info.index = -1;
        cardService.assignCardToList(info);
        List<Card> cards = cardService.getAllCardsByTaskListAndStatus(0L, Status.ACTIVE);
        assertEquals(2, cards.size());
        assertEquals(0L, cards.get(0).id);
        assertEquals(0, cards.get(0).index);
        assertEquals(2L, cards.get(1).id);
        assertEquals(1, cards.get(1).index);
    }

    @Test
    void testMoveCardActionAtEnd() {
        Card info = new Card(null, null, null);
        info.id = 1L;
        info.taskList = listRepo.getById(1L);
        info.status = Status.ACTIVE;
        info.index = null;

        assertNotNull(cardService.assignCardToList(info));

        List<Card> cards1 = cardService.getAllCardsByTaskListAndStatus(0L, Status.ACTIVE);
        assertEquals(2, cards1.size());
        assertEquals(0L, cards1.get(0).id);
        assertEquals(0, cards1.get(0).index);
        assertEquals(2L, cards1.get(1).id);
        assertEquals(1, cards1.get(1).index);

        List<Card> cards2 = cardService.getAllCardsByTaskListAndStatus(1L, Status.ACTIVE);
        assertEquals(2, cards2.size());
        assertEquals(3L, cards2.get(0).id);
        assertEquals(0, cards2.get(0).index);
        assertEquals(1L, cards2.get(1).id);
        assertEquals(1, cards2.get(1).index);
    }

    @Test
    void testMoveCardActionAtIndex() {
        Card info = new Card(null, null, null);
        info.id = 1L;
        info.taskList = listRepo.getById(1L);
        info.status = Status.ACTIVE;
        info.index = 0;

        assertNotNull(cardService.assignCardToList(info));

        List<Card> cards1 = cardService.getAllCardsByTaskListAndStatus(0L, Status.ACTIVE);
        assertEquals(2, cards1.size());
        assertEquals(0L, cards1.get(0).id);
        assertEquals(0, cards1.get(0).index);
        assertEquals(2L, cards1.get(1).id);
        assertEquals(1, cards1.get(1).index);

        List<Card> cards2 = cardService.getAllCardsByTaskListAndStatus(1L, Status.ACTIVE);
        assertEquals(2, cards2.size());
        assertEquals(1L, cards2.get(0).id);
        assertEquals(0, cards2.get(0).index);
        assertEquals(3L, cards2.get(1).id);
        assertEquals(1, cards2.get(1).index);
    }

    @Test
    void addTagToCard() {
    }

    @Test
    void removeTagFromCard() {
    }
}