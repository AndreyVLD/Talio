package server.service;

import commons.Card;
import commons.Status;
import commons.Tag;
import org.springframework.stereotype.Service;
import server.database.CardRepository;
import server.database.ListRepository;
import server.database.TagRepository;

import java.util.List;

@Service
public class CardService {
    private final CardRepository cards;
    private final ListRepository lists;
    private final TagRepository tags;

    /**
     * Constructs a newly created CardService which will handle the main part of the business logic
     * related to Cards
     * @param cards the repository of cards
     * @param lists the repository of task lists
     * @param tags the repository of tags
     */
    public CardService(CardRepository cards, ListRepository lists, TagRepository tags){
        this.cards = cards;
        this.lists = lists;
        this.tags = tags;
    }

    /**
     * Returns all cards
     * @return a list containing all cards in the repository
     */
    public List<Card> getAllCards() {
        return cards.findAll();
    }

    /**
     * Returns a card by its id
     * @param id the id of the card we are looking for
     * @return the card with the corresponding id or null if there is not such a card
     */
    public List<Card> getCardById(Long id) {
        if (id < 0 || !cards.existsById(id)) {
            return null;
        } else {
            return cards.findAllById(List.of(id));
        }
    }

    /**
     * Returns a tag by its id
     * @param id the id of the tag we are looking for
     * @return the tag with the corresponding id or null if there is not such a card
     */
    public List<Tag> getTagById(Long id) {
        if (id < 0 || !tags.existsById(id)) {
            return null;
        } else {
            return tags.findAllById(List.of(id));
        }
    }

    /**
     * A method which restores a card to a list rearranging the ones currently in it
     * @param card the card to be restored
     * @param newStatus the status after the card is restored
     * @param newIdx the new index of the card in the list or -1 if it is not visible anymore
     */
    private void restoreCardToList(Card card, Status newStatus, Integer newIdx) {
        if (card.index.equals(newIdx) && card.status.equals(newStatus)) return;

        List<Card> listOfCards = cards.findNotDeletedCardsByTaskListId(card.taskList.id);
        for (Card c : listOfCards) {
            if (c.index >= newIdx) {
                c.index++;
                cards.save(c);
            }
        }

        card.index = newIdx;
        card.status = newStatus;
    }

    /**
     * A method which removes a card from a list rearranging the ones currently in it
     * @param card the card to be removed
     */
    private void removeCardFromList(Card card) {
        if (card.index.equals(-1) && card.status.equals(Status.DELETED)) return;

        List<Card> listOfCards = cards.findNotDeletedCardsByTaskListId(card.taskList.id);
        for (Card c : listOfCards) {
            if (c.index > card.index) {
                c.index--;
                cards.save(c);
            }
        }

        card.index = -1;
        card.status = Status.DELETED;
    }

    /**
     * Edit basic attributes of a card with specific id
     * @param info object of class Card with a valid id and only those fields which have to change
     * @return the card with the applied changes or null if the id is invalid
     */
    public Card edit(Card info) {
        Long id = info.id;
        List<Card> list = getCardById(id);
        if (list == null) return null;
        Card card = getCardById(id).get(0);
        if (info.title != null) card.title = info.title;
        if (info.description != null) card.description = info.description;
        if (info.color != null) card.color = info.color;
        return cards.save(card);
    }

    /**
     * Selects all active cards for a given list
     * @param id the id of the task list
     * @param status the status to search for
     * @return List of all active cards inside the list sorted by index or null if invalid
     */
    public List<Card> getAllCardsByTaskListAndStatus(Long id, Status status) {
        if (id < 0 || !lists.existsById(id)) return null;
        return cards.findCardsByTaskListIdAndStatus(id, status.name());
    }

    /**
     * Adds a new card to the end of the list referenced in the field taskList
     * @param card the card to be added
     * @return the added card or null if unsuccessful
     */
    public Card addNewCard(Card card) {
        if (card.taskList.id == null || !lists.existsById(card.taskList.id)) return null;
        Long tid = card.taskList.id;
        card.index = calcNextIndex(tid);
        card.doneSubtasks = 0;
        card.totalSubtasks = -1;
        return cards.save(card);
    }

    /**
     * Adds a tag to a card
     * @param cid the id of the card
     * @param tid the id of the tag
     */
    public void addTagToCard(Long cid, Long tid) {
        if (!tags.existsById(tid)) return;
        Card card = getCardById(cid).get(0);
        if (card == null) return;
        cards.addTagToCard(cid, tid);
    }

    /**
     * Removes a tag from a card
     * @param cid the id of the card
     * @param tid the id of the tag
     */
    public void removeTagFromCard(Long cid, Long tid) {
        if (!tags.existsById(tid)) return;
        Tag tag = getTagById(tid).get(0);
        Card card = getCardById(cid).get(0);
        if (card == null) return;
        cards.removeTagFromCard(cid, tid);
    }

    /**
     * Assigns a card to another list (possibly the same but with different position)
     * @param info object of class Card containing a valid id and new taskList, index and status
     * @return the changes made on the Card object after the change or null if unsuccessful
     */
    public Card assignCardToList(Card info) {
        Long id = info.id;
        Card card = getCardById(id).get(0);

        if (card == null) return null;
        if (info.index == null) { // inferring to add the card to the end
            if (info.status.equals(Status.DELETED)) return null; // contradicting status
            info.index = calcNextIndex(info.taskList.id);
        }
        if (info.status.equals(Status.DELETED) ^ info.index.equals(-1)) return null;

        removeCardFromList(card);
        if (info.taskList != null) card.taskList = info.taskList;

        restoreCardToList(card, info.status, info.index);

        cards.save(card);
        return info;
    }

    /**
     * Calculates the next index to put a card on (at the end of a taskList)
     * @param id The id of the taskList
     * @return the index
     */
    public int calcNextIndex(Long id) {
        return cards.findNotDeletedCardsByTaskListId(id).size();
    }
}
