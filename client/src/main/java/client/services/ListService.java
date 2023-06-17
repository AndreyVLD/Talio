package client.services;

import client.BoardList;
import client.FXMLLoader;
import client.Task;
import client.scenes.BoardCtrl;
import client.utils.ConfigUtils;
import client.utils.ServerUtils;
import commons.Card;
import commons.Status;
import commons.TaskList;
import javafx.application.Platform;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ListService {
    private ServerUtils server;
    private CardService cardService;
    private BoardService boardService;

    private FXMLLoader fxml;
    private ConfigUtils config;
    private Map<Long, BoardList> lists;
    private int dragDrop = +1;

    /**
     * Initializes the ListService
     * @param server the instance of the ServerUtils
     * @param cardService the instance of the CardService
     * @param config the instance of the ConfigUtils
     * @param fxml the instance of the FXMLLoader
     */
    @Inject
    public ListService(ServerUtils server, CardService cardService, ConfigUtils config,
                       FXMLLoader fxml) {
        this.server = server;
        this.cardService = cardService;
        this.cardService.setListService(this);
        this.config = config;
        this.lists = new HashMap<>();
        this.fxml = fxml;
    }

    /**
     * Creates a link between the List and BoardService
     * @param bs the instance of BoardService
     */
    public void setBoardService(BoardService bs) {
        this.boardService = bs;
    }

    /**
     * Creates a new list and loads a list scene
     * @param list a blueprint for the list
     * @param parentBoard the parent board
     * @return the new list element
     */
    public BoardList initializeList(TaskList list, BoardCtrl parentBoard) {
        BoardList listElem = new BoardList(list, parentBoard, this);
        lists.put(list.id, listElem);
        retrieveCards(list.id);
        server.registerForMessages("/topic/" + list.id + "/cards/add", Card.class, q -> {
            Platform.runLater(() -> {
                addCard(q);
            });
        });
        server.registerForMessages("/topic/" + list.id + "/cards/relocate", Card.class, q -> {
            Platform.runLater(() -> {
                relocateCard(q, list.id);
            });
        });
        server.registerForMessages("/topic/" + list.id + "/cards/delete", Card.class, q -> {
            Platform.runLater(() -> {
                removeCard(q);
            });
        });

        server.registerForMessages("/topic/" + list.id + "/lists/edit/title", TaskList.class, q -> {
            Platform.runLater(() -> {
                editListTitle(q);
            });
        });
        return listElem;
    }

    /**
     * Edits the title a list
     * @param q the updated list
     */
    public void editListTitle(TaskList q) {
        BoardList bl = lists.get(q.id);
        bl.setTitle(q.name);
    }

    /**
     * Gets the list controller by id
     * @param id the id of the list
     * @return the instance of class BoardList that controls the visual elements of the list
     */
    private BoardList getBoardListById(Long id) {
        return lists.get(id);
    }

    /**
     * Retrieves all cards in the given list and displays them
     * @param listId the id of the list
     */
    public void retrieveCards(Long listId) {
        List<Card> cards = server.getCardsByList(listId);
        for (Card card : cards) {
            addCard(card);
        }
    }

    /**
     * Creates a new card inside a list and sends it to the server
     * @param listId the id of the list
     * @param title title of the list
     */
    public void createNewCard(Long listId, String title) {
        Card card = new Card();
        card.title = title;
        card.status = Status.ACTIVE;
        card.taskList = new TaskList();
        card.taskList.id = listId;
        card.createdBy = config.getUserName();
        server.send("/app/" + listId + "/cards/add", card);
    }

    /**
     * Adds a card to a specified list
     * @param card the card data
     */
    public void addCard(Card card) {
        BoardList list = lists.get(card.taskList.id);
        Task task = cardService.initializeCard(card, list.getBoard());
        list.addTask(task, card.index);
    }

    /**
     * Closes the list and deregister for websocket updates
     * @param listId the list id
     */
    public void closeList(Long listId) {
        server.deregisterForMessages("/topic/" + listId + "/cards/add");
        server.deregisterForMessages("/topic/" + listId + "/cards/relocate");
        server.deregisterForMessages("/topic/" + listId + "/cards/delete");
        server.deregisterForMessages("/topic/" + listId + "/lists/edit/title");
        BoardList list = lists.get(listId);
        for (Task task : list.getTasks()) {
            cardService.closeTask(task.getId());
        }
        lists.remove(list);
    }

    /**
     * Sends a request to the server to delete the list
     * @param id the id of the list
     * @param boardId the id of the board in which the list is located
     */
    public void deleteList(Long id, Long boardId) {
        TaskList tl = new TaskList();
        lists.remove(tl);
        tl.id = id;
        server.deleteListById(tl.id);
        closeList(id);
    }

    /**
     * Sends a websocket request to the server to relocate the list with the corresponding id
     * @param id the id of the list to be relocated
     * @param idx the new idx of the list
     */
    public void relocateList(Long id, int idx) {
        TaskList tl = server.getListById(id);
        tl.index = idx;
        server.send("/app/" + tl.board.id + "/lists/relocate", tl);
    }

    /**
     * Relocates a card on an update sent from the server
     * @param q the updated card object
     * @param oldListId the id of the old list
     */
    public void relocateCard(Card q, Long oldListId) {
        Long newListId = q.taskList.id;
        q.taskList = new TaskList();
        q.taskList.id = oldListId;
        removeCard(q);
        q.taskList.id = newListId;
        addCard(q);
        BoardList bl = lists.get(newListId);
        bl.applyHover();
    }

    /**
     * Removes the card on update from the server and closes it
     * @param card the card to be removed
     */
    public void removeCard(Card card) {
        Task t = cardService.getTaskById(card.id);
        cardService.closeTask(card.id);
        BoardList bl = getBoardListById(card.taskList.id);
        bl.removeTask(t);
    }

    /**
     * Enables/disables or checks the drag-and-drop for lists
     * @param i if i=+1 it will be enabled and if i=-1 it will be disabled, otherwise won't change
     * @return whether the drag-and-drop is currently enabled
     */
    public boolean dragDropEnabled(int i) {
        if (i != 0) dragDrop = i;
        if (dragDrop == +1) return true;
        else return false;
    }

    /**
     * sends to the right endpoint the edited tasklist
     * @param id id of the list
     * @param title title of the list
     */
    public void saveEditedList(Long id, String title) {
        TaskList list = server.getListById(id);
        list.name = title;
        server.send("/app/" + id + "/lists/edit/title", list);
    }

    /**
     * @return the fxml loader
     */
    public FXMLLoader getFXML() {
        return fxml;
    }

}
