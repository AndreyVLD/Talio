package client.scenes;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class RecentBoardCtrl implements Initializable {
    NewTabCtrl newTabCtrl;

    @FXML
    private AnchorPane root;
    @FXML
    private Hyperlink boardLink;
    @FXML
    private Button button;
    @FXML
    private Rectangle rectangle;

    /**
     *
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
        boardLink.prefWidthProperty().bind(rectangle.widthProperty());
        boardLink.prefHeightProperty().bind(rectangle.heightProperty());
        boardLink.setStyle("-fx-padding: 0px 0px 0px 15px;" +
                "-fx-background-radius: 10px;");
        boardLink.setOnMouseEntered(event -> rectangle.setFill(Paint.valueOf("#999999")));
        boardLink.setOnMouseExited(event -> rectangle.setFill(Paint.valueOf("#787878")));

        String normalColor = "rgba(0,0,0,0)";
        String hoverColor = "#BBBBBB";
        String pressedColor = "#666666";
        button.setStyle("-fx-background-color: " + normalColor + "; " +
                "-fx-text-fill: #BBBBBB;-fx-background-radius: 20px; ");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + hoverColor
                + "; -fx-text-fill: #333333;-fx-background-radius: 20px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + normalColor + ";" +
                " -fx-text-fill: #BBBBBB;-fx-background-radius: 20px;"));
        button.setOnMousePressed(e -> button.setStyle("-fx-background-color: " + pressedColor + "; "
                + "-fx-text-fill: black;-fx-background-radius: 20px;"));
        button.setOnMouseReleased(e -> button.setStyle("-fx-background-color: " + hoverColor + ";" +
                " -fx-text-fill: black;-fx-background-radius: 20px;"));
    }

    /**
     * Constructor for the RecentBoard
     * @param newTabCtrl - instance of the newTabCtrl
     */
    @Inject
    public RecentBoardCtrl(NewTabCtrl newTabCtrl){
        this.newTabCtrl = newTabCtrl;
    }

    /**
     * Sets the text and the onclick action of the hyperlink
     * @param boardName the name of the board
     * @param onclick action for displaying the selected board
     * @param onDelete action for when the delete button is clicked
     */
    public void initialize(String boardName, EventHandler onclick, EventHandler onDelete) {
        this.boardLink.setText(boardName);
        this.boardLink.setOnAction(onclick);
        this.button.setOnAction(onDelete);
    }

    /**
     * Return the root element
     *
     * @return the stack pane which contains the whole record
     */
    public AnchorPane getRootElement() {
        return root;
    }

}
