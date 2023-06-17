/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import commons.Status;
import commons.TaskList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Card;
import org.springframework.scheduling.config.Task;
import server.database.CardRepositoryTest;
import server.database.ListRepositoryTest;
import server.service.CardService;

import java.util.ArrayList;
import java.util.List;

public class CardControllerTest {
    private CardService cardService;
    private CardController sut;
    private List<Card> sampleCards;
    private List<TaskList> sampleLists;

    @BeforeEach
    public void setup() {
        var cardRepo = new CardRepositoryTest();
        var listRepo = new ListRepositoryTest();
        cardService = new CardService(cardRepo, listRepo, null);
        sut = new CardController(cardService);

        // add sample task lists
        sampleLists = new ArrayList<>();
        TaskList tl1 = new TaskList("List 1", null);
        TaskList tl2 = new TaskList("List 2", null);
        sampleLists.add(listRepo.save(tl1));
        sampleLists.add(listRepo.save(tl2));

        // add sample cards
        sampleCards = new ArrayList<>();
        Card c1 = new Card("Card 1", "-", listRepo.getById(0L));
        Card c2 = new Card("Card 2", "-", listRepo.getById(0L));
        c2.status = Status.DONE;
        Card c3 = new Card("Card 3", "-", listRepo.getById(0L));
        Card c4 = new Card("Card 4", "-", listRepo.getById(1L));

        sampleCards.add(sut.add(c1).getBody());
        sampleCards.add(sut.add(c2).getBody());
        sampleCards.add(sut.add(c3).getBody());
        sampleCards.add(sut.add(c4).getBody());
    }

    @Test
    public void areCardsSaved() {
        List<Card> cards = sut.all();
        for (int i = 0; i < 4; ++ i) {
            assertTrue(cards.contains(sampleCards.get(i)));
            System.out.println(cards.get(i));
        }
    }

    @Test
    public void cannotAddNullCard() {
        var actual = sut.add(getCard(null));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void cannotAddCardWithSetId() {
        Card c = new Card("Card 5", "-", sampleLists.get(1));
        c.id = 5L;
        var res = sut.add(c);
        assertEquals(BAD_REQUEST, res.getStatusCode());
        c.id = null;
        res = sut.add(c);
        assertEquals(OK, res.getStatusCode());
    }

    @Test
    public void cannotAddCardWithNullTitle() {
        Card c = new Card(null, "-", sampleLists.get(0));
        var res = sut.add(c);
        assertEquals(BAD_REQUEST, res.getStatusCode());
        c.title = "Title";
        System.out.println(c);
        res = sut.add(c);
        assertEquals(OK, res.getStatusCode());
    }

    @Test
    public void cannotAddCardWithInvalidTaskList() {
        Card c = new Card("Title", "-", null);
        var res = sut.add(c);
        assertEquals(BAD_REQUEST, res.getStatusCode());
        c.taskList = new TaskList("List 3", null);
        res = sut.add(c);
        assertEquals(BAD_REQUEST, res.getStatusCode());
        c.taskList = sampleLists.get(0);
        res = sut.add(c);
        assertEquals(OK, res.getStatusCode());
    }

    @Test
    public void cannotAddCardWithInvalidStatus() {
        Card c = new Card("Title", "-", sampleLists.get(0));
        c.status = null;
        var res = sut.add(c);
        assertEquals(BAD_REQUEST, res.getStatusCode());
        c.status = Status.DELETED;
        res = sut.add(c);
        assertEquals(BAD_REQUEST, res.getStatusCode());
        c.status = Status.DONE;
        res = sut.add(c);
        assertEquals(OK, res.getStatusCode());
    }

    @Test
    public void getById() {
        assertEquals(sampleCards.get(0), sut.getById(0L).getBody().get(0));
        assertEquals(sampleCards.get(2), sut.getById(2L).getBody().get(0));
    }

    @Test
    public void cannotGetByNonExistentId() {
        Card b = getCard("one");
        sut.add(b);

        var res = sut.getById(10L);
        assertEquals(BAD_REQUEST, res.getStatusCode());
    }

    @Test
    public void cannotGetByUnderZeroId() {
        Card b = getCard("one");
        sut.add(b);

        var res = sut.getById(-1L);
        assertEquals(BAD_REQUEST, res.getStatusCode());
    }

    @Test
    public void getCardsInTaskList0() {
        List<Card> cards = sut.getAllCardsByTaskListIdAndStatus(0L, Status.ACTIVE.name()).getBody();
        System.out.println(cards);
        assertTrue(cards.contains(sampleCards.get(0)));
        assertTrue(!cards.contains(sampleCards.get(1))); // not ACTIVE
        assertTrue(cards.contains(sampleCards.get(2)));
        assertTrue(!cards.contains(sampleCards.get(3))); // not in list 0
    }

    @Test
    public void getCardsInTaskListInvalidId() {
        assertEquals(BAD_REQUEST, sut.getAllCardsByTaskListIdAndStatus(5L, Status.ACTIVE.name()).getStatusCode());
        assertEquals(BAD_REQUEST, sut.getAllCardsByTaskListIdAndStatus(-1L, Status.ACTIVE.name()).getStatusCode());
    }

    @Test
    public void getCardsInTaskListInvalidStatus() {
        assertEquals(BAD_REQUEST, sut.getAllCardsByTaskListIdAndStatus(0L, "ATCIVE").getStatusCode());
    }

    @Test
    public void editSimpleAttributes() {
        Card c = new Card();
        c.id = 0L;
        c.title = "Updated title";
        c.description = "Updated description";
        c.color = "#00FF00";
        sut.edit(c);
        c = sut.getById(0L).getBody().get(0);
        assertEquals(0L, c.id);
        assertEquals("Updated title", c.title);
        assertEquals("Updated description", c.description);
        assertEquals("#00FF00", c.color);
    }

    @Test
    public void editWithInvalidId() {
        Card c = new Card();
        c.id = 5L;
        assertEquals(BAD_REQUEST, sut.edit(c).getStatusCode());
        c.id = -1L;
        assertEquals(BAD_REQUEST, sut.edit(c).getStatusCode());
    }

    @Test
    public void deleteCardAction() {
        Card c = new Card();
        c.id = 0L;
        c.index = -1;
        c.status = Status.DELETED;
        sut.edit(c);
        c = sut.getById(0L).getBody().get(0);
        assertEquals(0L, c.id);
        assertEquals(Status.DELETED, c.status);
        assertEquals(-1, c.index);

        System.out.println(sut.all());

        c = sut.getById(1L).getBody().get(0);
        assertEquals(0, c.index);

        c = sut.getById(2L).getBody().get(0);
        assertEquals(1, c.index);
    }

    private static Card getCard(String x) {
        return new Card(x, x, null);
    }

}
