package server.api;

import commons.TaskList;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import server.database.BoardRepository;
import commons.Board;
import server.service.ListService;

import java.util.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardRepository boards;
    private final ListService listService;

    /**
     * Contributor for the REST Controller of the boards
     * @param boards - the repository of Board
     * @param listService - list service instance
     */
    public BoardController(BoardRepository boards, ListService listService) {
        this.boards = boards;
        this.listService = listService;
    }

    /**
     * Request for getting all the boards
     * @return - a list with all the boards from the repository
     */

    @GetMapping(path = { "", "/" })
    public List<Board> all(){
        return boards.findAll();
    }

    /**
     * Request for getting a board by id
     * @param id - the id of the needed board
     * @return - a Response Entity with the board
     */

    @GetMapping("/{id}")
    public ResponseEntity<Board> getById(@PathVariable("id") long id){
        if (id < 0 || !boards.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(boards.findById(id).get());
    }

    /**
     * Post request for adding a board.
     * @param board -  board to be added
     * @return - a response entity with OK method if the adding was successful
     */

    @PostMapping(path = { "", "/" })
    public ResponseEntity<Board> add(@RequestBody Board board) {
        if ((board.id != null && board.id < 0) || board.name == null || board.name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Board saved = boards.save(board);

        // Add demo cards to board
        TaskList todo = new TaskList("To-Do", saved);
        TaskList working = new TaskList("Working", saved);
        TaskList done = new TaskList("Done", saved);

        listService.addList(todo, saved.id);
        listService.addList(working, saved.id);
        listService.addList(done, saved.id);

        return ResponseEntity.ok(saved);
    }

    /**
     * Method for changing the password of a certain board
     * @param board - the board with the id and new password supplied
     * @return - the board with the updated password
     */
    @PostMapping("/changePassword")
    public ResponseEntity<Board> changePassword(@RequestBody Board board) {
        if (board.id == null || !boards.findById(board.id).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Board updated = boards.findById(board.id).get();
        updated.password = board.password;

        Board result = boards.save(updated);
        return ResponseEntity.ok(result);
    }

    /**
     * Method for changing the name of a board
     * @param board - the board with the id and new name supplied
     * @return - the board with the updated name
     */
    @PostMapping("/changeName")
    public ResponseEntity<Board> changeName(@RequestBody Board board) {
        if (board.id == null || !boards.findById(board.id).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Board updated = boards.findById(board.id).get();
        updated.name = board.name;

        Board result = boards.save(updated);
        return ResponseEntity.ok(result);
    }

    /**
     * Websocket handler for when board password is changed
     * @param board - Board object with new password
     * @param boardId - the id of the board for sending to specific clients only
     * @return - the board from database with updated password
     */
    @MessageMapping("/{boardId}/changePassword")
    @SendTo("/topic/{boardId}/changePassword")
    public Board handleChangePassword(Board board, @DestinationVariable long boardId) {
        var boardResponse = changePassword(board);
        return boardResponse.getBody();
    }

    /**
     * Websocket handler for when board password is changed
     * @param board - Board object with new password
     * @param boardId - the id of the board for sending to specific clients only
     * @return - the board from database with updated password
     */
    @MessageMapping("/{boardId}/changeName")
    @SendTo("/topic/{boardId}/changeName")
    public Board handleChangeName(Board board, @DestinationVariable long boardId) {
        var boardResponse = changeName(board);
        return boardResponse.getBody();
    }
}
