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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import commons.Board;
import server.database.BoardRepositoryTest;
import server.database.ListRepositoryTest;
import server.service.ListService;

import java.util.ArrayList;
import java.util.List;

public class BoardControllerTest {
    private BoardRepositoryTest repo;
    private BoardController sut;

    @BeforeEach
    public void setup() {
        repo = new BoardRepositoryTest();

        sut = new BoardController(repo, new ListService(new ListRepositoryTest(), repo));
    }

    @Test
    public void isDatabaseUsed() {
        sut.add(getBoard("one"));
        assertTrue(repo.calledMethods.contains("save"));
    }

    @Test
    public void isBoardSaved() {
        Board b = getBoard("one");
        sut.add(b);

        assertEquals(b, repo.getById(0L));
    }

    @Test
    public void cannotAddNullBoard() {
        var actual = sut.add(getBoard(null));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void cannotAddUnderZeroId() {
        Board b = getBoard("one");
        b.id = -1L;
        var res = sut.add(b);

        assertEquals(BAD_REQUEST, res.getStatusCode());
    }

    @Test
    public void getById() {
        Board b = getBoard("one");
        sut.add(b);

        assertEquals(b, sut.getById(0L).getBody());
    }

    @Test
    public void cannotGetByNonExistentId() {
        Board b = getBoard("one");
        sut.add(b);

        var res = sut.getById(10L);
        assertEquals(BAD_REQUEST, res.getStatusCode());
    }

    @Test
    public void cannotGetByUnderZeroId() {
        Board b = getBoard("one");
        sut.add(b);

        var res = sut.getById(-1L);
        assertEquals(BAD_REQUEST, res.getStatusCode());
    }

    @Test
    public void getNoBoards() {
        var res = sut.all();

        assertEquals(res, new ArrayList<Board>());
    }

    @Test
    public void getAllBoards() {
        Board b1 = getBoard("one");
        Board b2 = getBoard("two");
        sut.add(b1);
        sut.add(b2);

        List<Board> blist = new ArrayList<>();
        blist.add(b1);
        blist.add(b2);

        var res = sut.all();
        assertEquals(res, blist);
    }

    private static Board getBoard(String name) {
        return new Board(name);
    }
}
