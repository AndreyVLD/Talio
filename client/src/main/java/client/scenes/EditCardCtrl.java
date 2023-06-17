package client.scenes;

import client.NestedTask;
import client.Task;
import client.services.CardService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import commons.Card;
import commons.Subtask;
import commons.Tag;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Singleton
public class EditCardCtrl implements Initializable {
    private Card currentCard;
    private Task taskCtrl;
    private Stage stage;
    private ServerUtils server;
    private CardService cardService;

    @FXML
    private GridPane root;
    @FXML
    private TextField cardTitle;
    @FXML
    private TextArea cardDescription;
    @FXML
    private Label createdByLabel;
    @FXML
    private Label nestedTasksLabel;
    @FXML
    private ImageView addNestedTasksButtonView;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private VBox nestedTasksHolder;

    private List<NestedTask> nestedTasks;
    private boolean disableEscape;
    private boolean textFieldShown;

    @FXML
    private Button plusbutton;
    @FXML
    private FlowPane flowPane;

    private Stage editTagsStage;
    private VBox assignedTagsHolder;
    private VBox availableTagsHolder;


    /**
     * Constructor for the EditCard Controller
     * @param server the instance of the ServerUtils
     */
    @Inject
    public EditCardCtrl(ServerUtils server) {
        this.server = server;
    }

    /**
     * @param cardService the instance of the card service
     */
    public void setCardService(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Returns the AnchorPane that serves as the root of this controller
     * @return The root AnchorPane of this controller
     */
    public GridPane getRootElement() {
        return root;
    }

    /**
     * Temporarily disables exit on ESCAPE pressed
     * @param flag whether the exit should be disabled
     */
    public void disableEscape(boolean flag) {
        this.disableEscape = flag;
    }

    /**
     * Getter for the boolean attribute textFieldShown
     * @return the current value of textFieldShown
     */
    public boolean getTextFieldShown() {
        return textFieldShown;
    }

    /**
     * Setter for the bo boolean attribute textFieldShown
     * @param textFieldShown the new value of textFieldShown
     */
    public void setTextFieldShown(boolean textFieldShown) {
        this.textFieldShown = textFieldShown;
    }

    /**
     * @return the id of the card currently being edited or null if no card is being edited
     */
    public Long getCurrentId() {
        if (currentCard == null) return null;
        else return currentCard.id;
    }

    /**
     * Getter for the attribute nestedTasksHolder
     * @return VBox that contains the subtasks elements
     */
    public VBox getNestedTasksHolder() {
        return this.nestedTasksHolder;
    }

    /**
     * Initializes the controller
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
        root.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        escapeToCloseCardEdit();

        // handle subtasks
        Image addSubtasksButton = new Image("client/scenes/img/add.png");
        addNestedTasksButtonView.setImage(addSubtasksButton);

        Image pencil = new Image("client/scenes/img/pencil.png");
        ImageView pencilview = new ImageView(pencil);
        pencilview.setFitHeight(27);
        pencilview.setFitWidth(27);
        plusbutton.setGraphic(pencilview);
        plusbutton.setStyle("-fx-background-color: transparent");
    }

    /**
     * Activates the ESC keyboard shortcut for closing the edit card stage
     */
    public void escapeToCloseCardEdit() {
        final KeyCombination escapeKeyCombination = new KeyCodeCombination(KeyCode.ESCAPE);

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (escapeKeyCombination.match(event)) {
                // if we are currently creating / editing a nested task
                if (disableEscape) {
                    disableEscape = false;
                    return;
                }
                closeEditCardView();
                event.consume();
            }
        };
        root.addEventHandler(KeyEvent.KEY_RELEASED, keyEventEventHandler);
    }

    /**
     Saves the changes made to the task's attributes
     in the EditCard window and closes the window.
     @param event The ActionEvent object that is triggered
     when the "Save" button is clicked.
     This method updates the title and description of the
     currentCard object based on the user input
     in the EditCard window. Then, it calls the saveEditedCard()
     method with the ID of the task object
     to save the changes to the task repository. The saveEditedCard()
     method uses websockets to update the task
     repository and broadcast the changes to other clients connected
     to the same repository. Finally, it closes
     the EditCard window from the user.
     */
    public void saveAndHideEditCard(ActionEvent event) {
        currentCard.title = cardTitle.getText();
        currentCard.description = cardDescription.getText();
        cardService.saveEditedCard(currentCard.id,
                currentCard.title,
                currentCard.description);
        closeEditCardView();
    }

    /**
     * Loads the provided task's corresponding card information and populates the UI fields with the
     * card's title, description, and creator information.
     * @param task The task whose corresponding card information will be loaded.
     * @param stage The stage in which the edit card pop up is displayed
     */
    public void loadEditingCardAndStage(Task task, Stage stage) {
        this.stage = stage;
        this.taskCtrl = task;
        currentCard = server.getCardById(task.getId());
        cardTitle.setText(currentCard.title);
        cardDescription.setText(currentCard.description);
        createdByLabel.setText(currentCard.createdBy);

        nestedTasksHolder.getChildren().clear();
        nestedTasks = new ArrayList<>();
        Card card = server.getCardById(task.getId());
        if (card.totalSubtasks != -1) {
            nestedTasksLabel.setText("Add a new subtask");
            progressBar.setVisible(true);
            setProgress(card.doneSubtasks, card.totalSubtasks);
        } else progressBar.setVisible(false);
        for (Subtask st : server.getSubtasksByCardId(currentCard.id)) {
            addNestedTaskOnPosition(st, -1);
        }
        textFieldShown = disableEscape = false;

        flowPane.getChildren().remove(0, flowPane.getChildren().size() - 1);
        List<Tag> assignedTags = server.getAssignedTagsByCardID(getCurrentId());
        for (Tag tag : assignedTags) {
            flowPane.getChildren().add(0, createTagVisualForCardEdit(tag, true));
        }
    }

    /**
     * Method that is executed when the AddSubtaskButton is clicked
     */
    public void onClickAddSubtasksButton() {
        if (textFieldShown) return;
        if (nestedTasks.size() >= 10) return; // set a reasonable limit for the subtask number
        if (nestedTasksHolder.getChildren().size() != nestedTasks.size()) return;
        if (nestedTasksHolder.getChildren().size() == 0
                && !nestedTasksLabel.getText().equals("Add a new subtask")) {
            cardService.createNestedTaskList(getCurrentId());
            return;
        }
        stage.setHeight(stage.getHeight() + 30);
        NestedTask nt = new NestedTask(this, cardService);
        int index = nestedTasks.size();
        nestedTasksHolder.getChildren().add(nt.createNestedTaskDialog(true));
        textFieldShown = true;
    }

    /**
     * Adds a nested task on a given position
     * @param st the subtask data structure to create the nested task from
     * @param index the index on which the nested task should be placed or -1 to add at the end
     */
    public void addNestedTaskOnPosition(Subtask st, int index) {
        if (nestedTasksHolder.getChildren().size() == 10) terminateAddingNestedTask();
        stage.setHeight(stage.getHeight() + 30);
        NestedTask nt = new NestedTask(this, cardService);
        if (index != -1) {
            nestedTasksHolder.getChildren().add(index, nt.createNestedTaskElement(st));
            nestedTasks.add(index, nt);
        } else {
            nestedTasksHolder.getChildren().add(nt.createNestedTaskElement(st));
            nestedTasks.add(nt);
        }
        resetSubtaskIndexes();
    }

    /**
     * Terminates adding a nested task
     */
    public void terminateAddingNestedTask() {
        if (nestedTasksHolder.getChildren().size() == nestedTasks.size()) return;
        nestedTasksHolder.getChildren().remove(nestedTasks.size());
        stage.setHeight(stage.getHeight() - 30);
    }

    /**
     * Replaces a nested task element with a dialog or vice versa
     * @param index the index of the nested subtask
     */
    public void toggleNestedTaskElementAndDialog(int index) {
        if (nestedTasksHolder.getChildren().get(index) == nestedTasks.get(index).getRoot()) {
            nestedTasksHolder.getChildren().remove(index);
            nestedTasksHolder.getChildren().add(index,
                    nestedTasks.get(index).createNestedTaskDialog(false));
        } else {
            nestedTasksHolder.getChildren().remove(index);
            nestedTasksHolder.getChildren().add(index, nestedTasks.get(index).getRoot());
        }
    }

    /**
     * Method for adding a nested task list
     */
    public void addNestedTaskList() {
        nestedTasksLabel.setText("Add a new subtask");
        progressBar.setVisible(true);
    }

    /**
     * Method for setting the progress bar
     * @param doneSubtasks the number of subtasks which are done
     * @param totalSubtasks the total number of subtasks
     */
    public void setProgress(Integer doneSubtasks, Integer totalSubtasks) {
        if (totalSubtasks == 0) progressBar.setProgress(0);
        else progressBar.setProgress(1.0 * doneSubtasks / totalSubtasks);
    }

    /**
     * Method for updating a subtask
     * @param st the updated subtask
     */
    public void updateSubtask(Subtask st) {
        var elem = nestedTasks.get(st.index).createNestedTaskElement(st);
        nestedTasksHolder.getChildren().remove((int) st.index);
        nestedTasksHolder.getChildren().add(st.index, elem);
    }

    /**
     * Method for removing a subtask
     * @param st the removed subtask
     */
    public void removeSubtask(Subtask st) {
        int index = findSubtask(st);
        nestedTasksHolder.getChildren().remove(index);
        nestedTasks.remove(index);
        resetSubtaskIndexes();
        stage.setHeight(stage.getHeight() - 30);
    }

    private int findSubtask(Subtask st) {
        for (int i = 0; i < nestedTasks.size(); ++ i) {
            if (nestedTasks.get(i).getId() == st.id) {
                return i;
            }
        }
        return -1;
    }


    private void resetSubtaskIndexes() {
        int idx = 0;
        for (NestedTask nt : nestedTasks) {
            nt.resetIndex(idx++);
        }
    }

    /**
     * Method for relocation a subtask
     * @param st the subtask which was repositioned
     */
    public void relocateSubtask(Subtask st) {
        int index = findSubtask(st);
        textFieldShown = false;
        stage.setHeight(stage.getHeight() - 30);
        nestedTasks.remove(index);
        nestedTasksHolder.getChildren().remove(index);
        addNestedTaskOnPosition(st, st.index);
        resetSubtaskIndexes();
    }

    /**
     * Creates a visual representation for the tag and
     * how it looks like in both the flowpane and the vbox
     * @param tag the tag for which the visual representation is created
     * @param flag a flag, which determines whether the tagVisual is build for
     * the flowpane or the vbox
     * @return the stackpane which represents the visual representation
     */
    public StackPane createTagVisualForCardEdit(Tag tag, boolean flag) {
        StackPane stackPane = new StackPane();
        stackPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                if (stackPane.getParent() == assignedTagsHolder) {
                    cardService.removeTagFromCard(tag, getCurrentId());
                    editTagsStage.close();
                }
                if (stackPane.getParent() == availableTagsHolder) {
                    if (assignedTagsHolder.getChildren().size() >= 7) return;
                    cardService.addTagToCard(tag, getCurrentId());
                    editTagsStage.close();
                }
            }
        });

        Rectangle background = new Rectangle(150, 27);
        background.setFill(Paint.valueOf(tag.color));
        background.setArcWidth(5);
        background.setArcHeight(5);

        Label tagTitle = new Label(tag.name);
        tagTitle.setFont(Font.font("Arial", 13));
        tagTitle.setStyle("-fx-text-fill: #ffffff;");

        Text text = new Text(tag.name);
        text.setFont(tagTitle.getFont());
        double textWidth = text.getLayoutBounds().getWidth();
        if (flag) background.setWidth(textWidth + 10);
        background.setHeight(27);

        StackPane sp = new StackPane();
        sp.getChildren().addAll(background, tagTitle);
        StackPane.setAlignment(tagTitle, Pos.CENTER);

        stackPane.setId("tag{" + tag.id + "}");

        stackPane.getChildren().add(sp);
        return stackPane;
    }

    /**
     * Creates a new stage, which displays all the tags
     * in two different vboxes: the ones that are added
     * to the card and the ones that are added to the board
     * but not to the card
     * @param actionEvent the ActionEvent that triggers the method
     */
    public void showListView(ActionEvent actionEvent) {
        if (editTagsStage != null && editTagsStage.isShowing()) return;
        editTagsStage = new Stage();
        editTagsStage.initModality(Modality.WINDOW_MODAL);
        editTagsStage.initStyle(StageStyle.UTILITY);
        editTagsStage.setWidth(200);
        editTagsStage.setHeight(300);
        editTagsStage.setResizable(false);

        assignedTagsHolder = new VBox();
        assignedTagsHolder.setSpacing(5);
        availableTagsHolder = new VBox();
        availableTagsHolder.setSpacing(5);
        List<Tag> assignedTags = server.getAssignedTagsByCardID(getCurrentId());
        List<Tag> availableTags = server.getAvailableTagsByCardID(getCurrentId());

        Label assignedTagsLabel = new Label("Tags in card");
        assignedTagsLabel.setFont(Font.font("Arial", 15));
        StackPane assignedTagsLabelsp = new StackPane(assignedTagsLabel);
        assignedTagsLabel.setAlignment(Pos.CENTER);
        assignedTagsHolder.getChildren().add(assignedTagsLabelsp);
        for (Tag tag : assignedTags) {
            assignedTagsHolder.getChildren().add(createTagVisualForCardEdit(tag, false));
        }

        Label availableTagsLabel = new Label("Tags not in card");
        availableTagsLabel.setFont(Font.font("Arial", 15));
        StackPane availableTagsLabelsp = new StackPane(availableTagsLabel);
        availableTagsLabel.setAlignment(Pos.CENTER);
        availableTagsHolder.getChildren().add(availableTagsLabelsp);
        for (Tag tag : availableTags) {
            availableTagsHolder.getChildren().add(createTagVisualForCardEdit(tag, false));
        }

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.getChildren().add(assignedTagsHolder);
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 0, 0));
        vbox.getChildren().add(separator);
        vbox.getChildren().add(availableTagsHolder);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Scene scene = new Scene(scrollPane);
        editTagsStage.setScene(scene);
        editTagsStage.centerOnScreen();
        editTagsStage.showAndWait();
    }

    /**
     * Adds a tag visual to the flowpane
     * @param tag the tag that is being added
     */

    public void addTag(Tag tag) {
        StackPane visualTag = createTagVisualForCardEdit(tag, true);
        int tagsCnt = flowPane.getChildren().size();
        flowPane.getChildren().add(tagsCnt - 1, visualTag);
    }

    /**
     * Removes tag from the flowpane
     * @param tag the tag that is being removed
     */

    public void removeTag(Tag tag) {
        for (int i = 0; i < flowPane.getChildren().size() - 1; i++) {
            if (flowPane.getChildren().get(i).getId().equals("tag{" + tag.id + "}")) {
                flowPane.getChildren().remove(i);
                return;
            }
        }
    }

    /**
     * Edits the tag and its color and title
     * @param tag the tag that is being edited
     */
    public void editTag(Tag tag) {
        for (int i = 0; i < flowPane.getChildren().size() - 1; i++) {
            if (flowPane.getChildren().get(i).getId().equals("tag{" + tag.id + "}")) {
                StackPane sp = (StackPane) (((StackPane) flowPane.getChildren().get(i))
                        .getChildren().get(0));
                Rectangle background = (Rectangle) (sp.getChildren().get(0));
                Label title = (Label) (sp.getChildren().get(1));
                background.setFill(Paint.valueOf(tag.color));
                Text text = new Text(tag.name);
                text.setFont(title.getFont());
                double textWidth = text.getLayoutBounds().getWidth();
                background.setWidth(textWidth + 10);
                title.setText(tag.name);
                return;
            }
        }
    }

    /**
     * Closes the edit card view
     */
    public void closeEditCardView() {
        taskCtrl.closeEditCardStage();
        stage = null;
    }

    /**
     * Update card title and description
     * @param card the card to update with
     */
    public void updateBasics(Card card) {
        cardTitle.setText(card.title);
        cardDescription.setText(card.description);
    }
}