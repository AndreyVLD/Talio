package server.database;

import commons.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Checks if there exists already a tag with that name in the given card
     * @param card_id - the id of the given card
     * @param name - the name of the tag
     * @return - true if there exists a tag with the specified name in the given card
     */
    @SuppressWarnings({"checkstyle:MethodName","checkstyle:ParameterName"})
    boolean existsByBoard_IdAndName(Long card_id, String name);


    /**
     * Query for getting all Tags that are not deleted by Board id
     * @param id - the ID of the given Board.
     * @return - A list of Tags
     */
    @Query(value = "SELECT * FROM TAG t WHERE t.board_id = " +
            "?1 AND NOT t.status = 'DELETED';", nativeQuery = true)
    List<Tag> findNotDeletedTagsByBoardId(Long id);

    /**
     * Query for getting all tags in a board that are not deleted or associated with a card
     * @param cardId the id of the card
     * @param boardID the id of the board
     * @return list of all tags that can be added to a card
    */
    @Query(value = "SELECT * FROM TAG t WHERE t.board_id = ?2 AND NOT t.status = 'DELETED' AND" +
        " NOT EXISTS (SELECT * FROM CARD_TAGS ct WHERE ct.card_id = ?1 AND ct.tags_id = t.id);",
        nativeQuery = true)
     List<Tag> findAvailableTagsByCardId(Long cardId, Long boardID);

    /**
     * Query for getting all Tags that are not deleted by Card ID
     * @param id - the ID of the card
     * @return - A list of tags
     */
    @Query(value = "SELECT T.ID, T.COLOR, T.NAME, T.STATUS,T.BOARD_ID FROM TAG AS T " +
            "JOIN CARD_TAGS ON T.ID = CARD_TAGS.TAGS_ID " +
            "WHERE CARD_TAGS.CARD_ID=?1 AND NOT T.STATUS = 'DELETED';",nativeQuery = true)
    List<Tag> findNotDeletedTagsByCardId(Long id);

    /**
     * Querry for finding all Cards that contain a given Tag
     * @param id - The ID of the Given TAG
     * @return - A list of Card IDs
     */
    @Query(value ="SELECT Card.id FROM Card JOIN CARD_TAGS ON Card.ID = CARD_TAGS.CARD_ID " +
            "WHERE CARD_TAGS.TAGS_ID=?1 AND NOT Card.STATUS = 'DELETED'",nativeQuery = true)
    List<Long> findCardIdByTagId(Long id);


}
