package server.database;

import commons.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    /**
     * @param id Id of a Task List
     * @return A list of cards belonging to that Task List
     */
    List<Card> findCardsByTaskListId(Long id);

    /**
     * Returns all active cards in a specified task list in increasing order of their index
     * @param id Id of a Task List
     * @param status the status which the card should match
     * @return A list of cards belonging to that Task List with the corresponding status
     */
    @Query(value = "SELECT * FROM Card c WHERE c.task_List_id = ?1 AND c.status = ?2 "
            + "ORDER BY c.index;", nativeQuery = true)
    List<Card> findCardsByTaskListIdAndStatus(Long id, String status);

    /**
     * Returns all not deleted cards in a specified task list in increasing order of their index
     * @param id - Id of a Task List
     * @return - A list of cards belonging to that Task List with status different than DELETED
     */
    @Query(value = "SELECT * FROM Card c WHERE c.task_List_id = ?1 AND NOT c.status = 'DELETED' "
            + "ORDER BY c.index;", nativeQuery = true)
    List<Card> findNotDeletedCardsByTaskListId(Long id);

    /**
     * Inserts a record into the relationship table between card and tag
     * @param cid the id of the card
     * @param tid the id of the tag
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO CARD_TAGS VALUES (?1, ?2);", nativeQuery = true)
    void addTagToCard(Long cid, Long tid);

    /**
     * Deletes a record from the relationship table between card and tag
     * @param cid the id of the card
     * @param tid the id of the tag
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM CARD_TAGS WHERE CARD_ID = ?1 AND TAGS_ID = ?2 ;",
            nativeQuery = true)
    void removeTagFromCard(Long cid, Long tid);
}