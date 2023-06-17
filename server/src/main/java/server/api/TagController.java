package server.api;

import commons.Card;
import commons.Status;
import commons.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import server.database.BoardRepository;
import server.database.TagRepository;
import server.service.CardService;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tags")

public class TagController {
    private final TagRepository repo;
    private final CardService cardService;

    private SimpMessagingTemplate template;
    private final BoardRepository boards;

    /**
     * Constructor for the Repository of Tags
     * @param repo  - TagRepository
     * @param boardRepository - the Board Repository
     * @param cardService - Instance of the Card Service
     * @param template - SimpMessagingTemplate object for updating tags across the board
     */
    public TagController(TagRepository repo, CardService cardService,
                         BoardRepository boardRepository, SimpMessagingTemplate template) {
        this.repo = repo;
        this.cardService = cardService;
        this.boards = boardRepository;
        this.template = template;
    }

    /**
     * Get request for all tags
     * @return - a list containing all tags
     */

    @GetMapping(path = { "", "/" })
    public List<Tag> getAll() {
        return repo.findAll();
    }

    /**
     * Get request for a tag with a certain id
     * @param id - the id of the wanted tag
     * @return - a ResponseEntity having the wanted tag
     */

    @GetMapping(path = "/{id}")
    public ResponseEntity<Tag> getById(@PathVariable("id") long id){
        if (id<0 || !repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repo.findById(id).get());
    }

    /**
     * Adds a given tag to the board with the id
     * @param tag - the tag itself
     * @return - a response entity with OK if the addition was successful
     */
    @PostMapping(path = {"","/"})
    public ResponseEntity<Tag> add(@RequestBody Tag tag){
        Tag newTag = null;
        if (tag.id == null && tag.name != null && tag.board != null && tag.status != null
                && !tag.status.equals(Status.DELETED) && boards.existsById(tag.board.id)) {
            newTag= repo.save(tag);
        }
        if (newTag == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(newTag);
        }
    }

    /**
     * GET Endpoint for getting Tags from a given card
     * @param cardId - the ID of the card we want to get tags from
     * @return - A Response Entity with a list of tags from the given card
     */
    @GetMapping(path ={"/card/{cardId}/assigned"})
    public ResponseEntity<List<Tag>> getTagsByCard(@PathVariable("cardId") Long cardId){
        if(cardService.getCardById(cardId) == null)
            return ResponseEntity.badRequest().build();
        else
            return ResponseEntity.ok(repo.findNotDeletedTagsByCardId(cardId));
    }

    /**
     * GET Endpoint for getting available tags for a given card
     * @param cardId - the ID of the card we want to get tags for
     * @return - A Response Entity with a list of available tags for the given card
     */
    @GetMapping(path ={"/card/{cardId}/available"})
    public ResponseEntity<List<Tag>> getAvailableTagsByCard(@PathVariable("cardId") Long cardId){
        List<Card> card = cardService.getCardById(cardId);
        if(card == null)
            return ResponseEntity.badRequest().build();
        else {
            Long boardId = card.get(0).taskList.board.id;
            return ResponseEntity.ok(repo.findAvailableTagsByCardId(cardId, boardId));
        }
    }

    /**
     * GET Endpoint for adding a Tag to Card
     * @param cardId - the ID of the Card
     * @param tagId - the ID of the Tag
     * @return - A response Entity with the Tag which was added
     */
    @GetMapping(path ={"/card/{cardId}/{tagId}"})
    @Transactional
    public ResponseEntity<Tag> addTagToCard(@PathVariable("cardId") Long cardId,
                                            @PathVariable("tagId") Long tagId){
        if(cardService.getCardById(cardId)==null || !repo.existsById(tagId))
            return ResponseEntity.badRequest().build();
        else {
            cardService.addTagToCard(cardId,tagId);
            return ResponseEntity.ok(repo.findById(tagId).get());
        }
    }

    /**
     * Delete mapping from removing a Tag from a Card
     * @param cardId - the ID of the Card
     * @param tagId - the ID of the Tag
     * @return - A response Entity with the Tag which was deleted
     */
    @DeleteMapping(path={"/delete/{cardId}/{tagId}"})
    @Transactional
    public ResponseEntity<Tag> removeTagFromCard(@PathVariable("cardId") Long cardId,
                                                 @PathVariable("tagId") Long tagId){
        if(cardService.getCardById(cardId)==null || !repo.existsById(tagId) )
            return ResponseEntity.badRequest().build();
        else {
            cardService.removeTagFromCard(cardId,tagId);
            return ResponseEntity.ok(repo.findById(tagId).get());
        }
    }

    /**
     * GET Endpoint for getting all Tags from a Given Board
     * @param id - the ID of the board
     * @return - A Response Entity with the list of tags
     */
    @GetMapping(path ={"/board/{id}"})
    public ResponseEntity<List<Tag>> getTagsByBoard(@PathVariable("id") Long id){
        if(id == null || id < 0 || !boards.existsById(id))
            return ResponseEntity.badRequest().build();
        else
            return ResponseEntity.ok(repo.findNotDeletedTagsByBoardId(id));
    }

    /**
     * DELETE Endpoint for removing a Tag directly from the DB
     * @param id - the ID of the card we want to delete
     * @return - A response Entity with the deleted Tag
     */
    @DeleteMapping(path={"/{id}"})
    @Transactional
    public ResponseEntity<Tag> deleteById(@PathVariable("id") Long id){
        Optional<Tag> tagOptional = repo.findById(id);
        Tag tag = null;
        if(tagOptional.isPresent())
            tag = tagOptional.get();
        if(tag == null)
            return ResponseEntity.badRequest().build();
        tag.status=Status.DELETED;
        repo.save(tag);

        List<Long> cardIds = repo.findCardIdByTagId(tag.id);
        for(Long i:cardIds){
            this.template.convertAndSend("/topic/"+i+"/removeTagFromCard",tag);
            this.template.convertAndSend("/topic/"+i+"/cards/edit",
                cardService.getCardById(i).get(0));
        }
        return ResponseEntity.ok().build();
    }

    /**
     * POST Mapping for updating a given Tag
     * @param id - The ID of the Tag we want to update
     * @param updatedTag - the tag containing the Updated Values
     * @return - A response entity with the updated tag
     */
    @PostMapping("/{id}")
    @Transactional
    public ResponseEntity<Tag> updateTag(@PathVariable Long id, @RequestBody Tag updatedTag) {
        Optional<Tag> tagOptional = repo.findById(id);
        if (tagOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tag tag = tagOptional.get();
        tag.name = updatedTag.name;
        tag.color = updatedTag.color;
        Tag savedTag =repo.save(tag);

        List<Long> cardIds = repo.findCardIdByTagId(id);
        for(Long i:cardIds){
            this.template.convertAndSend("/topic/"+i+"/editTagFromCard",tag);
            this.template.convertAndSend("/topic/"+i+"/cards/edit",
                cardService.getCardById(i).get(0));
        }

        return ResponseEntity.ok(savedTag);
    }

    /**
     * Websocket handler for adding a Tag to a given Card
     * @param tag - the Tag we want to add to a card
     * @param cardId - the ID of the card where we want to add the Tag
     * @return - the Tag which was added
     */
    @MessageMapping("/{cardId}/addTagToCard")
    @SendTo("/topic/{cardId}/addTagToCard")
    public Tag handleAddTagToCard(Tag tag,@DestinationVariable Long cardId){
        var cardResponse = addTagToCard(cardId,tag.id);
        return cardResponse.getBody();
    }

    /**
     * Websocket handler for removing a Tag from a given Card
     * @param tag - the Tag we want to remove
     * @param cardId - the ID of the card from where we want to remove
     * @return - the Tag which was removed
     */
    @MessageMapping("/{cardId}/removeTagFromCard")
    @SendTo("/topic/{cardId}/removeTagFromCard")
    public Tag handleRemoveTagFromCard(Tag tag,@DestinationVariable Long cardId){
        var cardResponse = removeTagFromCard(cardId, tag.id);
        return cardResponse.getBody();
    }
}