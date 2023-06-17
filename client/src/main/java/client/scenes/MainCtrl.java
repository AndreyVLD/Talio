/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.FXMLLoader;
import client.utils.ConfigUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainCtrl {
    private final ConfigUtils configUtils;
    private final FXMLLoader fxml;
    private final ServerUtils server;
    private Stage primaryStage;
    private Stage secondaryStage;
    private Stage boardSettings;
    private Scene base;

    /**
     * Constructor for the MainCtrl where we Inject configUtils
     * @param configUtils - injected instance of ConfigUtils
     * @param fxml - injected instance of FXMLLoader
     * @param server - injected instance of ServerUtils
     */
    @Inject
    public MainCtrl(ConfigUtils configUtils, FXMLLoader fxml, ServerUtils server){
        this.configUtils = configUtils;
        this.fxml = fxml;
        this.server = server;
    }

    /**
     * Method for initialising all the scenes
     * @param primaryStage - the primary stage
     * @param base - the main window of our application
     */
    public void initialize(Stage primaryStage, Pair<BaseCtrl, Parent> base) {
        this.primaryStage = primaryStage;

        this.base = new Scene(base.getValue());
        this.secondaryStage = new Stage();

        primaryStage.setTitle("Planner");
        primaryStage.setScene(this.base);

        primaryStage.show();
        checkName();
        setupKeyboardShortcut();
    }

    /**
     * General method that initializes all shortcuts
     */
    public void setupKeyboardShortcut() {
        questionMarkShortCut();
    }

    /**
     * When question mark is typed, it runs the action that opens the shortcut stage.
     * It must be 'SHIFT + /' in order to get '?'
     */
    public void questionMarkShortCut() {
        final KeyCombination keyCombination =
                new KeyCharacterCombination("?", KeyCombination.SHIFT_DOWN);
        final Runnable action = this::shortcutsPage;

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (keyCombination.match(event)) {
                action.run();
                event.consume();
            }
        };

        final Scene scene = primaryStage.getScene();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEventEventHandler);
    }

    /**
     * Opens the stage with the keyboard shortcuts
     */
    private void shortcutsPage() {
        System.out.println("Keyboard Shortcut : Question mark");
        shortcutsStage();
        secondaryStage.show();
    }

    /**
     * Method for creating a secondary Stage meant for the Preferences menu.
     */
    public void secondaryStage(){
        this.secondaryStage = new Stage();
        secondaryStage.setTitle("Preferences");
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        secondaryStage.initOwner(primaryStage);
        secondaryStage.setX(primaryStage.getX()+200);
        secondaryStage.setY(primaryStage.getY()+100);
        secondaryStage.setMinHeight(400);
        secondaryStage.setMinWidth(330);
        Pair<PreferencesCtrl,Parent> newScenePair = fxml.returnPreferenceScene();
        secondaryStage.setScene(new Scene(newScenePair.getValue()));
    }

    /**
     * The Help stage menu with user guidance
     */
    public void helpStage() {
        this.secondaryStage = new Stage();
        secondaryStage.setTitle("Help");
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        secondaryStage.initOwner(primaryStage);
        secondaryStage.setX(primaryStage.getX() + 200);
        secondaryStage.setY(primaryStage.getY() + 100);
        secondaryStage.setMinHeight(400);
        secondaryStage.setMinWidth(300);
        secondaryStage.setResizable(false);
        try{
            Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("Help.fxml"));
            secondaryStage.setScene(new Scene(root));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Getter for the secondary Stage
     * @return - the secondary Stage
     */
    public Stage getSecondaryStage() {
        return secondaryStage;
    }

    /**
     * Method for creating a secondary Stage meant for the Preferences menu.
     */
    public void shortcutsStage(){
        this.secondaryStage = new Stage();
        secondaryStage.setTitle("Keyboard Shortcuts");
        secondaryStage.setX(primaryStage.getX());
        secondaryStage.setY(primaryStage.getY());
        secondaryStage.setMinHeight(400);
        secondaryStage.setMinWidth(330);
        secondaryStage.setX(primaryStage.getX()+200);
        secondaryStage.setY(primaryStage.getY()+100);
        Pair<PreferencesCtrl,Parent> newScenePair = fxml.returnKeyboardShortcutScene();
        secondaryStage.setScene(new Scene(newScenePair.getValue()));
    }

    /**
     * Stage for when the username is missing.
     */
    public void usernamePopup() {
        this.secondaryStage = new Stage();
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        secondaryStage.initOwner(primaryStage);
        secondaryStage.centerOnScreen();
        Pair<PopupCtrl, Parent> newScenePair = fxml.returnPopupScene();
        Scene scene = new Scene(newScenePair.getValue());
        scene.setFill(Color.TRANSPARENT);
        secondaryStage.setScene(scene);
        secondaryStage.setResizable(false);
        secondaryStage.initStyle(StageStyle.UNDECORATED);
        secondaryStage.initStyle(StageStyle.TRANSPARENT);
    }

    /**
     * Function for showing the password input popup for a protected board
     * @param boardId - the id of the board to pass to the popup
     * @param parent - the parent Tab reference for making it possible to
     *               load a board from the popup submit button in the main view
     * @param isChangePassword - Whether the popup needs to be a changed
     *                         board password
     */
    public void showPasswordPopup(Long boardId, Tab parent, boolean isChangePassword) {
        this.secondaryStage = new Stage();
        secondaryStage.initModality(Modality.WINDOW_MODAL);
        secondaryStage.initOwner(primaryStage);
        secondaryStage.centerOnScreen();
        Pair<PasswordPopupCtrl, Parent> newScenePair = fxml.returnPasswordPopupScene();
        Scene scene = new Scene(newScenePair.getValue());
        scene.setFill(Color.TRANSPARENT);
        secondaryStage.setScene(scene);
        secondaryStage.setResizable(false);
        secondaryStage.initStyle(StageStyle.UNDECORATED);
        secondaryStage.initStyle(StageStyle.TRANSPARENT);

        PasswordPopupCtrl controller = newScenePair.getKey();
        controller.setBoardId(boardId);
        controller.setParent(parent);
        if (isChangePassword) {
            controller.convertToChangePasswordPopup();
        }
        secondaryStage.show();
    }

    /**
     * Checks the existence of the username in the Config File
     */
    public void checkName() {
        Map<String, Object> map;
        try {
            map = configUtils.load();
        } catch (IOException e) {
            map = new HashMap<>();
            map.put("portTextField", "8080");
            map.put("serverTextField", "localhost");
            map.put("colorPicker", "0xffffffff");
            map.put("usernameTextField", "");
            configUtils.save(map);
        }
        String username = (String) map.get("usernameTextField");
        if (username == null || username.isEmpty()) {
            usernamePopup();
            secondaryStage.show();
        }
    }

    /**
     * Shows a popup with an acknowledge button.
     * @param title The title of the popup.
     * @param message The message that the popup will display
     */
    public void createPopUp(String title, String message){
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.setStyle("-fx-background-color: grey; -fx-alignment: center; -fx-padding: 10");
        Text text = new Text(message);
        text.setStyle("-fx-font-size: 18");
        Button okButton = new Button("Ok");
        okButton.setStyle("-fx-background-color: darkgrey; -fx-font-size: 15;");
        okButton.setOnAction(event -> {
            dialog.close();
        });
        dialogVbox.getChildren().add(text);
        dialogVbox.getChildren().add(okButton);
        Scene dialogScene = new Scene(dialogVbox);
        dialog.setTitle(title);
        dialog.setAlwaysOnTop(true);
        dialog.setResizable(false);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * Show the BoardSettings scene in the boardSettings stage
     */
    public void showBoardPreferences(){
        boardSettings = new Stage();
        boardSettings.setTitle("Edit board");
        boardSettings.initOwner(primaryStage);
        boardSettings.setScene(new Scene(fxml.returnBoardSettings().getValue()));
        boardSettings.show();
    }

    /**
     * Close the boardSettings menu by closing the stage
     */
    public void closeBoardPreferences() {
        boardSettings.close();
    }

    /**
     * Method for creating a secondary Stage meant for the Tags menu.
     */
    public void tagsStage(){
        this.secondaryStage = new Stage();
        secondaryStage.setTitle("Tags");
        secondaryStage.setX(primaryStage.getX());
        secondaryStage.setY(primaryStage.getY());
        secondaryStage.initModality(Modality.APPLICATION_MODAL);
        secondaryStage.setMinHeight(400);
        secondaryStage.setMinWidth(280);
        Pair<EditTagsCtrl,Parent> newScenePair = fxml.returnEditTags();
        secondaryStage.setScene(new Scene(newScenePair.getValue()));
    }

    /**
     * Terminates long pollinh
     */
    public void stopLongPolling() {
        server.stopLongPolling();
    }
}