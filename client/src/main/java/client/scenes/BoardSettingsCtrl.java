package client.scenes;

import client.services.BoardService;
import commons.Board;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.control.Button;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class BoardSettingsCtrl implements Initializable {

    private long boardId;
    private Board board;
    private final BoardService boardService;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField boardName;
    @FXML
    private Text joinKey;
    @FXML
    private ImageView copyButton;
    @FXML
    private ImageView publicButtonImg;
    @FXML
    private ImageView privateButtonImg;
    @FXML
    private HBox publicButton;
    @FXML
    private HBox privateButton;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button saveButton;

    /**
     * Constructor for BoardSettings menu
     * @param boardService - boardService reference
     * @param mainCtrl - mainCtrl reference
     */
    @Inject
    public BoardSettingsCtrl(BoardService boardService, MainCtrl mainCtrl) {
        this.boardService = boardService;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Method for setting the boardId of the menu globally
     * @param boardId - the boardId
     */
    public void setBoardId(long boardId) {
        this.boardId = boardId;
    }

    /**
     * Method for formatting the join key to 6 digits
     * @param boardId - the boardId to format
     * @return - the formatted join key string
     */
    private String formatJoinKey(long boardId){
        return "0".repeat(6 - String.valueOf(boardId).length()) +
                boardId;
    }

    /**
     * Initialize logic and fetch data from the server
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
        joinKey.setText(formatJoinKey(boardId));
        switchRadio(true);
        publicButton.setOnMouseClicked(event -> {
            switchRadio(true);
        });
        privateButton.setOnMouseClicked(event -> {
            switchRadio(false);
        });
        copyButton.setOnMouseClicked(event -> {
            copy(joinKey.getText());
        });

        // load images
        Image copyImage = new Image("client/scenes/img/copy.png");
        copyButton.setImage(copyImage);
        Image publicImage = new Image("client/scenes/img/public.png");
        publicButtonImg.setImage(publicImage);
        Image privateImage = new Image("client/scenes/img/private.png");
        privateButtonImg.setImage(privateImage);

        // Load data
        this.board = boardService.getBoardFromServer(boardId);
        boardName.setText(board.name);

        if(board.password == null){
            switchRadio(true);
        } else {
            switchRadio(false);
            passwordField.setText(board.password);
        }
    }

    /**
     * Switch the button to public/private
     * @param publicBoard - true if public should be selected
     */
    private void switchRadio(boolean publicBoard){
        if(publicBoard){
            publicButton.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 10");
            privateButton.setStyle("-fx-background-color: white");
            passwordField.setVisible(false);
        } else {
            privateButton.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 10");
            publicButton.setStyle("-fx-background-color: white");
            passwordField.setVisible(true);
        }
    }

    /**
     * Method for copying the join key
     * @param copyString - the string to put in the clipboard
     */
    private void copy(String copyString){
        StringSelection stringSelection = new StringSelection(copyString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    /**
     * Method for saving the changes to the board on the server
     */
    public void save() {
        boardService.changeBoardName(boardId, boardName.getText());

        // Small pause in between request, to avoid concurrent modification
        // On the server (Hotfix)
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        if (passwordField.isVisible()) {
            if (passwordField.getText().length() < 1) {
                boardService.changeBoardPassword(boardId, null);
            }
            else if (!passwordField.getText().equals(board.password)) {
                boardService.changeBoardPassword(boardId, passwordField.getText());
            }
        }
        else {
            boardService.changeBoardPassword(boardId, null);
        }
        mainCtrl.closeBoardPreferences();
    }
}
