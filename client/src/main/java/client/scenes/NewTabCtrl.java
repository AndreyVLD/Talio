package client.scenes;

import client.FXMLLoader;
import client.services.BoardService;
import client.utils.RecentBoardsUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Board;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;


public class NewTabCtrl implements Initializable {
    private final ServerUtils server;
    private final BaseCtrl baseCtrl;
    private final MainCtrl mainCtrl;
    private final BoardService boardService;
    private final RecentBoardsUtils rbu;
    private final FXMLLoader fxml;

    private Tab parent;
    @FXML
    private TextField boardName;
    @FXML
    private TextField boardId;
    @FXML
    private VBox recentBoards;
    @FXML
    private ScrollPane scrollPaneRecents;

    /**
     * @param server   Instance of server class.
     * @param baseCtrl Instance of the base controller.
     * @param mainCtrl Instance of the main control.
     * @param bs       Instance of the board service.
     * @param rbu      Instance of the recent boards utils.
     * @param fxml     Instance of the fxml loader.
     */
    @Inject
    public NewTabCtrl(ServerUtils server, BaseCtrl baseCtrl, MainCtrl mainCtrl, BoardService bs,
                      RecentBoardsUtils rbu, FXMLLoader fxml) {
        this.baseCtrl = baseCtrl;
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.boardService = bs;
        this.rbu = rbu;
        this.fxml = fxml;
    }

    /**
     * Initializes a new tab
     * @param location
     * The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     *
     * @param resources
     * The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPaneRecents.getStylesheets().add(getClass()
                .getResource("sec_styles.css").toExternalForm());
        scrollPaneRecents.setFitToWidth(true);
        recentBoards.setSpacing(5);


        Map<Long, String> boards = rbu.getBoards();

        for (Map.Entry<Long, String> entry : boards.entrySet()) {

            RecentBoardCtrl rb = fxml.returnRecentBoard().getKey();
            rb.initialize(entry.getValue(), event -> {
                Long id = entry.getKey();
                boardId.setText(id.toString());
                joinBoardButton();
            }, event -> {
                recentBoards.getChildren().remove(rb.getRootElement());
                rbu.deleteBoard(entry.getKey());
                });

            recentBoards.getChildren().add(rb.getRootElement());
        }
    }

    /**
     * @param parent Sets the parent of the tab so the tab class
     *               can interact with it.
     */
    public void setParent(Tab parent){
        this.parent = parent;
    }

    /**
     * Creates a new board and opens it in a new tab
     */
    public void createBoardButton(){
        if (!server.isConnected()) {
            mainCtrl.createPopUp("No connection", "Client not connected to a server! " +
                "Please go to Preferences to enter connection details");
        }
        // Check name
        if (boardName.getText().equals("")){
            mainCtrl.createPopUp("Invalid input!", "Please enter a valid board name");
            return;
        }

        // Create new board on the server
        Board b = new Board(boardName.getText());
        boardService.createNewBoard(b, parent);
        baseCtrl.changeSelectedBoardId(null);
    }

    /**
     * Function to try to join a new board.
     */
    public void joinBoardButton(){
        long boardIdLong;
        if (!server.isConnected()) {
            mainCtrl.createPopUp("No connection", "Client not connected to a server! " +
                "Please go to Preferences to enter connection details");
        }
        try{
            boardIdLong = Long.parseLong(boardId.getText());
        } catch (Exception e) {
            mainCtrl.createPopUp("Invalid input!", "Please enter a valid board id.");
            return;
        }
        if (boardService.checkIfOpened(boardIdLong)) {
            mainCtrl.createPopUp("Board already opened!", "You cannot open a single" +
                " board in more than one tab.");
            return;
        }
        if (boardService.getBoardFromServer(boardIdLong) == null) {
            mainCtrl.createPopUp("Board does not exist!", "Board with id '" + boardIdLong +
                    "' does not exist on the server.");
            return;
        }
        String curPassword = boardService.getPasswordFromBoard(boardIdLong);
        if (curPassword != null) {
            if (curPassword.equals(rbu.getPasswordByBoardId(boardIdLong))) {
                boardService.joinExistingBoard(boardIdLong, parent);
                baseCtrl.changeSelectedBoardId(null);
                return;
            }
            mainCtrl.showPasswordPopup(boardIdLong, parent, false);
            return;
        }
        boardService.joinExistingBoard(boardIdLong, parent);
        baseCtrl.changeSelectedBoardId(null);
    }
}
