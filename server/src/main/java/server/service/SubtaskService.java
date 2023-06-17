package server.service;

import commons.Card;
import commons.Status;
import commons.Subtask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.database.CardRepository;
import server.database.SubtaskRepository;

import java.util.List;

@Service
public class SubtaskService {
    private final CardRepository cards;
    private final SubtaskRepository subtasks;

    /**
     * Initializes the SubtaskService which offers the business logic of the subtask functionality
     * @param cards the repository containing all cards
     * @param subtasks the repository containing all subtasks
     */
    public SubtaskService(CardRepository cards, SubtaskRepository subtasks) {
        this.cards = cards;
        this.subtasks = subtasks;
    }

    /**
     * Gets a card by id
     * @param id the id of the card
     * @return the specified card or null if such does not exist
     */
    public Card getCardById(Long id) {
        var res = cards.findById(id);
        if (res.isPresent()) return res.get();
        else return null;
    }

    /**
     * Gets a subtask by id
     * @param id the id of the subtask
     * @return the specified subtask or null if such does not exist
     */
    public Subtask getSubtaskById(Long id) {
        var res = subtasks.findById(id);
        if (res.isPresent()) return res.get();
        else return null;
    }

    /**
     * Gets all not deleted subtasks by card id
     * @param cardId the id of the card
     * @return the specified list or null if the operation was not successful
     */
    public List<Subtask> getAllByCardId(Long cardId) {
        if (!cards.existsById(cardId)) return null;
        return subtasks.findNotDeletedSubtasksByCardId(cardId);
    }

    /**
     * Creates a subtask list for a given card
     * @param cardId the id of the card
     * @return the updated card or null if the operation was not successful
     */
    public Card createSubtaskList(Long cardId) {
        if (!cards.existsById(cardId)) return null;
        Card card = getCardById(cardId);
        if (card.totalSubtasks != -1) return null;
        card.totalSubtasks = 0;
        return cards.save(card);
    }

    /**
     * Creates a new subtask and updates the progress of the corresponding card
     * @param subtask the subtask blueprint
     * @param cardId the card id in which the subtask should be added
     * @return the newly created subtask or null if the operation was not successful
     */
    @Transactional
    public Subtask createSubtask(Subtask subtask, Long cardId) {
        if (!cards.existsById(cardId)) return null;
        Card card = getCardById(cardId);
        if (card.totalSubtasks == -1) return null;
        subtask.card = getCardById(cardId);
        subtask.status = Status.PLANNED;
        subtask.index = card.totalSubtasks++;
        subtask.card = cards.save(card);
        return subtasks.save(subtask);
    }

    /**
     * Edits the title or status of a subtask
     * @param info a subtask object containing the id and the new info
     * @param cardId the id of the card to which the subtask belongs
     * @return the updated subtask or null if the operation was not successful
     */
    @Transactional
    public Subtask editSubtask(Subtask info, Long cardId) {
        if (!subtasks.existsById(info.id) || !cards.existsById(cardId)) return null;
        Card card = getCardById(cardId);
        Subtask subtask = getSubtaskById(info.id);

        if (info.status.equals(Status.PLANNED) && subtask.status.equals(Status.DONE)) {
            card.doneSubtasks--;
            subtask.status = Status.PLANNED;
        }
        if (info.status.equals(Status.DONE) && subtask.status.equals(Status.PLANNED)) {
            card.doneSubtasks++;
            subtask.status = Status.DONE;
        }

        if (info.title != null) subtask.title = info.title;

        subtask.card = cards.save(card);
        return subtasks.save(subtask);
    }

    /**
     * Deletes a subtask
     * @param info a subtask object containing the id and the previous status
     * @param cardId the id of the card to which the subtask belongs
     * @return the deleted subtask or null if the operation was not successful
     */
    @Transactional
    public Subtask deleteSubtask(Subtask info, Long cardId) {
        if (!subtasks.existsById(info.id) || !cards.existsById(cardId)) return null;
        Card card = getCardById(cardId);
        Subtask subtask = getSubtaskById(info.id);

        if (subtask.index == -1) return null; // the subtask is already deleted
        if (info.status.equals(Status.DONE)) card.doneSubtasks--;
        card.totalSubtasks--;

        reorderSubtasks(card.id, subtask.index, -1);
        subtask.index = -1;
        subtask.status = Status.DELETED;

        subtask.card = cards.save(card);
        return subtasks.save(subtask);
    }

    /**
     * Relocates a subtask
     * @param info a subtask object containing the id and the new index
     * @param cardId the id of the card to which the subtask belongs
     * @return the deleted subtask or null if the operation was not successful
     */
    @Transactional
    public Subtask relocateSubtask(Subtask info, Long cardId) {
        if (!subtasks.existsById(info.id) || !cards.existsById(cardId)) return null;
        Card card = getCardById(cardId);
        Subtask subtask = getSubtaskById(info.id);

        if (subtask.index == -1) return null; // the subtask is already deleted
        if (info.index < 0 || info.index >= card.totalSubtasks) return null; // invalid index

        reorderSubtasks(card.id, subtask.index, info.index);
        subtask.index = info.index;

        return subtasks.save(subtask);
    }

    /**
     * Reorders the subtasks and update their indexes
     * @param cardId the index of the card to which the nested task list belongs
     * @param prevIdx the previous index of the subtask
     * @param newIdx the updated index of the subtask
     */
    private void reorderSubtasks(Long cardId, Integer prevIdx, Integer newIdx) {
        List<Subtask> stList = subtasks.findNotDeletedSubtasksByCardId(cardId);
        for (Subtask st : stList) {
            if (newIdx == -1) {
                if (st.index > prevIdx) st.index--;
            }
            else if (newIdx > prevIdx && st.index > prevIdx && st.index <= newIdx) st.index--;
            else if (newIdx < prevIdx && st.index < prevIdx && st.index >= newIdx) st.index++;
            else continue;
            subtasks.save(st);
        }
    }
}
