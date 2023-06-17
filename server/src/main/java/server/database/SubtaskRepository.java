package server.database;

import commons.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface SubtaskRepository extends JpaRepository<Subtask,Long> {

    /**
     * Returns all not deleted subtasks in a nested task list in increasing order of their index
     * @param id - id of the card that contains the nested task list
     * @return - A list of subtasks belonging to that nested task list which are not deleted
     */
    @Query(value = "SELECT * FROM Subtask st WHERE st.card_id = ?1 AND NOT st.status = 'DELETED' "
        + "ORDER BY st.index;", nativeQuery = true)
    List<Subtask> findNotDeletedSubtasksByCardId(Long id);

}
