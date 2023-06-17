package server.api;

import commons.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import commons.Card;
import server.service.CardService;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    /**
     * Constructor for the Controller handling the Cards inside a List
     * @param cardService - the service for handling operations on cards
     */
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Request for getting all the Cards
     * @return - a list with all the Lists from the repository
     */

    @GetMapping(path = { "", "/" })
    public List<Card> all(){
        return cardService.getAllCards();
    }

    /**
     * Request for getting a Card by id
     * @param id - the id of the needed Card
     * @return - a Response Entity with the Card
     */

    @GetMapping("/{id}")
    public ResponseEntity<List<Card>> getById(@PathVariable("id") Long id){
        List<Card> card = cardService.getCardById(id);
        if (card == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(card);
        }
    }

    /**
     * Selects the cards in a specified task list matching certain status and returns them in
     * increasing order of their indexes (positions inside the list)
     * @param id The id of a Task List
     * @param status The value the status to be matched
     * @return All active cards within that task list
     */
    @GetMapping("/taskList/{id}/{status}")
    public ResponseEntity<List<Card>> getAllCardsByTaskListIdAndStatus(@PathVariable("id") Long id,
        @PathVariable("status") String status) {
        List<Card> cards = null;
        if (id != null && Status.findByName(status) != null)
            cards = cardService.getAllCardsByTaskListAndStatus(id, Status.findByName(status));
        if (cards == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(cards);
        }
    }

    /**
     * Post request for adding a Card to the end of a specific task list
     * @param card - Card to be added (does not allow for DELETED status and index is overwritten)
     * @return - a response entity with OK method if the adding was successful
     */

    @PostMapping(path = { "", "/" })
    public ResponseEntity<Card> add(@RequestBody Card card) {
        Card newCard = null;
        if (card.id == null && card.title != null && card.taskList != null && card.status != null
            && !card.status.equals(Status.DELETED)) {
            newCard = cardService.addNewCard(card);
        }
        if (newCard == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(newCard);
        }
    }

    /**
     * Patch request for editing an existing card with only the non-null attributes
     * Takes care of change in both simple attributes and change in the related TaskList
     * and in the latter case updates indexes in the old and the new TaskList
     * @param card The new content of the existing card (if index attribute is set, this will be the
     *  index of the card after the update; if it is null and taskList changes, will be added last)
     * @return a response entity with OK status containing the changes made or Bad Request
     */
    @PatchMapping(path = { "", "/"})
    public ResponseEntity<Card> edit(@RequestBody Card card) {
        Card updCard = null;
        if (card.id != null) {
            updCard = cardService.edit(card);
            if (card.taskList != null || card.index != null || card.status != null) {
                updCard = cardService.assignCardToList(card);
            }
        }
        if (updCard == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(updCard);
        }
    }

    /**
     * Card message handler for websockets
     * @param card - Card instance to be added
     * @param listId - ID of list to add card to
     * @return - The Card to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{listId}/cards/add")
    @SendTo("/topic/{listId}/cards/add")
    public Card handleAdd(Card card, @DestinationVariable long listId) {
        var cardResponse = add(card);
        return cardResponse.getBody();
    }

    /**
     * Card patch message handler for websockets
     * @param card - Card instance to be edited
     * @param listId - Not used, only here for correct messagemapping syntax
     * @return - The Card to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{listId}/cards/relocate")
    @SendTo("/topic/{listId}/cards/relocate")
    public Card handleRelocate(Card card, @DestinationVariable long listId) {
        var cardResponse = edit(card);
        return cardResponse.getBody();
    }

    /**
     * Card patch message handler for websockets
     * @param card - Card instance to be edited
     * @param listId - Not used, only here for correct messagemapping syntax
     * @return - The Card to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{listId}/cards/delete")
    @SendTo("/topic/{listId}/cards/delete")
    public Card handleDelete(Card card, @DestinationVariable long listId) {
        var cardResponse = edit(card);
        return cardResponse.getBody();
    }

    /**
     * Card patch message handler for websockets
     * @param card - Card instance to be edited
     * @param cardId - Id of the card to be edited
     * @return - The Card to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{cardId}/cards/edit")
    @SendTo("/topic/{cardId}/cards/edit")
    public Card handleEditCardId(Card card, @DestinationVariable Long cardId) {
        var cardResponse = edit(card);
        return cardResponse.getBody();
    }
    /**
     * Card patch message handler for websockets
     * @param card - Card instance to change the color to
     * @param cardId - Id of the card to change the color to
     * @return - The Card to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{cardId}/changeCardColor")
    @SendTo("/topic/{cardId}/changeCardColor")
    public Card changeCardColor(Card card, @DestinationVariable Long cardId) {
        var cardResponse = edit(card);
        return cardResponse.getBody();
    }

}
