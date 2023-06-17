package server.service;

import commons.Board;
import server.database.BoardRepository;
import server.database.ListRepository;
import commons.Status;
import commons.TaskList;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ListService {
    private final ListRepository lists;
    private final BoardRepository boards;

    /**
     * Constructs a newly created ListService which will handle the main part of the logic
     * related to TaskLists
     * @param lists the repository of task lists
     * @param boards the repository of boards
     */
    public ListService(ListRepository lists, BoardRepository boards) {
        this.lists = lists;
        this.boards = boards;
    }

    /**
     * Wrapper method for returning all lists ignoring boardId
     * @return - a list of all TaskLists
     */
    public List<TaskList> findAll() {
        return lists.findAll();
    }

    /**
     * Get tasklist by id
     * @param id - the id of the tasklist to get
     * @return - the tasklist object
     */
    public TaskList getById(Long id) {
        if (id == null || !lists.existsById(id)) {
            return null;
        }
        return lists.findById(id).get();
    }

    /**
     * Gets all lists by a certain boardId
     * @param boardId - the boardId to get the lists from
     * @return - A list of TaskLists from the specified board
     */
    public List<TaskList> getListsByBoardId(long boardId) {
        return lists.findByBoard_Id(boardId);
    }

    /**
     * Gets all lists by boardId and status
     * @param boardId - the boardId to get the lists from
     * @param status - the status to filter the tasklists to
     * @return - A list of TaskLists from the specified board and status
     */
    public List<TaskList> findListByBoardIdAndStatus(long boardId, Status status) {
        if (!boards.existsById(boardId)) return null;
        return lists.findListsByBoardIdAndStatus(boardId, status.name());
    }

    /**
     * Gets all lists by boardId and status
     * @param boardId - the boardId to get the lists from
     * @return - A list of TaskLists from the specified board and status
     */
    public List<TaskList> findListByBoardIdNotDeleted(long boardId) {
        if (!boards.existsById(boardId)) return null;
        return lists.findNotDeletedListsByBoardId(boardId);
    }

    /**
     * Gets all lists by boardId and not deleted status
     * @param boardId - the boardId to get the lists from
     * @return - A list of TaskLists from the specified board which are not deleted
     */
    public List<TaskList> findNotDeletedListsByBoardId(long boardId) {
        return lists.findNotDeletedListsByBoardId(boardId);
    }

    /**
     * Calculates the next free index for a TaskList by board
     * @param boardId - the board to calculate the index for
     * @return - the calculated next index
     */
    public int calcNextIndex(Long boardId) {
        List<TaskList> taskListList = findNotDeletedListsByBoardId(boardId);
        if(taskListList == null) return 0;
        return taskListList.size();
    }

    /**
     * Logic for adding a Tasklist to a board, keeping in mind that index has to
     * be calculated
     * @param taskList - the TaskList object to add
     * @param boardId - the boardId of the board to add the TaskList to.
     * @return - the newly added TaskList
     */
    public TaskList addList(TaskList taskList, long boardId) {
        Optional<Board> b = boards.findById(boardId);
        if (taskList.name == null || !b.isPresent()) {
            return null;
        }
        taskList.board = b.get();

        // Newly created list should be added to the end initially
        if (taskList.index == null) {
            taskList.index = calcNextIndex(taskList.board.id);
        }
        if (taskList.status == null) {
            taskList.status = Status.ACTIVE;
        }

        TaskList saved = lists.save(taskList);
        return saved;
    }

    /**
     * Method for changing the order of indexes from a list of TaskLists
     * @param oldIndex - the previous index of the TaskList
     * @param newIndex - the new index of the TaskList
     * @param taskListList - the list of TaskLists to apply the updates to
     */
    public void reorderLists(int oldIndex, int newIndex, List<TaskList> taskListList) {
        // update all indexes to match the order of newly set index
        if (newIndex > oldIndex) {
            for (TaskList cur : taskListList) {
                if (cur.index <= newIndex && cur.index > oldIndex) {
                    cur.index--;
                }
            }
        }
        else {
            for (TaskList cur : taskListList) {
                if (cur.index >= newIndex && cur.index < oldIndex) {
                    cur.index++;
                }
            }
        }
        lists.saveAll(taskListList);
    }

    /**
     * Method for editing a list, keeping in mind the changing of ordered indexes, and
     * only updating the fields which are not null.
     * @param taskList - the TaskList that contains the updated data and id of old
     *                 tasklist
     * @return - the updated tasklist now stored in the database
     */
    public TaskList editList(TaskList taskList) {
        if (taskList.id == null || !lists.existsById(taskList.id)) {
            return null;
        }
        TaskList updList = lists.findById(taskList.id).get();

        if (taskList.name != null) updList.name = taskList.name;
        if (taskList.color != null) updList.color = taskList.color;

        // Update indexes of other TaskLists in the board, if the index of
        // the current TaskList is changed
        if (taskList.index != null) {
            List<TaskList> taskListList = findNotDeletedListsByBoardId(updList.board.id);
            // if index larger than amount of TaskLists in the board,
            // set index to match the end index
            if (taskList.index >= taskListList.size()) {
                taskList.index = taskListList.size() - 1;
            }
            reorderLists(updList.index, taskList.index, taskListList);
            updList.index = taskList.index;
        }
        TaskList saved = lists.save(updList);
        return saved;
    }

    /**
     * A method which removes a TaskList from a board rearranging the ones currently in that board
     * @param listId - id of list to be removed
     * @return the list that has been removed
     */
    public TaskList removeListFromBoard(Long listId) {
        if (listId == null) return null;

        Optional<TaskList> tl = lists.findById(listId);
        if (!tl.isPresent()) {
            return null;
        }
        TaskList taskList = tl.get();
        if (taskList.index.equals(-1) && taskList.status.equals(Status.DELETED)) return null;

        List<TaskList> listOfLists = findNotDeletedListsByBoardId(taskList.board.id);
        for (TaskList t : listOfLists) {
            if (t.index > taskList.index) {
                t.index--;
                lists.save(t);
            }
        }

        taskList.index = -1;
        taskList.status = Status.DELETED;
        return lists.save(taskList);
    }
}
