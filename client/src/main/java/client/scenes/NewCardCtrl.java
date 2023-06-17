package client.scenes;

import client.BoardList;
import client.services.ListService;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ResourceBundle;

public class NewCardCtrl implements Initializable {
    private ListService listService;

    private BoardList parentBoardList;
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private StackPane stackPane;

    @FXML
    private TextField newCardTitle;

    @FXML
    private Button closeDialogButton;

    /**
     * Constructor for the new card ctrl
     * @param listService the instance of the ListService
     */
    @Inject
    public NewCardCtrl(ListService listService) {
        this.listService = listService;
    }

    /**
     * Sets the controller of the parent list
     * @param parentBoardList
     */
    public void setParentBoardList(BoardList parentBoardList) {
        this.parentBoardList = parentBoardList;
    }

    /**
     * Initializes the new card controller and its related scene
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

        newCardTitle.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                listService.createNewCard(parentBoardList.getId(), newCardTitle.getText());
                parentBoardList.closeNewCardDialog();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                parentBoardList.closeNewCardDialog();
            }
        });

        closeDialogButton.setFont(Font.font("Arial", 13));
        closeDialogButton.setStyle(
            "-fx-background-color:transparent; -fx-border-color:transparent; -fx-text-fill:black;");
        closeDialogButton.setOnMouseEntered(event ->closeDialogButton.setStyle(
            "-fx-background-color:transparent; -fx-border-color:transparent; -fx-text-fill: red;"));
        closeDialogButton.setOnMouseExited(event -> closeDialogButton.setStyle(
            "-fx-background-color:transparent; -fx-border-color:transparent;-fx-text-fill:black;"));

        Platform.runLater(() -> newCardTitle.requestFocus());
        newCardTitle.focusedProperty().addListener((k, p, n) -> {
            if (n == false) onCloseButtonClicked();
        });
    }

    /**
     * @return get root element
     */
    public AnchorPane getRoot()
    {
        return anchorPane;
    }

    /**
     * Method for preforming action on close button clicked
     */
    public void onCloseButtonClicked() {
        parentBoardList.closeNewCardDialog();
    }
}