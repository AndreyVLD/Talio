package client.services;

import client.BoardList;
import client.FXMLLoader;
import client.Task;
import client.scenes.BoardCtrl;
import client.scenes.MainCtrl;
import client.utils.RecentBoardsUtils;
import client.utils.ServerUtils;
import commons.Board;
import commons.TaskList;
import javafx.scene.control.Tab;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

class BoardServiceTest {
    private MainCtrl mainCtrl;
    private ServerUtils server;
    private ListService listService;
    private RecentBoardsUtils rbu;
    private CardService cardService;
    private FXMLLoader fxml;
    private BoardService sut;


    @BeforeEach
    void setUp() {
        mainCtrl = Mockito.mock(MainCtrl.class);
        server = Mockito.mock(ServerUtils.class);
        listService = Mockito.mock(ListService.class);
        rbu = Mockito.mock(RecentBoardsUtils.class);
        cardService = Mockito.mock(CardService.class);
        fxml = Mockito.mock(FXMLLoader.class);
        sut = new BoardService(mainCtrl, server, listService, rbu, cardService, fxml);
    }

    @Test
    void checkIfOpened() {
        BoardCtrl boardCtrl = Mockito.mock(BoardCtrl.class);
        Map<Long, BoardCtrl> boards = new HashMap<>();
        boards.put(1L, boardCtrl);
        sut.setBoards(boards);

        assertEquals(false, sut.checkIfOpened(0L));
        assertEquals(true, sut.checkIfOpened(1L));
    }

    @Test
    void retrieveLists() {
        BoardCtrl boardCtrl = Mockito.mock(BoardCtrl.class);
        Map<Long, BoardCtrl> boards = new HashMap<>();
        boards.put(1L, boardCtrl);
        sut.setBoards(boards);

        List<TaskList> lists = new ArrayList<>();
        lists.add(new TaskList());
        Mockito.when(server.getNotDeletedListsByBoard(1L)).thenReturn(lists);

        BoardList bl = new BoardList();
        Mockito.when(listService.initializeList(lists.get(0), boardCtrl)).thenReturn(bl);

        sut.retrieveLists(1L);

        Mockito.verify(listService).initializeList(lists.get(0), boardCtrl);
        Mockito.verify(boardCtrl).addListOnPosition(bl, 0);
    }

    @Test
    void createNewList() {
        sut.createNewList(1L);
        Mockito.verify(server).send(eq("/app/" + 1 + "/lists/add"), Mockito.any(TaskList.class));
    }

    @Test
    void addList() {
        BoardCtrl boardCtrl = Mockito.mock(BoardCtrl.class);
        Map<Long, BoardCtrl> boards = new HashMap<>();
        boards.put(1L, boardCtrl);
        sut.setBoards(boards);

        BoardList bl = new BoardList();
        TaskList tl = new TaskList();
        tl.index = 5;
        Mockito.when(listService.initializeList(tl, boardCtrl)).thenReturn(bl);

        sut.addList(1L, tl);

        Mockito.verify(boardCtrl).addListOnPosition(bl, 5);
    }

    @Test
    void relocateList() {
        BoardCtrl boardCtrl = Mockito.mock(BoardCtrl.class);
        Map<Long, BoardCtrl> boards = new HashMap<>();
        boards.put(1L, boardCtrl);
        sut.setBoards(boards);

        TaskList tl = new TaskList();
        tl.id = 5L;
        tl.index = 6;

        sut.relocateList(1L, tl);

        Mockito.verify(boardCtrl).relocateList(tl.id, tl.index);
    }

    @Test
    void removeList() {
        BoardCtrl boardCtrl = Mockito.mock(BoardCtrl.class);
        Map<Long, BoardCtrl> boards = new HashMap<>();
        boards.put(1L, boardCtrl);
        sut.setBoards(boards);

        sut.removeList(1L, 5L);

        Mockito.verify(boardCtrl).removeList(5L);
    }

    @Test
    void closeBoard() {
        BoardCtrl boardCtrl = Mockito.mock(BoardCtrl.class);
        Map<Long, BoardCtrl> boards = new HashMap<>();
        boards.put(1L, boardCtrl);
        sut.setBoards(boards);

        List<BoardList> list = new ArrayList<>();
        BoardList bl = new BoardList();
        bl.setId(5L);
        list.add(bl);
        Mockito.when(boardCtrl.getLists()).thenReturn(list);

        sut.closeBoard(1L);

        Mockito.verify(boardCtrl).getLists();
        Mockito.verify(listService).closeList(5L);
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/lists/add");
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/lists/relocate");
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/changePassword");
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/changeName");
        Mockito.verify(server).deregisterForLongPolling("/api/lists/" + 1 + "/lists/remove");

        assertEquals(0, sut.getBoards().size());
    }

    @Test
    void closeAll() {
        BoardCtrl boardCtrl = Mockito.mock(BoardCtrl.class);
        Map<Long, BoardCtrl> boards = new HashMap<>();
        boards.put(1L, boardCtrl);
        sut.setBoards(boards);

        List<BoardList> list = new ArrayList<>();
        BoardList bl = new BoardList();
        bl.setId(5L);
        list.add(bl);
        Mockito.when(boardCtrl.getLists()).thenReturn(list);

        sut.closeAll();

        Mockito.verify(boardCtrl).getLists();
        Mockito.verify(listService).closeList(5L);
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/lists/add");
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/lists/relocate");
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/changePassword");
        Mockito.verify(server).deregisterForMessages("/topic/" + 1 + "/changeName");
        Mockito.verify(server).deregisterForLongPolling("/api/lists/" + 1 + "/lists/remove");

        assertEquals(0, sut.getBoards().size());
    }

    @Test
    void getPasswordFromBoard() {
        Board b = new Board();
        b.password = "123";
        Mockito.when(server.getBoard(1L)).thenReturn(b);
        Mockito.when(server.getBoard(2L)).thenReturn(null);

        assertEquals("123", sut.getPasswordFromBoard(1L));
        assertEquals(null, sut.getPasswordFromBoard(2L));
    }

    @Test
    void getBoardFromServer() {
        Board b = new Board();
        Mockito.when(server.getBoard(1L)).thenReturn(b);

        assertEquals(b, sut.getBoardFromServer(1L));
    }

    @Test
    void changeBoardName() {
        sut.changeBoardName(1L, "new name");

        ArgumentCaptor<Board> arg = ArgumentCaptor.forClass(Board.class);
        Mockito.verify(server).send(eq("/app/" + 1 + "/changeName"), arg.capture());

        assertEquals(1L, arg.getValue().id);
        assertEquals("new name", arg.getValue().name);
    }

    @Test
    void changeBoardPassword() {
        sut.changeBoardPassword(1L, "new password");

        ArgumentCaptor<Board> arg = ArgumentCaptor.forClass(Board.class);
        Mockito.verify(server).send(eq("/app/" + 1 + "/changePassword"), arg.capture());

        assertEquals(1L, arg.getValue().id);
        assertEquals("new password", arg.getValue().password);
    }
}