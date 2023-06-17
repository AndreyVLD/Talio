package client;

import client.scenes.EditCardCtrl;
import client.services.CardService;
import commons.Status;
import commons.Subtask;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class NestedTask {
    private Subtask subtask;
    private EditCardCtrl parent;
    private CardService cardService;
    private HBox root;
    private int index;
    private boolean dragging = false;

    /**
     * Constructs a Nested Task
     * @param parent the edit card controller which initialized the nested task
     * @param cs the card service which provides functionality for editing a card
     */
    public NestedTask(EditCardCtrl parent, CardService cs) {
        this.parent = parent;
        this.cardService = cs;
    }

    /**
     * @return the root element
     */
    public HBox getRoot() {
        return root;
    }

    /**
     * @return the id of the subtask
     */
    public Long getId() {
        return subtask.id;
    }

    /**
     * Resets the subtask index
     * @param idx the new index
     */
    public void resetIndex(int idx) {
        subtask.index = idx;
//        CheckBox cb = (CheckBox)((StackPane)root.getChildren().get(0)).getChildren().get(0);
//        Label tl = (Label) ((StackPane)root.getChildren().get(1)).getChildren().get(0);
//        ImageView tv = (ImageView)((StackPane)root.getChildren().get(2)).getChildren().get(0);
//        assignElementActions(cb, tl, tv);
    }

    /**
     * Creates the root element of the nested task
     * @param subtask the subtask to create the nested task from
     * @return the newly crated root element
     */
    public HBox createNestedTaskElement(Subtask subtask) {
        this.subtask = subtask;

        root = new HBox();
        root.setPrefHeight(30);

        StackPane[] align = new StackPane[3];
        align[0] = new StackPane(); align[1] = new StackPane(); align[2] = new StackPane();
        align[0].setPadding(new Insets(1, 0, 0, 13));
        align[1].setPadding(new Insets(0, 13, 0, 15));
        align[2].setPadding(new Insets(0, 0, 0, 0));

        CheckBox checkBox = new CheckBox();
        if (subtask.status.equals(Status.DONE)) checkBox.setSelected(true);
        align[0].getChildren().add(checkBox);

        Label titleLabel = new Label(subtask.title);
        titleLabel.setPrefWidth(370);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setTextFill(Color.GRAY);
        align[1].getChildren().add(titleLabel);

        ImageView trashView = new ImageView();
        Image trashImg = new Image("client/scenes/img/trash.png");
        trashView.setImage(trashImg);
        align[2].getChildren().add(trashView);

        assignElementActions(checkBox, titleLabel, trashView);

        root.getChildren().addAll(align);
        return root;
    }

    /**
     * Assign actions when creating a new card dialog
     * @param checkBox the CheckBox for marking the subtask as planned / done
     * @param titleLabel the titleLabel containing the title
     * @param trashView the ImageView containing the trash button
     */
    public void assignElementActions(CheckBox checkBox, Label titleLabel, ImageView trashView) {
        checkBox.selectedProperty().addListener((k, p, n) -> {
            if (n) subtask.status = Status.DONE;
            else subtask.status = Status.PLANNED;
            cardService.editSubtask(subtask);
        });
        titleLabel.setOnMouseClicked(event -> {
            if (parent.getTextFieldShown()) return;
            parent.toggleNestedTaskElementAndDialog(subtask.index);
            parent.disableEscape(true);
            parent.setTextFieldShown(true);
        });
        trashView.setOnMouseClicked(event -> {
            cardService.deleteSubtask(subtask);
        });
        
        root.setOnMouseDragged(this::onMouseDragged);
        root.setOnMouseReleased(this::onMouseReleased);
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        parent.setTextFieldShown(true);

        dragging = true;
        mouseEvent.consume();
        VBox subtasks = parent.getNestedTasksHolder();

        double top = subtasks.localToScene(subtasks.getBoundsInLocal()).getMinY();
        double distToTop = mouseEvent.getSceneY() - top;
        double translation = distToTop - root.getLayoutY() - 15;
        translation = Math.max(translation, -root.getLayoutY());
        translation = Math.min(translation, subtasks.getHeight() - root.getLayoutY() - 30);
        root.setTranslateY(translation);

        index = (int) (distToTop / 30);
        index = Math.max(0, index);
        index = Math.min(index, subtasks.getChildren().size() - 1);

        for (int i = subtask.index + 1; i < subtasks.getChildren().size(); ++ i) {
            if (i <= index) {
                subtasks.getChildren().get(i).setTranslateY(-30);
            } else {
                subtasks.getChildren().get(i).setTranslateY(0);
            }
        }
        for (int i = 0; i < subtask.index; ++ i) {
            if (i >= index) {
                subtasks.getChildren().get(i).setTranslateY(+30);
            } else {
                subtasks.getChildren().get(i).setTranslateY(0);
            }
        }
    }

    private void onMouseReleased(MouseEvent mouseEvent) {
        if (!dragging) return;
        mouseEvent.consume();
        VBox subtasks = parent.getNestedTasksHolder();

        for (int i = 0; i < subtasks.getChildren().size(); ++ i) {
            subtasks.getChildren().get(i).setTranslateY(0);
        }

        subtask.index = index;
        cardService.relocateSubtask(subtask);
        dragging = false;
    }

    /**
     * Creates a dialog with similar design to the root element which allows to enter a title
     * @param flag whether it will be used for creating a new or editing an existing nested task
     * @return the newly created dialog
     */
    public HBox createNestedTaskDialog(boolean flag) {
        parent.disableEscape(true);

        HBox dialog = new HBox();
        dialog.setPrefHeight(30);

        StackPane[] align = new StackPane[3];
        align[0] = new StackPane(); align[1] = new StackPane(); align[2] = new StackPane();
        align[0].setPadding(new Insets(0, 0, 0, 10));
        align[1].setPadding(new Insets(0, 10, 0, 10));
        align[2].setPadding(new Insets(0, 0, 0, 0));

        ImageView saveView = new ImageView();
        Image saveImg = new Image("client/scenes/img/save.png");
        saveView.setImage(saveImg);
        align[0].getChildren().add(saveView);

        TextField titleField = new TextField();
        titleField.setPrefWidth(375);
        titleField.setFont(Font.font("Arial", 13));
        titleField.setStyle("-fx-text-fill:gray;");
        if (subtask != null) titleField.setText(subtask.title);
        align[1].getChildren().add(titleField);

        ImageView trashView = new ImageView();
        Image trashImg = new Image("client/scenes/img/trash.png");
        trashView.setImage(trashImg);
        align[2].getChildren().add(trashView);

        if (flag) assignNewActions(saveView, titleField, trashView);
        else assignEditActions(saveView, titleField, trashView);

        Platform.runLater(() -> titleField.requestFocus());

        dialog.getChildren().addAll(align);
        return dialog;
    }

    /**
     * Assign actions when creating a new card dialog
     * @param saveView the ImageView containing the save button
     * @param titleField the TextField containing the title
     * @param trashView the ImageView containing the trash button
     */
    public void assignNewActions(ImageView saveView, TextField titleField, ImageView trashView) {
        saveView.setOnMouseClicked(event -> {
            parent.terminateAddingNestedTask();
            Subtask st = new Subtask();
            st.title = titleField.getText();
            cardService.createSubtask(st, parent.getCurrentId());
            parent.disableEscape(false);
            parent.setTextFieldShown(false);

        });
        trashView.setOnMouseClicked(event -> {
            parent.terminateAddingNestedTask();
            parent.disableEscape(false);
            parent.setTextFieldShown(false);
        });
        titleField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                parent.terminateAddingNestedTask();
                Subtask st = new Subtask();
                st.title = titleField.getText();
                cardService.createSubtask(st, parent.getCurrentId());
                parent.disableEscape(false);
                parent.setTextFieldShown(false);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                parent.terminateAddingNestedTask();
                parent.setTextFieldShown(false);
            }
        });
    }

    /**
     * Assign actions when creating a new card dialog
     * @param saveView the ImageView containing the save button
     * @param titleField the TextField containing the title
     * @param trashView the ImageView containing the trash button
     */
    public void assignEditActions(ImageView saveView, TextField titleField, ImageView trashView) {
        saveView.setOnMouseClicked(event -> {
            parent.toggleNestedTaskElementAndDialog(subtask.index);
            subtask.title = titleField.getText();
            cardService.editSubtask(subtask);
            parent.disableEscape(false);
            parent.setTextFieldShown(false);
        });
        trashView.setOnMouseClicked(event -> {
            parent.toggleNestedTaskElementAndDialog(subtask.index);
            parent.disableEscape(false);
            parent.setTextFieldShown(false);
        });
        titleField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                parent.toggleNestedTaskElementAndDialog(subtask.index);
                subtask.title = titleField.getText();
                cardService.editSubtask(subtask);
                parent.disableEscape(false);
                parent.setTextFieldShown(false);

            } else if (event.getCode() == KeyCode.ESCAPE) {
                parent.toggleNestedTaskElementAndDialog(subtask.index);
                parent.setTextFieldShown(false);
            }
        });
    }
}
