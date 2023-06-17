package client;

import client.scenes.*;
import com.google.inject.Singleton;
import javafx.scene.Parent;
import javafx.util.Pair;

@Singleton
public class FXMLLoader {
    private final MyFXML fxml;

    /**
     * Constructs the singleton instance of FXML Loader so that each class can use in to load fxml
     */
    public FXMLLoader() {
        this.fxml = Main.getFXML();
    }

    /**
     * @return A new instance of a new tab scene.
     */
    public Pair<NewTabCtrl, Parent> returnNewTabScene(){
        return fxml.load(NewTabCtrl.class, "client", "scenes", "NewTab.fxml");
    }

    /**
     * @return A new instance of a board scene.
     */
    public Pair<BoardCtrl, Parent> returnBoardScene(){
        return fxml.load(BoardCtrl.class, "client", "scenes", "Board.fxml");
    }

    /**
     * @return A new instance of the Preference Settings Scene
     */
    public Pair<PreferencesCtrl,Parent> returnPreferenceScene(){
        return  fxml.load(PreferencesCtrl.class,"client","scenes","Preferences.fxml");
    }

    /**
     * @return A new instance of the Preference Settings Scene
     */
    public Pair<PreferencesCtrl,Parent> returnKeyboardShortcutScene(){
        return  fxml.load(PreferencesCtrl.class,"client","scenes","KeyboardShortcuts.fxml");
    }

    /**
     * @return A new instance of the Popup Scene when the username is missing
     */
    public Pair<PopupCtrl,Parent> returnPopupScene(){
        return fxml.load(PopupCtrl.class,"client","scenes","UsernamePopup.fxml");
    }

    /**
     * @return A new instance of the Password popup when the board requires a password
     */
    public Pair<PasswordPopupCtrl,Parent> returnPasswordPopupScene(){
        return fxml.load(PasswordPopupCtrl.class,"client","scenes","PasswordPopup.fxml");
    }

    /**
     * Uses the FXML loader to create a new instance of the RecentBoard class
     * @return A new instance of the Recent Board Scene
     */
    public Pair<RecentBoardCtrl, Parent> returnRecentBoard(){
        return fxml.load(RecentBoardCtrl.class,"client","scenes","RecentBoard.fxml");
    }

    /**
     * Uses the FXML loader to create a new instance of the EditCard class
     * @return A new instance of the EditCard Scene
     */
    public Pair<EditCardCtrl, Parent> returnEditCard(){
        return fxml.load(EditCardCtrl.class,"client","scenes","EditCard.fxml");
    }

    /**
     * Uses the FXML loader to create a new instance of the NewCard class
     * @return A new instance of the NewCard Scene
     */
    public Pair<NewCardCtrl, Parent> returnNewCard(){
        return fxml.load(NewCardCtrl.class,"client","scenes","NewCard.fxml");
    }

    /**
     * Uses the FXML loader to create a new instance of the BoardSettings class
     * @return A new instance of the Board Settings Scene
     */
    public Pair<BoardSettingsCtrl, Parent> returnBoardSettings(){
        return fxml.load(BoardSettingsCtrl.class,"client","scenes","BoardSettings.fxml");
    }

    /**
     * Uses the FXML loader to create a new instance of the EditTags class
     * @return A new instance of the Edit Tags Scene
     */
    public Pair<EditTagsCtrl,Parent> returnEditTags(){
        return fxml.load(EditTagsCtrl.class,"client","scenes","EditTags.fxml");
    }


}
