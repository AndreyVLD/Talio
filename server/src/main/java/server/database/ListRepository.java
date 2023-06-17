package server.database;

import commons.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ListRepository extends JpaRepository<TaskList, Long> {
     /**
      * Finds a List of Tasks By id
      * @param boardId - ID of the board
      * @return - a list containing TaskLists
      */
    @SuppressWarnings("checkstyle:MethodName")
    List<TaskList>findByBoard_Id(long boardId);

    /**
     * Returns all lists by specified status in a board in increasing order of their index
     * @param id id of a board
     * @param status the status which the list should match
     * @return A list of TaskLists belonging to that Board with the corresponding status
     */
    @Query(value = "SELECT * FROM Task_List l WHERE l.board_id = ?1 AND l.status = ?2 "
            + "ORDER BY l.index;", nativeQuery = true)
    List<TaskList> findListsByBoardIdAndStatus(Long id, String status);

    /**
     * Returns all lists by specified status not deleted in a board in increasing order of
     * their index
     * @param id id of a board
     * @return A list of TaskLists belonging to that Board with the not deleted status
     */
    @Query(value = "SELECT * FROM Task_List l WHERE l.board_id = ?1 AND NOT l.status = 'DELETED' "
            + "ORDER BY l.index;", nativeQuery = true)
    List<TaskList> findNotDeletedListsByBoardId(Long id);
}
