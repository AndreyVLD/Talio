package server.api;

import commons.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import commons.TaskList;
import org.springframework.web.context.request.async.DeferredResult;
import server.service.ListService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/lists")
public class ListController {
    private final ListService listService;

    /**
     * Constructor for the Controller handling the List of Tasks
     * @param listService - The service handling everything that has to do with tasklists
     */
    public ListController(ListService listService) {
        this.listService = listService;
    }

    /**
     * Request for getting all the TaskLists
     * @return - a list with all the Lists from the repository
     */
    @GetMapping(path = { "", "/" })
    public List<TaskList> all(){
        return listService.findAll();
    }

    /**
     * Request for getting a List by id
     * @param id - the id of the needed TaskList
     * @return - a Response Entity with the TaskList
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskList> getById(@PathVariable("id") Long id) {
        TaskList tl = listService.getById(id);
        if (tl == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(tl);
    }

    /**
     * Request for getting all TaskLists from a given board
     * @param id - the id of the given Board
     * @return - a Response Entity with the List of TaskList
     */
    @GetMapping("/board/{id}")
    public ResponseEntity<List<TaskList>> getListsByBoard(@PathVariable("id") Long id){
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(listService.getListsByBoardId(id));
    }

    /**
     * Request for getting all TaskLists from a given board
     * @param id - the id of the given Board
     * @param status - the status to filter
     * @return - a Response Entity with the List of TaskList
     */
    @GetMapping("/board/{id}/{status}")
    public ResponseEntity<List<TaskList>> getListsByBoardAndStatus(@PathVariable("id") Long id,
        @PathVariable("status") String status) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        if (Status.findByName(status) == null) status = "ACTIVE";

        List<TaskList> lists = listService
                .findListByBoardIdAndStatus(id, Status.findByName(status));
        if (lists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(lists);
    }

    /**
     * Request for getting all TaskLists from a given board
     * @param id - the id of the given Board
     * @return - a Response Entity with the List of TaskList
     */
    @GetMapping("/notDeleted/board/{id}")
    public ResponseEntity<List<TaskList>> getListsByBoardAndStatus(@PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        List<TaskList> lists = listService
            .findListByBoardIdNotDeleted(id);
        if (lists == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(lists);
    }


    /**
     * Post request for adding a List of tasks.
     * @param taskList - taskList to be added
     * @param boardId - ID of board to add list to
     * @return - a response entity with OK method if the adding was successful
     */
    @PostMapping("/{boardId}")
    public ResponseEntity<TaskList> add(@RequestBody TaskList taskList,
                                        @PathVariable("boardId") Long boardId) {
        TaskList added = listService.addList(taskList, boardId);
        if (added == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(added);
    }

    /**
     * Patch request for editing an existing tasklist
     * @param taskList The new content of the existing tasklist,
     * @return a response entity with OK status containing the changes made or Bad Request
     */
    @PatchMapping(path = { "", "/" })
    public ResponseEntity<TaskList> edit(@RequestBody TaskList taskList) {
        TaskList editedList = listService.editList(taskList);
        if (editedList == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(editedList);
    }

    /**
     * TaskList message handler for websockets
     * @param tl - TaskList instance to be added
     * @param boardId - ID of board to add list to
     * @return - The TaskList to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{boardId}/lists/add") //app/lists
    @SendTo("/topic/{boardId}/lists/add")
    public TaskList handleAdd(TaskList tl, @DestinationVariable long boardId) {
        var tlr = add(tl, boardId);
        return tlr.getBody();
    }

    /**
     * TaskList patch message handler for websockets
     * @param tl - TaskList instance to be edited
     * @param boardId - Not used, only here for correct messagemapping syntax
     * @return - The TaskList to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{boardId}/lists/edit") //app/lists
    @SendTo("/topic/{boardId}/lists/edit")
    public TaskList handleEdit(TaskList tl, @DestinationVariable long boardId) {
        var tlr = edit(tl);
        return tlr.getBody();
    }

    /**
     * TaskList patch message handler for websockets
     * @param tl - TaskList instance to be edited
     * @param boardId - Not used, only here for correct messagemapping syntax
     * @return - The TaskList to propagate along subscribers of this websocket topic
     */
    @MessageMapping("/{boardId}/lists/relocate") //app/lists
    @SendTo("/topic/{boardId}/lists/relocate")
    public TaskList handleRelocation(TaskList tl, @DestinationVariable long boardId) {
        var tlr = edit(tl);
        return tlr.getBody();
    }

    private ConcurrentMap<Object, Consumer<TaskList>> listeners = new ConcurrentHashMap<>();

    /**
     * Delete request for deleting an existing tasklist
     * in case of ok also notifies all listeners of getDeleteUpdates about the deleted taskList
     * @param listId the ID of the tasklist to delete,
     * @return a response entity with OK status containing deleted tasklist or Bad Request
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<TaskList> deleteById(@PathVariable("listId") Long listId) {
        TaskList deleted = listService.removeListFromBoard(listId);
        if (deleted == null) {
            return ResponseEntity.badRequest().build();
        }
        TaskList tlid = new TaskList();
        tlid.id = listId;
        listeners.forEach((k, l) -> l.accept(tlid));
        return ResponseEntity.ok().build();
    }

    /**
     * Registers a listener for long polling updates when deletion of a list occurs
     * @param boardId the id of the board
     * @return a response entity with OK if a deletion occurred or NO CONTENT after 5 seconds
     */
    @GetMapping("/{boardId}/lists/remove")
    public DeferredResult<ResponseEntity<TaskList>> getDeleteUpdates(
            @PathVariable("boardId") Long boardId) {
        var noContent = ResponseEntity.noContent().build();
        var res = new DeferredResult<ResponseEntity<TaskList>>(5000L, noContent);

        var key = new Object();
        listeners.put(key, q -> {
            res.setResult(ResponseEntity.ok(q));
        });
        res.onCompletion(() -> listeners.remove(key));

        return res;
    }

    /**
     * @param list List to change the title
     * @param listId id of the list
     * @return Task list of the edited tasklist
     */
    @MessageMapping("/{listId}/lists/edit/title")
    @SendTo("/topic/{listId}/lists/edit/title")
    public TaskList handleListEdit(TaskList list, @DestinationVariable Long listId) {
        var listResponse = edit(list);
        return listResponse.getBody();
    }
}
