package client;

import client.scenes.BoardCtrl;
import client.scenes.EditCardCtrl;
import client.services.CardService;
import commons.Card;
import commons.Tag;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class Task {
    private Card card;
    private List<Tag> tags;
    private CardService cardService;
    private EditCardCtrl editCardCtrl;
    private FXMLLoader fxml;
    private BoardCtrl board;


    private final StackPane root;
    private Rectangle background;
    private Label titleLabel;
    private StackPane copy;
    private Separator separator;

    private boolean dragging = false;
    private BoardList prevParent;
    private int prevIdx;

    private Stage stage;
    private String color;

    /**
     * Constructor of Task class.
     * Creates the UI elements for the task.
     *
     * @param card  The card object containing the info for creating a new visual element
     * @param board The board instance in which the Task will be located.
     * @param cs    The instance of the CardService which provides
     *             functionality for modifying the task
     * @param edit  The instance of the edit card controller
     */
    public Task(Card card, BoardCtrl board, CardService cs, EditCardCtrl edit) {
        this.card = card;
        this.board = board;
        this.cardService = cs;
        this.editCardCtrl = edit;
        this.fxml = cardService.getFXML();
        if (card.color == null) {
            this.color = "#ffffff";
        } else
            this.color = card.color;

        root = getRootInstance(true);

        background = (Rectangle) ((StackPane) root.getChildren().get(0)).getChildren().get(0);
        // Set mouse events
        root.setOnMouseDragged(this::onMouseDragged);
        root.setOnMouseReleased(this::onMouseUp);
        root.setOnMouseClicked(this::editCard);
        root.setOnMouseEntered(event -> {
            background.setStroke(Paint.valueOf("black"));
            background.setStrokeWidth(2);
            Node focusOwner = board.getRootPane().getScene().getFocusOwner();
            String focusOwnerId = (focusOwner == null) ? null : focusOwner.getId();
            if (focusOwnerId == null || !focusOwnerId.equals("newCardTitle"))
                root.requestFocus();
        });

        root.setOnMouseExited(event -> {
            background.setStroke(Paint.valueOf("white"));
            background.setStrokeWidth(0);
            Node focusOwner = board.getRootPane().getScene().getFocusOwner();
            String focusOwnerId = (focusOwner == null) ? null : focusOwner.getId();
            if (focusOwnerId == null || !focusOwnerId.equals("newCardTitle"))
                board.setScrollPaneFocus();
        });

        addKeyboardShortcutToDeleteCard();
        editCardShortcut();
        addColorPickerShortcut();
        editTitleOfCardShortcut();
    }

    private void addKeyboardShortcutToDeleteCard() {

        root.requestFocus();

        final KeyCombination deleteKeyCombination = new KeyCodeCombination(KeyCode.DELETE);
        final KeyCombination backspaceKeyCombination = new KeyCodeCombination(KeyCode.BACK_SPACE);

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (deleteKeyCombination.match(event) || backspaceKeyCombination.match(event)) {
                deleteTask();
                cardService.deleteTask(card.id);
                event.consume();
                System.out.println("Keyboard Shortcut : Delete Key / Backspace Key");
            }
        };

        root.addEventHandler(KeyEvent.KEY_PRESSED, keyEventEventHandler);
    }

    private void editTitleOfCardShortcut() {

        root.requestFocus();

        final KeyCombination deleteKeyCombination = new KeyCodeCombination(KeyCode.E);

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (deleteKeyCombination.match(event)) {
                editTitle();
                event.consume();
                System.out.println("Keyboard Shortcut : E");
            }
        };

        root.addEventHandler(KeyEvent.KEY_PRESSED, keyEventEventHandler);
    }

    private void editTitle() {
        //TODO: Implement this method that makes a text-field appear to edit the task's title
        System.out.println("Edits the tasks title");
    }

    private void addColorPickerShortcut() {

        root.requestFocus();

        final KeyCombination cKeyCombination = new KeyCodeCombination(KeyCode.C);

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (cKeyCombination.match(event)) {
                Point2D sceneCoords = root.localToScene(0.0, 0.0);
                board.setColorPickerActive(true);
                editColor(sceneCoords);
                event.consume();
                System.out.println("Keyboard Shortcut : C");
            }
        };

        root.addEventHandler(KeyEvent.KEY_PRESSED, keyEventEventHandler);
    }

    /**
     * @param coordinates Coordinates of the card that the shortcut happened
     * Gets the color from a method and sets the color to the respective card.
     */
    public void editColor(Point2D coordinates) {
        CompletableFuture<String> hexCodeFuture = board.colorPickerShortcut(coordinates);
        hexCodeFuture.thenAccept(hexCode -> {
            System.out.println(hexCode);
            this.color = hexCode;
            setCardColor();
            board.setColorPickerStackPaneVisibility(false);
            board.setScrollPaneFocus();
        });
    }

    private void setCardColor() {
        cardService.changeCardColor(card.id, color);
    }

    private void editCardShortcut() {

        root.requestFocus();

        final KeyCombination enterKeyCombination = new KeyCodeCombination(KeyCode.ENTER);

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (enterKeyCombination.match(event)) {
                showEditCard();
                event.consume();
                System.out.println("Keyboard Shortcut : Enter");
            }
        };

        root.addEventHandler(KeyEvent.KEY_PRESSED, keyEventEventHandler);
    }


    private void onMouseDragged(MouseEvent event) {
        cardService.dragDropEnabled(-1);
        if (!dragging) {
            prevParent = board.getLists().get(findClosestList(event.getSceneX()));
            prevIdx = prevParent.getTaskHolder().getChildren().indexOf(root);
        }
        dragging = true;

        Node topNode = event.getPickResult().getIntersectedNode();
        if (topNode != null) {
            // Consume the event so that it doesn't propagate to other nodes
            event.consume();
        }
        if (copy == null) {
            // Add copy to layout
            copy = getRootInstance(true);
            copy.setOpacity(0.4);
            board.getRootPane().getChildren().add(copy);
        }

        // Align copy with mouse
        Bounds rootBounds =
                board.getRootPane().localToScene(board.getRootPane().getBoundsInLocal());

        double posX = event.getSceneX() - rootBounds.getMinX()
                - copy.getBoundsInParent().getWidth() / 2;
        posX = Math.max(posX, 20);
        posX = Math.min(posX, rootBounds.getMaxX() - rootBounds.getMinX()
                - copy.getBoundsInParent().getWidth() - 20);
        double posY = event.getSceneY() - rootBounds.getMinY()
                - copy.getBoundsInParent().getHeight() / 2;
        posY = Math.max(posY, 20);
        posY = Math.min(posY, rootBounds.getMaxY() - rootBounds.getMinY()
                - copy.getBoundsInParent().getHeight() - 20);

        copy.setLayoutX(posX);
        copy.setLayoutY(posY);

        // Find closes list
        BoardList closestList = board.getLists().get(findClosestList(event.getSceneX()));
        int closestTaskPosition = closestList.findClosestTaskPosition(event.getSceneY());

        // Add separator
        if (separator == null) separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        int separatorIndex = closestList.getTaskHolder().getChildren().indexOf(separator);
        if (separatorIndex != -1) closestList.getTaskHolder().getChildren().remove(separator);
        if (separatorIndex != closestTaskPosition - 1 || separatorIndex == -1)
            closestList.getTaskHolder().getChildren().add(closestTaskPosition, separator);
    }

    /**
     * @param title String representing the title.
     *              Sets the title of the task.
     */
    public void setTitle(String title) {
        card.title = title;
        titleLabel.setText(title);
    }

    private int findClosestList(double mouseX) {
        int closest = 0;
        double closestDistance = Math.abs(0 - mouseX);
        ObservableList<Node> lists = board.getListHolder().getChildren().filtered(
                node -> node.getClass() != Separator.class
        );
        for (int i = 0; i < lists.size() - 1; i++) {
            Node list = lists.get(i);
            double mouseDistance =
                    Math.abs(list.localToScene(list.getBoundsInLocal()).getCenterX() - mouseX);
            if (mouseDistance < closestDistance) {
                closestDistance = mouseDistance;
                closest = i;
            }
        }
        return closest;
    }

    private void onMouseUp(MouseEvent event) {
        if (!dragging) return;
        // Remove the copy
        board.getRootPane().getChildren().remove(copy);
        copy = null;

        Pane closestList = (Pane) separator.getParent();

        BoardList newParent = board.getLists().get(findClosestList(event.getSceneX()));
        int newIdx = closestList.getChildren().indexOf(separator);
        if (prevParent == newParent && newIdx > prevIdx) newIdx--;

        // Remove separator
        closestList.getChildren().remove(separator);
        dragging = false;

        cardService.relocateTask(card.id, prevParent.getId(), newParent.getId(), newIdx);
        cardService.dragDropEnabled(+1);
    }

    /**
     * @return Root UI object.
     */
    public StackPane getRoot() {
        return root;
    }

    /**
     * @param flag whether to create a new root or the update the existing one
     * @return Returns the root of a new instance of the tasks UI.
     */
    public StackPane getRootInstance(boolean flag) {
        StackPane newRoot;
        if (flag) newRoot = new StackPane();
        else { newRoot = this.root; this.root.getChildren().clear(); }
        newRoot.setPadding(new Insets(8, 0, 8, -10));

        // Create the background rectangle
        Rectangle background = new Rectangle(250, 50);
        background.setFill(Paint.valueOf(color));
        background.setArcWidth(20);
        background.setArcHeight(20);
        // Create the title label
        titleLabel = new Label(card.title);
        titleLabel.setMaxWidth(200); titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Paint.valueOf("#787878"));

        // Add a delete button
        Button deleteCard = new Button("X");
        deleteCard.setPrefWidth(15); deleteCard.setPrefHeight(15);
        deleteCard.setFont(Font.font("Arial", 12));
        deleteCard.setStyle("-fx-background-color: transparent; "
                + "-fx-border-color: transparent; -fx-text-fill: black;");
        deleteCard.setOnMouseEntered(event ->
                deleteCard.setStyle("-fx-background-color:transparent;"
                + " -fx-border-color:transparent;-fx-text-fill: red;"));
        deleteCard.setOnMouseExited(event ->
                deleteCard.setStyle("-fx-background-color:transparent;"
                + " -fx-border-color: transparent; -fx-text-fill: black;"));
        deleteCard.setOnAction(e -> {
            cardService.deleteTask(card.id);
            board.hideColorPicker();
        });

        HBox tagsIndicator = new HBox();
        Label descrIndicator = new Label(), progressIndicator = new Label();
        initializeIndicators(tagsIndicator, descrIndicator, progressIndicator);

        // Create a StackPane to stack the button on top of the rectangle
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(background, titleLabel);
        stackPane.getChildren().addAll(tagsIndicator, descrIndicator, progressIndicator);
        stackPane.getChildren().add(deleteCard);
        StackPane.setAlignment(deleteCard, Pos.CENTER_RIGHT);
        StackPane.setAlignment(descrIndicator, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(progressIndicator, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(tagsIndicator, Pos.TOP_CENTER);
        newRoot.getChildren().add(stackPane);
        return newRoot;
    }

    private void initializeIndicators(HBox tagsInd, Label descrInd, Label progrInd) {
        tagsInd.setSpacing(5);
        tagsInd.setPadding(new Insets(5, 15, 0, 15));
        tagsInd.setPrefHeight(10);

        for (Tag t : cardService.getTagsByCard(card.id)) {
            if (tagsInd.getChildren().size() == 6) break;
            Rectangle visualTag = new Rectangle(32, 5);
            visualTag.setFill(Paint.valueOf(t.color));
            tagsInd.getChildren().add(visualTag);
        }

        if (card.description != null && !card.description.isBlank()) descrInd
            .setText("  \u2261   ");
        descrInd.setFont(Font.font("Arial", 14));
        descrInd.setTextFill(Paint.valueOf("787878"));

        if (card.totalSubtasks != -1)
            progrInd.setText("(" + card.doneSubtasks + " / " +  card.totalSubtasks + " done)   ");
        progrInd.setFont(Font.font("Arial", 12));
        progrInd.setTextFill(Paint.valueOf("787878"));
    }

    /**
     * deletes an existing task from the list
     */
    private void deleteTask() {
        board.hideColorPicker();
        ((Pane) root.getParent()).getChildren().remove(root);
    }

    /**
     * This method detects when the user double-clicks on a task card
     * When this is detected, the Edit Card scene opens, where the
     * user can edit the card details.
     * @param event MouseEvent - in this case click
     */
    private void editCard(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                showEditCard();
            }
        }
    }

    /**
     * This method loads a new EditCard scene and displays it in a new stage
     * The EditCard Controller of the new scene is responsible for saving all the changes made.
     */
    public void showEditCard() {
        var res = fxml.returnEditCard(); // reload only the scene, the controller is the same
        stage = new Stage();

        editCardCtrl.loadEditingCardAndStage(this, stage);
        board.setDarkBackground(true);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initStyle(StageStyle.TRANSPARENT);

        AtomicReference<Double> xOffset = new AtomicReference<>((double) 0);
        AtomicReference<Double> yOffset = new AtomicReference<>((double) 0);

        stage.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            xOffset.set(event.getSceneX());
            yOffset.set(event.getSceneY());
        });

        stage.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            stage.setX(event.getScreenX() - xOffset.get());
            stage.setY(event.getScreenY() - yOffset.get());
        });

        Scene s = new Scene(editCardCtrl.getRootElement());
        s.setFill(Color.TRANSPARENT);
        stage.setScene(s);
        stage.setResizable(false);
        stage.showAndWait();
    }

    /**
     * Closes the edit card stage and returns the focus on the board scroll pane
     */
    public void closeEditCardStage (){
        if (stage == null) return;
        stage.close();
        stage = null;
        board.setDarkBackground(false);
        board.setScrollPaneFocus();
    }

    /**
     * @return Title of the Task
     */
    public String getTitle() {
        return card.title;
    }

    /**
     * @return Description of the Task
     */
    public String getDescription() {
        return card.description;
    }

    /**
     * @param description new description
     */
    public void setDescription(String description) {
        card.description = description;
    }

    /**
     * @return id of the card
     */
    public Long getId() {
        return card.id;
    }

    /**
     * @return Background of the task
     */
    public Rectangle getBackground() {
        return background;
    }

    /**
     * Sets the color when rendering the task
     * @param color hex value of the cards color
     */
    public void setColor(String color) {
        this.color = color;
        background.setFill(Paint.valueOf(color));
    }

    /**
     * Apply visual changes
     * @param card the updated card
     */
    public void applyVisualChanges(Card card) {
        this.card = card;
        getRootInstance(false);
    }
}

