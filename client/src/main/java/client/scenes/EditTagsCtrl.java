package client.scenes;

import client.services.CardService;
import client.utils.ConfigUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Status;
import commons.Tag;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditTagsCtrl implements Initializable {

    private final MainCtrl mainCtrl;

    private final ConfigUtils configUtils;
    private ServerUtils server;

    private BaseCtrl baseCtrl;

    private CardService cardService;

    @FXML
    private TextField titleTextField;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button addButton;

    @FXML
    private StackPane stackpaneaddtag;

    @FXML
    private VBox tagHolder;

    private String prevText = "";
    private Stage editOrCreateTagStage;

    /**
     *
     * @param server - Instance of server
     * @param mainCtrl - Instance of mainCtrl
     * @param configUtils - Instance of the configuration file helper method
     * @param cardService - instance of CardService
     * @param baseCtrl - Instance of baseCtrl
     */
    @Inject
    public EditTagsCtrl(ServerUtils server, MainCtrl mainCtrl,
                        ConfigUtils configUtils, CardService cardService, BaseCtrl baseCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configUtils = configUtils;
        this.cardService = cardService;
        this.baseCtrl = baseCtrl;
    }

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
        List<Tag> tags = server.getNotDeletedTagsByBoardId(getBoardID());
        for (Tag tag : tags)
        {
            tagHolder.getChildren().add(0, createTagVisual(tag));
        }
        stackpaneaddtag.setOnMouseClicked(event -> {
            if (event.getClickCount()==2)
            {
                addTag();;
            }
        });

    }

    /**
     * Displays a stage for adding a new tag, where the user can enter
     * the desired title and color for the tag
     * Then, a new tag is created as well as a visual representation for it
     * if the textfield is empty, a new tag won't be created
     */
    public void addTag() {
        if (editOrCreateTagStage != null && editOrCreateTagStage.isShowing()) return;
        else editOrCreateTagStage = new Stage();
        editOrCreateTagStage.setTitle("Add New Tag");
        editOrCreateTagStage.initStyle(StageStyle.UTILITY);

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(5, 0, 0, 0));
        vbox.setPrefSize(200,160);

        Label titleLabel = new Label("Set Tag Title");
        titleLabel.setFont(Font.font("Arial", 14));
        TextField textField = new TextField();
        prevText = "";
        textField.setOnKeyTyped(event -> {
            String text = textField.getText();
            Text t = new Text(text); t.setFont(new Font("Arial", 13));
            if (t.getLayoutBounds().getWidth() > 120) textField.setText(prevText);
            else  prevText = textField.getText();
        });
        textField.setMaxSize(160, 20);
        Label colorLabel = new Label("Set Tag Color");
        colorLabel.setFont(Font.font("Arial", 14));
        ColorPicker cp = new ColorPicker(Color.BLUE);
        Button addButton = new Button("DONE");
        addButton.setPrefSize(200, 30);
        addButton.setStyle("-fx-background-color:#308FFF ; -fx-text-fill: #ffffff;");
        addButton.setOnAction(event -> {
            if(!textField.getText().isEmpty()) {
                Tag tag = new Tag(textField.getText(), String.valueOf(cp.getValue()));
                tag.status = Status.ACTIVE;
                tag.board = server.getBoard(getBoardID());
                Tag newtag =server.createTag(tag);
                tagHolder.getChildren().add(0, createTagVisual(newtag));
                editOrCreateTagStage.close();
            }
            else {
                editOrCreateTagStage.close();
            }
        });

        vbox.getChildren().addAll(titleLabel, textField, colorLabel, cp, addButton);

        Scene scene = new Scene(vbox);
        editOrCreateTagStage.setScene(scene);
        editOrCreateTagStage.centerOnScreen();
        editOrCreateTagStage.show();
    }

    /**
     * Creates a visual representation of a tag
     * @param tag The tag which the visual representation will be created for
     * @return A stackpane representing the tag
     */

    public StackPane createTagVisual(Tag tag) {
        StackPane tagVisual = new StackPane();
        tagVisual.setPadding(new Insets(5, 0, 5, 0));

        Rectangle background = new Rectangle(260, 35);
        background.setId("background");
        background.setFill(Paint.valueOf(tag.color));
        background.setArcWidth(20);
        background.setArcHeight(20);

        // Create the title label
        Label tagTitle = new Label(tag.name);
        tagTitle.setId("label");
        tagTitle.setFont(Font.font("Arial", 16));
        tagTitle.setStyle("-fx-text-fill: #ffffff;");
        tagTitle.setPadding(new Insets(0,0,0,15));


        // Add a delete button
        Button deleteTag = new Button("X");
        extractedDeleteTag(tag, tagVisual, deleteTag);

        Button editTag = new Button();
        extractedEditTag(tag, tagVisual, editTag);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(editTag, deleteTag);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(0, 0, 0, 20));

        StackPane sp = new StackPane();
        sp.getChildren().addAll(background,tagTitle, hbox);
        StackPane.setAlignment(tagTitle, Pos.CENTER_LEFT);

        tagVisual.getChildren().add(sp);
        return tagVisual;
    }

    /**
     *
     * @param tag
     * @param tagVisual
     * @param editTag
     */

    private void extractedEditTag(Tag tag, StackPane tagVisual, Button editTag) {
        Image paintPalette = new Image("client/scenes/img/paint-palette.png");
        ImageView paletteImageView = new ImageView(paintPalette);
        Image paintPaletteDark = new Image("client/scenes/img/paint-palette-dark.png");
        ImageView paletteImageViewDark = new ImageView(paintPaletteDark);
        paletteImageView.setFitHeight(25);
        paletteImageView.setFitWidth(25);
        paletteImageViewDark.setFitHeight(25);
        paletteImageViewDark.setFitWidth(25);
        editTag.setGraphic(paletteImageView);
        editTag.setStyle("-fx-background-color:transparent; -fx-border-color:transparent;");
        editTag.setOnMouseEntered(event ->
                editTag.setGraphic(paletteImageViewDark));
        editTag.setOnMouseExited(event ->
                editTag.setGraphic(paletteImageView));
        editTag.setOnAction(event -> {
            int index = tagHolder.getChildren().indexOf(tagVisual);
            editTag(tag, index);
        });
    }

    private void extractedDeleteTag(Tag tag, StackPane tagVisual, Button deleteTag) {
        deleteTag.setPrefWidth(15);
        deleteTag.setPrefHeight(15);
        deleteTag.setFont(Font.font("Arial", 12));
        deleteTag.setStyle("-fx-background-color: transparent; "
                + "-fx-border-color: transparent; -fx-text-fill: White;");
        deleteTag.setOnMouseEntered(event ->
                deleteTag.setStyle("-fx-background-color:transparent;"
                        + " -fx-border-color:transparent;-fx-text-fill: black;"));
        deleteTag.setOnMouseExited(event ->
                deleteTag.setStyle("-fx-background-color:transparent;"
                        + " -fx-border-color: transparent; -fx-text-fill: White;"));
        deleteTag.setOnAction(event -> {
            server.deleteTagById(tag.id);
            tagHolder.getChildren().remove(tagVisual);

        });
    }

    /**
     * Retrieves the ID of the currently selected board
     * @return the ID of the board
     */
    public Long getBoardID()
    {
        TabPane tabs = baseCtrl.getTabPane();
        return Long.parseLong(tabs.getSelectionModel().getSelectedItem()
                .getContent().getStyleClass().get(0));
    }

    /**
     * Displays a JavaFX stage for editting a Tag object
     * @param tag The tag that is eddited
     * @param index The index of the tag in the vbox
     */

    public void editTag(Tag tag, int index) {
        if (editOrCreateTagStage != null && editOrCreateTagStage.isShowing()) return;
        else editOrCreateTagStage = new Stage();
        editOrCreateTagStage.setTitle("Edit Tag");

        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(5, 0, 0, 0));
        vbox.setPrefSize(200,150);

        Label titleLabel = new Label("Update Tag Title");
        titleLabel.setFont(Font.font("Arial", 14));
        TextField textField = new TextField(tag.name);
        prevText = tag.name;
        textField.setOnKeyTyped(event -> {
            String text = textField.getText();
            Text t = new Text(text); t.setFont(new Font("Arial", 13));
            if (t.getLayoutBounds().getWidth() > 120) textField.setText(prevText);
            else  prevText = textField.getText();
        });
        textField.setMaxSize(160, 20);
        tag.name = textField.getText();

        Label colorLabel = new Label("Update Tag Color");
        colorLabel.setFont(Font.font("Arial", 14));
        ColorPicker cp = new ColorPicker(Color.valueOf(tag.color));
        tag.color = cp.getValue().toString();

        Button saveButton = new Button("SAVE");
        saveButton.setPrefSize(200, 25);
        saveButton.setStyle("-fx-background-color:#308FFF ; -fx-text-fill: #ffffff;");
        saveButton.setOnAction(event -> {
            if (!textField.getText().isEmpty()) {
                server.updateTag(tag.id, textField.getText(), cp.getValue().toString());
                StackPane newstackpane = (StackPane) tagHolder.getChildren().get(index);
                Label newlabel = (Label) newstackpane.lookup("#label");
                newlabel.setText(textField.getText());
                tag.name = textField.getText();
                tag.color = cp.getValue().toString();
                Rectangle background = (Rectangle) newstackpane.lookup("#background");
                background.setFill(cp.getValue());
                editOrCreateTagStage.close();}
            else editOrCreateTagStage.close();
        });

        vbox.getChildren().addAll(titleLabel, textField, colorLabel, cp, saveButton);
        Scene scene = new Scene(vbox); editOrCreateTagStage.setScene(scene);
        editOrCreateTagStage.centerOnScreen(); editOrCreateTagStage.show();
    }

}
