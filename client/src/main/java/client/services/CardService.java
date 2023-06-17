package client.services;

import client.FXMLLoader;
import client.Task;
import client.scenes.BoardCtrl;
import client.scenes.EditCardCtrl;
import client.utils.ServerUtils;
import commons.Card;
import commons.Status;
import commons.Subtask;
import commons.Tag;
import commons.TaskList;
import javafx.application.Platform;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class CardService {
    private ServerUtils server;
    private FXMLLoader fxml;
    private EditCardCtrl editCardCtrl;
    private Map<Long, Task> cards;
    private ListService listService;

    /**
     * Initialize card service
     * @param server the instance of server utils
     * @param fxml the instance of the fxml loader
     * @param editCardCtrl the instance of the edit card controller
     */
    @Inject
    public CardService(ServerUtils server, FXMLLoader fxml, EditCardCtrl editCardCtrl) {
        this.server = server;
        this.fxml = fxml;
        this.editCardCtrl = editCardCtrl;
        editCardCtrl.setCardService(this);
        this.cards = new HashMap<>();
    }

    /**
     * Creates a link between the Card and ListService
     * @param ls the instance of ListService
     */
    public void setListService(ListService ls) {
        this.listService = ls;
    }

    /**
     * Creates a new task and loads a card scene
     * @param card a blueprint for the card
     * @param parentBoard the parent board
     * @return the new card element
     */
    public Task initializeCard(Card card, BoardCtrl parentBoard) {
        Task cardElem = new Task(card, parentBoard, this, editCardCtrl);
        cards.put(card.id, cardElem);
        server.registerForMessages("/topic/" + card.id + "/cards/edit", Card.class, q -> {
            Platform.runLater(() -> {
                editCardVisual(q);
                if (editCardCtrl.getCurrentId() == card.id) editCardCtrl.updateBasics(q);
            });
        });
        server.registerForMessages("/topic/" + card.id + "/changeCardColor", Card.class, q -> {
            Platform.runLater(() -> {
                editCardColorVisual(q);
            });
        });

        registerForSubtasks(card);
        registerForTags(card);
        return cardElem;
    }

    private void registerForSubtasks(Card card) {
        server.registerForMessages("/topic/" + card.id + "/addNestedTaskList", Card.class, q -> {
            Platform.runLater(() -> {
                addNestedTaskList(card.id);
            });
        });
        server.registerForMessages("/topic/" + card.id + "/addSubtask", Subtask.class, q -> {
            Platform.runLater(() -> {
                addSubtask(q);
            });
        });
        server.registerForMessages("/topic/" + card.id + "/editSubtask", Subtask.class, q -> {
            Platform.runLater(() -> {
                applySubtaskEdit(q);
            });
        });
        server.registerForMessages("/topic/" + card.id + "/deleteSubtask", Subtask.class, q -> {
            Platform.runLater(() -> {
                removeSubtask(q);
            });
        });
        server.registerForMessages("/topic/" + card.id + "/relocateSubtask", Subtask.class, q -> {
            Platform.runLater(() -> {
                applySubtaskRelocate(q);
            });
        });
    }

    private void registerForTags(Card card) {
        server.registerForMessages("/topic/" + card.id + "/addTagToCard", Tag.class, q -> {
            Platform.runLater(() -> {
                if (editCardCtrl.getCurrentId() == card.id) editCardCtrl.addTag(q);
                //System.out.println("Card: "+card.id+"\n"+q.toString());
            });
        });

        server.registerForMessages("/topic/" + card.id + "/removeTagFromCard", Tag.class, q -> {
            Platform.runLater(() -> {
                if (editCardCtrl.getCurrentId() == card.id) editCardCtrl.removeTag(q);
                //System.out.println("Card: "+card.id+"\n"+q.toString());
            });
        });

        server.registerForMessages("/topic/"+card.id+"/editTagFromCard",Tag.class, q ->{
            Platform.runLater(()->{
                if (editCardCtrl.getCurrentId() == card.id) {
                    editCardCtrl.editTag(q);
                }
                // update the overview and also card Details
                // change the colors
            });
        });
    }

    /**
     * Method for unlinking a Tag to Card
     * @param tag - The tag we want to unlink
     * @param cardId - the Id of the receiving card
     */
    public void removeTagFromCard(Tag tag, Long cardId) {
        server.send("/app/"+cardId+"/removeTagFromCard",tag);
    }

    /**
     * Method for linking a Tag to Card
     * @param tag - The tag we want to link
     * @param cardId - the Id of the receiving card
     */
    public void addTagToCard(Tag tag,Long cardId){
        server.send("/app/"+cardId+"/addTagToCard",tag);
    }

    /**
     * Gets the task controller by id
     * @param id the id of the card
     * @return the Task object that controls the visual element
     */
    public Task getTaskById(Long id) {
        return cards.get(id);
    }

    /**
     * @param card Card that is displayed in the list
     * Makes the card visible and changes the title of it in the list.
     *             Also updates from the database
     */
    public void editCardVisual(Card card) {
        Task task = cards.get(card.id);
        task.applyVisualChanges(card);
    }

    /**
     * Changes the Tasks color visually
     * @param card Reference Card
     */
    public void editCardColorVisual(Card card) {
        Task task = cards.get(card.id);
        task.setColor(card.color);
    }

    /**
     * This method saved a recently edited card to the repository and updates the board for
     * all users using websockets.
     * @param id ID of the card that got edited and will now be saved
     * @param title the new title
     * @param description the new description
     */
    public void saveEditedCard(Long id, String title, String description) {
        Card card = server.getCardById(id);
        card.title = title;
        card.description = description;
        card.id = id;
        server.send("/app/" + id + "/cards/edit", card);
    }

    /**
     * Sends a request to the server to relocate the task which will basically send the changed
     * task to the client to be added again. So this method also deletes the existing task.
     * @param cardId the id of the card
     * @param oldListId the id of the old list
     * @param newListId the id of the list in which the card will be relocated
     * @param newIdx the new index of the card
     */
    public void relocateTask(Long cardId, Long oldListId, Long newListId, int newIdx) {
        Card card = server.getCardById(cardId);
        TaskList list = server.getListById(newListId);
        card.taskList = list;
        card.index = newIdx;
        server.send("/app/" + oldListId + "/cards/relocate", card);
    }

    /**
     * Sends a websocket request to the server to delete a card
     * @param id
     */
    public void deleteTask(Long id) {
        Card card = server.getCardById(id);
        card.status = Status.DELETED;
        card.index = -1;
        server.send("/app/" + card.taskList.id + "/cards/delete", card);
    }

    /**
     * Deregisters for updates related to a card and removes it from the service
     * @param id the id of the card
     */
    public void closeTask(Long id) {
        server.deregisterForMessages("/topic/" + id + "/cards/edit");
        server.deregisterForMessages("/topic/" + id + "/changeCardColor");
        server.deregisterForMessages("/topic/" + id + "/addNestedTaskList");
        server.deregisterForMessages("/topic/" + id + "/addSubtask");
        server.deregisterForMessages("/topic/" + id + "/editSubtask");
        server.deregisterForMessages("/topic/" + id + "/deleteSubtask");
        server.deregisterForMessages("/topic/" + id + "/relocateSubtask");
        server.deregisterForMessages("/topic/" + id + "/addTagToCard");
        server.deregisterForMessages("/topic/" + id + "/removeTagFromCard");
        server.deregisterForMessages("/topic/" + id + "/editTagFromCard");
        if (editCardCtrl.getCurrentId() == id) editCardCtrl.closeEditCardView();
        cards.remove(id);
    }

    /**
     *  This method saved a recently changed color of card to the
     *  repository and updates the board for all users using websockets.
     * @param id Id of the card
     * @param color hex value
     */
    public void changeCardColor(Long id, String color) {
        Card card = server.getCardById(id);
        card.color = color;
        card.id = id;
        server.send("/app/" + id + "/changeCardColor", card);
    }

    /** Enables/disables the drag-and-drop for lists
     * @param i if i=+1 it will be enabled and if i=-1 it will be disabled, otherwise won't change
     */
    public void dragDropEnabled(int i) {
        listService.dragDropEnabled(i);
    }

    /**
     * @return the instance of the fxml loader
     */
    public FXMLLoader getFXML() {
        return fxml;
    }

    /**
     * Send a request to the server to create a nested task list
     * @param cardId the card id in which the nested task list will be created
     */
    public void createNestedTaskList(Long cardId) {
        server.send("/app/" + cardId + "/addNestedTaskList", new Subtask());
    }

    /**
     * Creates a nested task list as instructed by the server
     * @param cardId the card id in which the nested task list will be created
     */
    public void addNestedTaskList(Long cardId) {
        Task task = cards.get(cardId);
        if (editCardCtrl.getCurrentId() == cardId) {
            editCardCtrl.addNestedTaskList();
        }
    }

    /**
     * Send a request to the server to create a subtask
     * @param st the info for creating the subtask
     * @param cardId the card id in which the subtask will be created
     */
    public void createSubtask(Subtask st, Long cardId) {
        server.send("/app/" + cardId + "/addSubtask", st);
    }

    /**
     * Creates a subtask as instructed by the server
     * @param st the new subtask
     */
    public void addSubtask(Subtask st) {
        if (editCardCtrl.getCurrentId() == st.card.id) {
            editCardCtrl.setProgress(st.card.doneSubtasks, st.card.totalSubtasks);
            editCardCtrl.addNestedTaskOnPosition(st, st.index);
        }
    }

    /**
     * Send a request to the server to edit a subtask
     * @param st the info for editing the subtask
     */
    public void editSubtask(Subtask st) {
        server.send("/app/" + st.card.id + "/editSubtask", st);
    }

    /**
     * Updates a subtask as instructed by the server
     * @param st the updated subtask
     */
    public void applySubtaskEdit(Subtask st) {
        if (editCardCtrl.getCurrentId() == st.card.id) {
            editCardCtrl.setProgress(st.card.doneSubtasks, st.card.totalSubtasks);
            editCardCtrl.updateSubtask(st);
        }
    }

    /**
     * Send a request to the server to delete a subtask
     * @param st the subtask to be deleted
     */
    public void deleteSubtask(Subtask st) {
        server.send("/app/" + st.card.id + "/deleteSubtask", st);
    }

    /**
     * Removes a subtask as instructed by the server
     * @param st the deleted subtask
     */
    public void removeSubtask(Subtask st) {
        if (editCardCtrl.getCurrentId() == st.card.id) {
            editCardCtrl.setProgress(st.card.doneSubtasks, st.card.totalSubtasks);
            editCardCtrl.removeSubtask(st);
        }
    }

    /**
     * Send a request to the server to relocate a subtask
     * @param st the subtask to be relocated
     */
    public void relocateSubtask(Subtask st) {
        server.send("/app/" + st.card.id + "/relocateSubtask", st);
    }

    /**
     * Relocates a subtask as instructed by the server
     * @param st the relocated subtask
     */
    public void applySubtaskRelocate(Subtask st) {
        if (editCardCtrl.getCurrentId() == st.card.id) {
            editCardCtrl.relocateSubtask(st);
        }
    }

    /**
     * Gets the assigned tags to a card
     * @param cardId the id of the card
     * @return the list of tags
     */
    public List<Tag> getTagsByCard(Long cardId) {
        return server.getAssignedTagsByCardID(cardId);
    }
}