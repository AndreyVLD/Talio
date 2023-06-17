package server.api;

import commons.Subtask;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.service.SubtaskService;

import java.util.List;

@RestController
@RequestMapping("/api/subtasks")
public class SubtaskCtrl {
    private final SubtaskService subtaskService;

    /**
     * Initializes the subtask controller
     * @param subtaskService the subtask service
     */
    public SubtaskCtrl(SubtaskService subtaskService) {
        this.subtaskService = subtaskService;
    }

    /**
     * Request for getting the list of all subtasks by card id
     * @param cardId - the id of the card
     * @return - a Response Entity with the Card
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<List<Subtask>> getById(@PathVariable("cardId") Long cardId){
        List<Subtask> subtasks = subtaskService.getAllByCardId(cardId);
        if (subtasks == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(subtasks);
        }
    }

    /**
     * Message for adding a nested subtask to a card
     * @param card an empty card
     * @param cardId the id of the card
     * @return an empty subtask
     */
    @MessageMapping("/{cardId}/addNestedTaskList")
    @SendTo("/topic/{cardId}/addNestedTaskList")
    @Transactional
    public Subtask addNestedTaskList(Subtask card, @DestinationVariable Long cardId) {
        subtaskService.createSubtaskList(cardId);
        return new Subtask();
    }

    /**
     * Message for creating a new subtask and adding it to the nested task list of a card
     * @param subtask the blueprint for creating the new subtask
     * @param cardId the id of the card
     * @return the created subtask
     */
    @MessageMapping("/{cardId}/addSubtask")
    @SendTo("/topic/{cardId}/addSubtask")
    public Subtask addSubtask(Subtask subtask, @DestinationVariable Long cardId) {
        Subtask st = subtaskService.createSubtask(subtask, cardId);
        st.card.tags = null;
        return st;
    }

    /**
     * Message for editing a subtask
     * @param subtask the info for editing the subtask
     * @param cardId the id of the card
     * @return the edited subtask
     */
    @MessageMapping("/{cardId}/editSubtask")
    @SendTo("/topic/{cardId}/editSubtask")
    public Subtask editSubtask(Subtask subtask, @DestinationVariable Long cardId) {
        Subtask st = subtaskService.editSubtask(subtask, cardId);
        st.card.tags = null;
        return st;
    }

    /**
     * Message for deleting a subtask
     * @param subtask the subtask to be deleted
     * @param cardId the id of the card whose nested task list the subtask belongs to
     * @return the deleted subtask
     */
    @MessageMapping("/{cardId}/deleteSubtask")
    @SendTo("/topic/{cardId}/deleteSubtask")
    public Subtask deleteSubtask(Subtask subtask, @DestinationVariable Long cardId) {
        Subtask st = subtaskService.deleteSubtask(subtask, cardId);
        st.card.tags = null;
        return st;
    }

    /**
     * Message for relocating a subtask
     * @param subtask the subtask to be relocated containing the new index
     * @param cardId the id of the card whose nested task list the subtask belongs to
     * @return the relocated subtask with the updated index
     */
    @MessageMapping("/{cardId}/relocateSubtask")
    @SendTo("/topic/{cardId}/relocateSubtask")
    public Subtask relocateSubtask(Subtask subtask, @DestinationVariable Long cardId) {
        Subtask st = subtaskService.relocateSubtask(subtask, cardId);
        st.card.tags = null;
        return st;
    }
}
