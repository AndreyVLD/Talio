package client;

import client.scenes.BoardCtrl;
import client.services.ListService;
import commons.TaskList;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;

public class BoardList {
    private Long id;
    private ListService listService;
    private ArrayList<Task> tasks = new ArrayList<>();
    private VBox root;
    private VBox copy;
    private String title;
    private Label titleLabel;
    private BoardCtrl board;
    private Separator separator;

    private Button addButton;
    private boolean dragging = false;

    private HBox buttonBox;

    private FXMLLoader fxml;

    /**
     * Empty constructor
     */
    public BoardList() {};

    /**
     * Constructor of board list class.
     * Creates the ui elements for the list.
     * @param list the TaskList object containing the info for creating a new visual element
     * @param board The board instance of the board.
     * @param ls The instance of the ListService which provides functionality for modifying the list
     */
    public BoardList(TaskList list, BoardCtrl board, ListService ls) {
        this.id = list.id;
        this.title = list.name;
        this.board = board;
        this.listService = ls;
        this.fxml = listService.getFXML();

        root = getRootInstance();
        buttonBox = ((HBox) root.getChildren().get(root.getChildren().size() - 1));
        addButton = (Button) buttonBox.getChildren().get(0);
        addButton.setOnAction(event -> {
            newCardDialog();
        });
        titleLabel = (Label) ((HBox) root.getChildren().get(0)).getChildren().get(0);

        // Set mouse events
        root.setOnMouseReleased(this::onMouseUp);
        root.setOnMouseDragged(this::onDragMouse);
        titleLabel.setOnMouseClicked(this::editListTitle);
    }

    /**
     * Setter fot the id
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    @SuppressWarnings("CyclomaticComplexity")

    private void onDragMouse(MouseEvent event){
        if (!listService.dragDropEnabled(0)) return;

        Bounds rootBounds =
            board.getRootPane().localToScene(board.getRootPane().getBoundsInLocal());

        Node topNode = event.getPickResult().getIntersectedNode();
        if (topNode != null) {
            // Consume the event so that it doesn't propagate to other nodes
            event.consume();
        }

        dragging = true;

        // Check if copy already exists
        if(copy == null){
            // Add copy to layout
            copy = getRootInstance();
            copy.setOpacity(0.4);
            board.getRootPane().getChildren().add(copy);
        }

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

        // Align copy with mouse
        copy.setLayoutX(posX);
        copy.setLayoutY(posY);

        // Find the closest position and add separator
        int closest = findClosestPosition(event.getSceneX());
        if(separator == null) separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);
        int separatorIndex = board.getListHolder().getChildren().indexOf(separator);
        if(separatorIndex != -1) board.getListHolder().getChildren().remove(separator);
        if(separatorIndex != closest -1 || separatorIndex == -1)
            board.getListHolder().getChildren().add(closest, separator);
    }

    private int findClosestPosition(double mouseX){
        int closest = 0;
        double closestDistance = Math.abs(0 - mouseX);
        ObservableList<Node> lists = board.getListHolder().getChildren().filtered(
                node -> node.getClass() != Separator.class
        );
        for (int i = 0; i < lists.size() - 1; i++){
            Node list = lists.get(i);
            double mouseDistance =
                    Math.abs(list.localToScene(list.getBoundsInLocal()).getMaxX() - mouseX);
            if(mouseDistance < closestDistance){
                closestDistance = mouseDistance;
                closest = i + 1;
            }
        }
        return closest;
    }

    /**
     * @param mouseY The scene mouse Y position.
     * @return the index of the closest position of the mouse to drop the task.
     */
    public int findClosestTaskPosition(double mouseY){
        int closest = 0;
        ObservableList<Node> lists =
                getTaskHolder().getChildren().filtered(node -> node.getClass() != Separator.class);
        double closestDistance =Math.abs(getTaskHolder().localToScene(
                getTaskHolder().getBoundsInLocal()).getMinY() - mouseY);
        for (int i = 0; i <lists.size(); i++){
            Node list = lists.get(i);
            double mouseDistance =
                    Math.abs(list.localToScene(list.getBoundsInLocal()).getMaxY()- mouseY);
            if(mouseDistance < closestDistance){
                closestDistance = mouseDistance;
                closest = i + 1;
            }
        }
        return closest;
    }

    private void onMouseUp(MouseEvent event){
        if(!dragging) return;
        // Remove the copy
        board.getRootPane().getChildren().remove(copy);
        copy = null;

        int oldIdx = board.getListHolder().getChildren().indexOf(root);
        int newIdx = board.getListHolder().getChildren().indexOf(separator);
        if (oldIdx < newIdx) newIdx--;

        // Remove separator
        board.getListHolder().getChildren().remove(separator);
        separator = null;
        dragging = false;

        listService.relocateList(id, newIdx);
    }

    /**
     * This method detects when the user double-clicks on a list
     * When this is detected, the Change title scene opens, where the
     * user can edit the title of the list.
     * @param event MouseEvent - in this case click
     *
     */
    private void editListTitle(MouseEvent event) {
        if(event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                editList();
            }
        }
    }


    /**
     * @param title String representing the title
     * Adds a title to a new board
     */
    public void setTitle(String title){
        this.title = title;
        titleLabel.setText(title);
    }

    /**
     * @return returns the task container.
     */
    public VBox getTaskHolder() {
        return (VBox) root.getChildren().get(1);
    }

    /**
     * @return Root UI object.
     */
    public VBox getRoot() {
        return root;
    }


    /** Adds a Task to an existing BoardList
     * @param task The task object you want to add.
     * @param pos The position to add the task on.
     */
    public void addTask(Task task, int pos) {
        board.resetHover();
        if (pos == -1) {
            tasks.add(task);
            getTaskHolder().getChildren().add(task.getRoot());
        } else {
            tasks.add(pos, task);
            getTaskHolder().getChildren().add(pos, task.getRoot());
        }
    }

    /**
     * Remove task
     * @param t the task object to be removed
     */
    public void removeTask(Task t) {
        board.resetHover();
        // Remove the task from the UI
        getTaskHolder().getChildren().remove(t.getRoot());

        // Remove the task from the list
        tasks.remove(t);
    }

    /**
     * @return Returns the root of a new instance of the list UI.
     */
    public VBox getRootInstance(){
        VBox newRoot = new VBox();
        newRoot.setStyle("-fx-background-color:  #D9D9D9; -fx-background-radius: 10;");
        newRoot.setPadding(new Insets(30, 30, 30, 30));

        newRoot.setMinWidth(300);
        newRoot.setPrefWidth(300);
        newRoot.setMaxWidth(Region.USE_COMPUTED_SIZE);
        newRoot.setMaxHeight(Region.USE_PREF_SIZE);

        // Create title label
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Paint.valueOf("#6A6A6A"));
        newRoot.getChildren().add(titleLabel);
        titleLabel.setMinWidth(240);

        //Create a button, which deletes existing lists
        Button deleteList = new Button("-");
        addDeleteButton(deleteList);

        HBox titleAndControl = new HBox();
        titleAndControl.getChildren().addAll(titleLabel, deleteList);
        titleAndControl.setPadding(new Insets(-20, -20, 10, 0));
        newRoot.getChildren().add(titleAndControl);

        VBox taskHolder = new VBox();
        newRoot.getChildren().add(taskHolder);
        for (Task task : tasks) {
            newRoot.getChildren().add(task.getRootInstance(true));
        }
        Button addButton = new Button("+");
        addButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        addButton.setStyle("-fx-text-fill: Black; -fx-background-color: transparent;");
        HBox buttonBox = new HBox(addButton);
        buttonBox.setAlignment(Pos.CENTER);
        newRoot.getChildren().add(buttonBox);
        return newRoot;
    }

    /**
     * Creates a button that allows the user to edit the lists name
     * @param editListTitle Button from the method above
     */
    private void addEditListButton(Button editListTitle) {
        editListTitle.setPrefWidth(10);
        editListTitle.setPrefHeight(10);
        editListTitle.setFont(Font.font("Aerial", 10));
        editListTitle.setStyle("-fx-background-color: #fcd200;" +
                " -fx-border-color: transparent;"
                + "-fx-text-fill: black; -fx-background-radius:50;");
        editListTitle.setOnMouseEntered(event -> editListTitle.setStyle(
                "-fx-background-color: #ff7300;"
                + "-fx-border-color:transparent; -fx-text-fill:black; " +
                        "-fx-background-radius:50%;"));
        editListTitle.setOnMouseExited(event -> editListTitle.setStyle(
                "-fx-background-color: #fcd200;"
                + "-fx-border-color:transparent; -fx-text-fill:black;" +
                        " -fx-background-radius:50%;"));
        editListTitle.setOnAction(event -> editList());
    }

    /**
     * Makes a button that allows the user to delete list
     * @param deleteList button
     */
    private void addDeleteButton(Button deleteList) {
        deleteList.setPrefWidth(10);
        deleteList.setPrefHeight(10);
        deleteList.setFont(Font.font("Arial", 10));
        deleteList.setStyle("-fx-background-color: #6A6A6A; " +
                "-fx-border-color: transparent;"
                + "-fx-text-fill: black; -fx-background-radius:50%;");
        deleteList.setOnMouseEntered(event -> deleteList.setStyle(
                "-fx-background-color: red;"
                + "-fx-border-color:transparent; -fx-text-fill:black;" +
                        "-fx-background-radius:50%;"));
        deleteList.setOnMouseExited(event -> deleteList.setStyle(
                "-fx-background-color: #6A6A6A;"
                + "-fx-border-color:transparent; -fx-text-fill:black;" +
                        " -fx-background-radius:50%;"));
        deleteList.setOnAction(event -> listService.deleteList(id, board.getId()));
    }

    /**
     * Method called when pressing the edit list buttons
     * Opens a new stage that prompts the user to change the name of the list.
     * Saves and updates the database and websockets
     */
    public void editList() {
        Stage stage = new Stage();
        stage.setTitle("Edit List");
        stage.initStyle(StageStyle.UTILITY);

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.setSpacing(10);

        TextField textField = new TextField(title);
        textField.setStyle("-fx-background-color: #f2f2f2;" +
                " -fx-background-radius: 5px; " +
                "-fx-border-radius: 20px; -fx-padding: 5px;");

        Button saveButton = new Button("SAVE");
        saveButton.setStyle("-fx-background-color: #00b2ff;" +
                " -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 5px 20px;");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(textField, new Insets(0, 0, saveButton.getHeight() / 2, 0));
        VBox.setMargin(saveButton, new Insets(10, 0, 0, 0));

        saveButton.setOnAction(event -> {
            saveEditedListTitle(textField.getText());
            stage.close();
        });

        vbox.getChildren().addAll(textField, saveButton);

        Scene scene = new Scene(vbox, 300, 100);
        scene.getRoot().setStyle("-fx-background-color: #5b5b5b;");

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.show();
    }

    private void saveEditedListTitle(String newTitle) {
        listService.saveEditedList(id, newTitle);
    }

    /**
     * @return the parent board controller
     */
    public BoardCtrl getBoard() {
        return board;
    }

    /**
     * @return the id of the list
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the title
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Shows a dialog box where the user can enter a title for a new card.
     */
    public void newCardDialog() {
        var res = fxml.returnNewCard();
        res.getKey().setParentBoardList(this);
        buttonBox.getChildren().remove(addButton);
        buttonBox.getChildren().add(res.getKey().getRoot());
    }

    /**
     * Closes the dialog box for setting up the title of a new card
     */
    public void closeNewCardDialog(){
        buttonBox.getChildren().remove(0);
        buttonBox.getChildren().add(addButton);
        board.setScrollPaneFocus();
    }

    /**
     * @return List of tasks in a board list
     */
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    /**
     * Turns hover on in the board
     */
    public void applyHover() {
        board.turnHoverOn();
    }
}
