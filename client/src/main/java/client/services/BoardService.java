package client.services;

import client.BoardList;
import client.FXMLLoader;
import client.scenes.BoardCtrl;
import client.scenes.MainCtrl;
import client.utils.RecentBoardsUtils;
import client.utils.ServerUtils;
import commons.Board;
import commons.TaskList;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class BoardService {
    private MainCtrl mainCtrl;
    private ServerUtils server;
    private ListService listService;
    private RecentBoardsUtils rbu;
    private FXMLLoader fxml;
    private Map<Long, BoardCtrl> boards;
    private CardService cardService;

    /**
     * Initializes board service
     * @param mainCtrl    the instance of MainCtrl (for password changed popup)
     * @param server      the instance of the ServerUtils
     * @param listService the instance of the ListService
     * @param rbu         the instance of the RecentBoardUtils
     * @param cardService the instance of the CardService
     * @param fxml        the instance of the FXMLLoader
     */
    @Inject
    public BoardService(MainCtrl mainCtrl, ServerUtils server, ListService listService,
                        RecentBoardsUtils rbu, CardService cardService, FXMLLoader fxml) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.listService = listService;
        this.cardService = cardService;
        this.listService.setBoardService(this);
        this.rbu = rbu;
        this.fxml = fxml;
        this.boards = new HashMap<>();
    }

    /**
     * Setter for the map of boards
     * @param boards the map of boards
     */
    public void setBoards(Map<Long, BoardCtrl> boards) {
        this.boards = boards;
    }

    /**
     * Getter for the map of boards
     * @return the map of boards
     */
    public Map<Long, BoardCtrl> getBoards() {
        return boards;
    }

    /**
     * Returns a board by id
     * @param id the id of the board
     * @return the board with the specific id
     */
    public BoardCtrl getBoardById(Long id) {
        return boards.get(id);
    }

    private void displayBoard(Board board, Tab parent) {
        Pair<BoardCtrl, Parent> newBoardScene = fxml.returnBoardScene();
        BoardCtrl boardCtrl = newBoardScene.getKey();
        boards.put(board.id, boardCtrl);
        parent.setContent(newBoardScene.getValue());
        boardCtrl.loadBoard(board, parent);
        retrieveLists(board.id);
        rbu.addBoard(board.id, board.name, null);
        parent.setOnClosed(event -> {
            closeBoard(board.id);
        });
        boardCtrl.getAddListButton().setOnAction(event -> {
            createNewList(board.id);
        });
        server.registerForMessages("/topic/" + board.id + "/lists/add", TaskList.class, q -> {
            Platform.runLater(() -> {
                addList(board.id, q);
            });
        });
        server.registerForLongPolling("/api/lists/" + board.id + "/lists/remove", q -> {
            Platform.runLater(() -> {
                removeList(board.id, ((TaskList)q).id);
            });
        });
        server.registerForMessages("/topic/" + board.id + "/lists/relocate", TaskList.class, q -> {
            Platform.runLater(() -> {
                relocateList(board.id, q);
            });
        });
        server.registerForMessages("/topic/" + board.id + "/changePassword", Board.class, q -> {
            Platform.runLater(() -> {
                if (q.password != null && !q.password.equals(board.password)) {
                    mainCtrl.showPasswordPopup(q.id, parent, true);
                }
            });
        });
        server.registerForMessages("/topic/" + board.id + "/changeName", Board.class, q -> {
            Platform.runLater(() -> {
                parent.setText(q.name);
            });
        });
    }

    /**
     * Creates a new board, loads a board scene and displays it in the parent tab
     * @param board a blueprint of the board to be created
     * @param parent the parent tab
     */
    public void createNewBoard(Board board, Tab parent) {
        Board newBoard = server.createBoard(board);
        displayBoard(newBoard, parent);
    }

    /**
     * Joins an existing board, loads a board scene and displays it in the parent tab
     * @param id the id of the board
     * @param parent the parent tab
     */
    public void joinExistingBoard(Long id, Tab parent) {
        Board board = server.getBoard(id);
        displayBoard(board, parent);
    }

    /**
     * Checks whether the board is currently opened
     * @param id the id of the board
     * @return true if the board is open or false otherwise
     */
    public boolean checkIfOpened(Long id) {
        return boards.containsKey(id);
    }

    /**
     * Retrieves all lists inside a board and displays them
     * @param boardId the id of the board
     */
    public void retrieveLists(Long boardId) {
        List<TaskList> serverLists = server.getNotDeletedListsByBoard(boardId);
        BoardCtrl parentBoard = getBoardById(boardId);
        int idx = 0;
        for (TaskList list : serverLists) {
            BoardList listElem = listService.initializeList(list, parentBoard);
            parentBoard.addListOnPosition(listElem, idx++);
        }
    }

    /**
     * Creates a new list inside a board and sends it to the server
     * @param boardId the id of the board
     */
    public void createNewList(Long boardId) {
        TaskList tl = new TaskList();
        tl.name = "New List";
        server.send("/app/" + boardId + "/lists/add", tl);
    }

    /**
     * Add new list, initializes the scene and displays it inside the board
     * @param boardId the id of the board
     * @param tl the content of the new list
     */
    public void addList(Long boardId, TaskList tl) {
        BoardCtrl board = boards.get(boardId);
        BoardList list = listService.initializeList(tl, board);
        board.addListOnPosition(list, tl.index);
    }

    /**
     * Relocates a given list to its new position
     * @param boardId the id of the board to which the list belongs
     * @param tl the updated TaskList received from the server
     */
    public void relocateList(Long boardId, TaskList tl) {
        BoardCtrl board = boards.get(boardId);
        board.relocateList(tl.id, tl.index);
    }

    /**
     * Remove a list from a board on a received request from the server to remove it
     * @param boardId the id of the board
     * @param listId the id of the list
    */
    public void removeList(Long boardId, Long listId) {
        BoardCtrl board = boards.get(boardId);
        board.removeList(listId);
    }

    /**
     * Deregister for websocket updates to related lists on board close
     * @param boardId the board id
     */
    public void closeBoard(Long boardId) {
        server.deregisterForMessages("/topic/" + boardId + "/lists/add");
        server.deregisterForLongPolling("/api/lists/" + boardId + "/lists/remove");
        server.deregisterForMessages("/topic/" + boardId + "/lists/relocate");
        server.deregisterForMessages("/topic/" + boardId + "/changePassword");
        server.deregisterForMessages("/topic/" + boardId + "/changeName");
        BoardCtrl board = boards.get(boardId);
        var lists = board.getLists();
        for (BoardList list : lists) {
            listService.closeList(list.getId());
        }
        boards.remove(boardId);
    }

    /**
     * Closes all boards
     */
    public void closeAll() {
        for (Long boardId : boards.keySet()) {
            server.deregisterForMessages("/topic/" + boardId + "/lists/add");
            server.deregisterForLongPolling("/api/lists/" + boardId + "/lists/remove");
            server.deregisterForMessages("/topic/" + boardId + "/lists/relocate");
            server.deregisterForMessages("/topic/" + boardId + "/changePassword");
            server.deregisterForMessages("/topic/" + boardId + "/changeName");
            BoardCtrl board = boards.get(boardId);
            var lists = board.getLists();
            for (BoardList list : lists) {
                listService.closeList(list.getId());
            }
        }
        boards.clear();
    }

    /**
     * Returns the password for a board by boardId in plain string format
     * @param boardId - the id of the board to get the password from
     * @return - the password in string format
     */
    public String getPasswordFromBoard(Long boardId) {
        Board board = server.getBoard(boardId);
        if (board == null) return null;
        return board.password;
    }

    /**
     * Gets the board from the server by specified Id
     * @param boardId - the boardId
     * @return - the board or null if not found
     */
    public Board getBoardFromServer(Long boardId) {
        return server.getBoard(boardId);
    }

    /**
     * Method for sending the server a request to change board name
     * @param boardId - the id of the board that needs the name change
     * @param newName - the new name
     */
    public void changeBoardName(Long boardId, String newName) {
        Board board = new Board();
        board.id = boardId;
        board.name = newName;

        server.send("/app/" + boardId + "/changeName", board);
    }

    /**
     * Method for sending the server a request to change board password
     * @param boardId - the id of the board that needs the password change
     * @param newPassword - the new password
     */
    public void changeBoardPassword(Long boardId, String newPassword) {
        Board board = new Board();
        board.id = boardId;
        board.password = newPassword;

        server.send("/app/" + board.id + "/changePassword", board);
    }
}
