package client.scenes;

import client.services.BoardService;
import client.utils.RecentBoardsUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordPopupCtrl implements Initializable {
    private final MainCtrl mainCtrl;
    private final BaseCtrl baseCtrl;
    private final BoardService boardService;
    private final RecentBoardsUtils rbu;

    private long boardId;
    private Tab parent;

    private boolean isChangePassword;

    @FXML
    Label popupTitle;
    @FXML
    Label invalid;
    @FXML
    PasswordField password;
    @FXML
    VBox vBox;
    @FXML
    CheckBox savePasswordCheck;
    @FXML
    HBox buttonHBox;
    @FXML
    Button cancelButton;
    @FXML
    Button submitButton;

    /**
     * Constructor for the password popup
     *
     * @param mainCtrl     - Used to manage scenes/stages
     * @param baseCtrl     - Used for updating the selected board id
     * @param boardService - Used to verify password and joining the board
     * @param rbu
     */
    @Inject
    public PasswordPopupCtrl(MainCtrl mainCtrl, BaseCtrl baseCtrl, BoardService boardService,
                             RecentBoardsUtils rbu) {
        this.mainCtrl = mainCtrl;
        this.baseCtrl = baseCtrl;
        this.boardService = boardService;
        this.rbu = rbu;
        this.isChangePassword = false;
    }

    /**
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        invalid.setVisible(false);
        vBox.getStyleClass().add("popup-vbox");
        vBox.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        submitButton.getStyleClass().add("submitButton");
        cancelButton.getStyleClass().add("submitButton");
    }

    /**
     * Method for when the cancel button is pressed
     */
    public void cancel() {
        if (isChangePassword) {
            parent.getOnClosed().handle(null);
            parent.getTabPane().getTabs().remove(parent);
        }
        invalid.setVisible(false);
        mainCtrl.getSecondaryStage().close();
    }

    /**
     * Method for when the submit button is pressed
     */
    public void submit() {
        String passwordText = password.getText();
        String boardPassword = boardService.getPasswordFromBoard(boardId);

        if (passwordText.equals(boardPassword)) {
            // This means the password is changed, so we don't have to join
            // a board
            if (isChangePassword) {
                invalid.setVisible(false);
                mainCtrl.getSecondaryStage().close();
            }
            else {
                invalid.setVisible(false);
                mainCtrl.getSecondaryStage().close();
                boardService.joinExistingBoard(boardId, parent);
                baseCtrl.changeSelectedBoardId(null);
            }
            if (savePasswordCheck.isSelected()) {
                rbu.setPasswordByBoardId(boardId, boardPassword);
            }
            else {
                rbu.setPasswordByBoardId(boardId, null);
            }
        }
        else {
            invalid.setVisible(true);
        }
    }

    /**
     * Method for converting the standard password popup to a changed password popup
     */
    public void convertToChangePasswordPopup() {
        popupTitle.setText("Password has been updated!");
        cancelButton.setText("Leave");
        this.isChangePassword = true;
    }

    /**
     * Sets the boardId for this popup instance (used for retrieving the correct password)
     * @param boardId - the boardId
     */
    public void setBoardId(long boardId) {
        this.boardId = boardId;
    }

    /**
     * Sets the parent Tab reference for the popup instance
     * (used for displaying the joined board correctly)
     * @param parent - the parent Tab
     */
    public void setParent(Tab parent) {
        this.parent = parent;
    }
}
