package client.scenes;

import client.FXMLLoader;
import client.services.BoardService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.beans.Observable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class BaseCtrl implements Initializable {
    private final MainCtrl mainCtrl;
    private final BoardSettingsCtrl boardSettingsCtrl;
    private final BoardService boardService;
    private final ServerUtils server;
    private final FXMLLoader fxml;

    @FXML
    private MenuItem boardSettings;
    @FXML
    private TabPane tabs;
    @FXML
    private MenuBar menu;

    /**
     * Constructor for the base controller
     * @param mainCtrl - Instance of the main control.
     * @param boardSettingsCtrl - Instance of the boardSettingsCtrl
     * @param server - Instance of serverutils class.
     * @param boardService - Instance of boardService
     * @param fxml - Instance of the fxml loader.
     */
    @Inject
    public BaseCtrl(MainCtrl mainCtrl, BoardSettingsCtrl boardSettingsCtrl,
                    ServerUtils server, BoardService boardService, FXMLLoader fxml) {
        this.mainCtrl = mainCtrl;
        this.boardSettingsCtrl = boardSettingsCtrl;
        this.server = server;
        this.boardService = boardService;
        this.fxml = fxml;
    }

    /**
     * Initialization of the base ctrl scene.
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tabs.getStylesheets().add(getClass().getResource("stylestab.css").toExternalForm());
        menu.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        tabs.getSelectionModel().selectedItemProperty().addListener(this::changeSelectedBoardId);
    }

    /**
     * Event for when the tab gets changed/loaded, it will parse the boardId
     * from a css rule, and pass it to the boardSettings (this workaround is needed
     * because there can be multiple board instances with different id's open in
     * tabs, whereas there is only one boardSettings menu item
     * @param observable
     */
    public void changeSelectedBoardId(Observable observable){
        try{
            long boardId = Long.parseLong(tabs.getSelectionModel().getSelectedItem()
                    .getContent().getStyleClass().get(0));
            boardSettings.setDisable(false);
            boardSettingsCtrl.setBoardId(boardId);
        } catch (Exception e){
            boardSettings.setDisable(true);
        }
    }

    /**
     * Creates a new tab when clicking the plus in the top left corner
     * This tab shows the initial page with the option to add boards or join them
     */
    public void addTab(){
        // Create a new tab
        Tab newTab = new Tab("New tab");
        tabs.getTabs().add(tabs.getTabs().size() - 1, newTab);
        tabs.getSelectionModel().select(newTab);
        Pair<NewTabCtrl, Parent> newTabScene = fxml.returnNewTabScene();
        newTabScene.getKey().setParent(newTab);
        newTab.setContent(newTabScene.getValue());
    }

    /**
     * @param tab the tab to be closed.
     */
    public void closeTab(Tab tab){
        tabs.getTabs().remove(tab);
    }

    /**
     * Creates a new Stage(pop-up Window) with the preferences settings in it
     */
    public void preferences(){
        // Create new window for the preferences
        mainCtrl.secondaryStage();
        mainCtrl.getSecondaryStage().show();
    }

    /**
     * Creates a new Stage (pop-up Window) with helping!
     */
    public void help(){
        mainCtrl.helpStage();
        mainCtrl.getSecondaryStage().show();
    }

    /**
     * Show the board settings screen
     */
    public void showBoardSettings(){
        mainCtrl.showBoardPreferences();
    }

    /**
     * Terminates the application whenever you click on the file - exit menu item
     */
    public void terminateApplication() {
        Platform.exit();
    }

    /**
     * Closes the current tab when clicking the file - close tab menu item
     */
    public void closeCurrentTab() {
        Tab curr = tabs.getSelectionModel().getSelectedItem();
        if (curr != null) {
            System.out.println(curr.getContent().getClass());
//            boardService.closeBoard(id);
//            tabs.getTabs().remove(curr);
        }
        //TODO how to retrieve board id from tab
    }

    /**
     * Creates a new Stage(pop-up Window) with the keyboard shortcuts in it
     */
    public void shortcuts(){
        mainCtrl.shortcutsStage();
        mainCtrl.getSecondaryStage().show();
    }

    /**
     * Getter for tabs
     * @return Tabs
     */
    public TabPane getTabPane() {
        return tabs;
    }

    /**
     * Creates a new Stage for the tags.
     * If a board is not selected, it creates a warning pop up
     */
    public void tags()
    {
        try {
            mainCtrl.tagsStage();
            mainCtrl.getSecondaryStage().show();
        }
        catch (Exception e) {
            mainCtrl.createPopUp("Board Not selected",
                    "Please open a board before accessing the Tags menu");
        }
    }
}
