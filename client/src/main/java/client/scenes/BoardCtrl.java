package client.scenes;

import client.BoardList;
import client.Task;
import client.services.BoardService;
import client.services.CardService;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Board;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BoardCtrl implements Initializable{
    private Long id;
    private BoardService boardService;
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final BaseCtrl baseCtrl;

    @FXML
    private HBox listHolder;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private StackPane editPopUp;
    @FXML
    private Button addListButton;

    @FXML
    private StackPane colorPickerStackPane;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private ScrollPane scrollPane;

    private final ArrayList<BoardList> lists = new ArrayList<>();
    private int hoverIndex = -1;
    private int hoverListIndex = -1;
    private boolean firstTimePressing = true;
    private String hexColor = "";
    private List<Long> cardIds;
    private boolean colorPickerActive = false;
    private CardService cardService;
    private int h1 = -1;
    private int h2 = -1;


    /**
     * @param server The instance of the ServerUtils
     * @param mainCtrl The instance of the MainCtrl
     * @param baseCtrl The instance of the BaseCtrl
     * @param bs Instance of the BoardService which provides functionality for modifying the board
     * @param cardService The instance of the CardService
     */
    @Inject
    public BoardCtrl(ServerUtils server, MainCtrl mainCtrl, BaseCtrl baseCtrl, BoardService bs,
                     CardService cardService) {
        this.mainCtrl = mainCtrl;
        this.baseCtrl = baseCtrl;
        this.server = server;
        this.boardService = bs;
        this.cardService = cardService;
    }

    /**
     * Makes the edit card pop-up not visible and links the stylesheet
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        editPopUp.setVisible(false);
        editPopUp.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        scrollPane.requestFocus();

        addKeyboardShortcutToControlHover();
        addKeyboardShortcutToSwitchOrderOfTasks();
        colorPickerStackPane.setVisible(false);
        scrollPane.setOnMouseClicked(this::makeColorPickerDisappearOnClick);
    }

    /**
     * @param coordinates coordinates of the card the click happened
     * @return hex value of the color picked
     * This method uses a completableFuture so the value
     * of the chosen color can be saved even after the event finished
     */
    public CompletableFuture<String> colorPickerShortcut(Point2D coordinates) {
        colorPickerStackPane.setLayoutX(coordinates.getX());
        colorPickerStackPane.setLayoutY(coordinates.getY());
        colorPickerStackPane.setTranslateX(-30);
        colorPickerStackPane.setTranslateY(-90);
        colorPickerStackPane.setVisible(true);

        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        colorPicker.setOnAction(event -> {
            Color color = colorPicker.getValue();
            String hexColor = String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));
            completableFuture.complete(hexColor);
        });
        return completableFuture;
    }

    /**
     * Makes the color picker stack pane visible or not
     * @param flag Visible or Not Visible
     */
    public void setColorPickerStackPaneVisibility(boolean flag) {
        colorPickerStackPane.setVisible(flag);
    }

    private void addKeyboardShortcutToControlHover() {
        scrollPane.requestFocus();
        final KeyCombination upKeyCombination = new KeyCodeCombination(KeyCode.UP);
        final KeyCombination downKeyCombination = new KeyCodeCombination(KeyCode.DOWN);
        final KeyCombination leftKeyCombination = new KeyCodeCombination(KeyCode.LEFT);
        final KeyCombination rightKeyCombination = new KeyCodeCombination(KeyCode.RIGHT);
        final KeyCombination escapeKeyCombination = new KeyCodeCombination(KeyCode.ESCAPE);

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (upKeyCombination.match(event)) {
                goUpShortcut();
                event.consume();
                System.out.println("Keyboard Shortcut : UP");
            } else if (downKeyCombination.match(event)) {
                goDownShortcut();
                event.consume();
                System.out.println("Keyboard Shortcut : DOWN");
            } else if (leftKeyCombination.match(event)) {
                goLeftShortcut();
                event.consume();
                System.out.println("Keyboard Shortcut : LEFT");
                scrollPane.requestFocus();
            } else if (rightKeyCombination.match(event)) {
                goRightShortcut();
                event.consume();
                System.out.println("Keyboard Shortcut : RIGHT");
            } else if (escapeKeyCombination.match(event)) {
                resetHover();
                event.consume();
                System.out.println("Keyboard Shortcut : ESC");
            }
        };

        scrollPane.addEventHandler(KeyEvent.KEY_PRESSED, keyEventEventHandler);
    }

    private void addKeyboardShortcutToSwitchOrderOfTasks() {
        scrollPane.requestFocus();
        final KeyCombination upKeyCombination =
                new KeyCodeCombination(KeyCode.UP, KeyCombination.SHIFT_DOWN);
        final KeyCombination downKeyCombination =
                new KeyCodeCombination(KeyCode.DOWN, KeyCombination.SHIFT_DOWN);

        final EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if (isValidHoverIndex() && upKeyCombination.match(event)) {
                if (hoverIndex != 0) {
                    h1 = hoverIndex - 1;
                    h2 = hoverListIndex;
                    moveTaskUp();
                }
                event.consume();
                System.out.println("Keyboard Shortcut : SHIFT + UP");
            } else if (isValidHoverIndex() && downKeyCombination.match(event)) {
                if (hoverIndex != lists.get(hoverListIndex).getTasks().size() - 1) {
                    h1 = hoverIndex + 1;
                    h2 = hoverListIndex;
                    moveTaskDown();
                }
                event.consume();
                System.out.println("Keyboard Shortcut : SHIFT + DOWN");
            }
        };
        scrollPane.addEventHandler(KeyEvent.KEY_PRESSED, keyEventEventHandler);
    }

    private boolean isValidHoverIndex() {
        return hoverIndex != -1 && hoverListIndex != -1;
    }

    private void moveTaskUp() {
        Long cardId = lists.get(hoverListIndex).getTasks().get(hoverIndex).getId();
        Long listId = lists.get(hoverListIndex).getId();
        cardService.relocateTask(cardId, listId, listId,hoverIndex - 1);
    }

    /**
     * Reinstantiates the hovering
     */
    public void turnHoverOn() {
        if (h1 != -1 && h2 != -1) {
            hoverIndex = h1;
            hoverListIndex = h2;
            h1 = h2 = -1;
            setListHoverColor(0);
            setTaskHoverBorder(0);
        }
        scrollPane.requestFocus();
    }

    private void moveTaskDown() {
        Long cardId = lists.get(hoverListIndex).getTasks().get(hoverIndex).getId();
        Long listId = lists.get(hoverListIndex).getId();
        cardService.relocateTask(cardId, listId, listId,hoverIndex + 1);
    }

    private void goRightShortcut() {
        scrollPane.setVvalue(0);
        if (hoverListIndex == -1) {
            scrollPane.setHvalue(0);
        } else if(hoverListIndex == lists.size()-1) {
            scrollPane.setHvalue(0);
        } else {
            double rightX = (listHolder.getWidth() - scrollPane.getWidth()) * scrollPane.getHvalue()
                + scrollPane.getWidth();
            double listRightX = lists.get(hoverListIndex + 1).getRoot().getLayoutX()
                + lists.get(hoverListIndex).getRoot().getWidth() + 20;
            if (rightX < listRightX) {
                listRightX -= scrollPane.getWidth();
                listRightX /= listHolder.getWidth() - scrollPane.getWidth();
                scrollPane.setHvalue(listRightX);
            }
        }

        setListHoverColor(+1);
        System.out.println("Going right | List index: " + hoverListIndex
                    + "| List ID: " + lists.get(hoverListIndex).getId());
    }

    private void goLeftShortcut() {
        scrollPane.setVvalue(0);
        if (hoverListIndex == -1) {
            scrollPane.setHvalue(1);
            hoverListIndex = 0;
        } else if(hoverListIndex == 0) {
            scrollPane.setHvalue(1);
        } else {
            double leftY = (listHolder.getWidth() - scrollPane.getWidth()) * scrollPane.getHvalue();
            double listLeftY = lists.get(hoverListIndex - 1).getRoot().getLayoutX() - 20;
            if (listLeftY < leftY) {
                listLeftY /= listHolder.getWidth() - scrollPane.getWidth();
                scrollPane.setHvalue(listLeftY);
            }
        }

        setListHoverColor(-1);
        System.out.println("Going left | List index: " + hoverListIndex
                    + "| List ID: " + lists.get(hoverListIndex).getId());
    }

    private void setListHoverColor(int delta) {
        resetHoverPaint();
        if (delta != 0) hoverIndex = -1;

        hoverListIndex = (hoverListIndex + delta + lists.size()) % lists.size();
        lists.get(hoverListIndex).getRoot().setStyle(
                "-fx-background-color:  #b7b7b7; -fx-background-radius: 10;");
        lists.get(hoverListIndex).getRoot().setPadding(
                new Insets(30,30,30,30));

        scrollPane.requestFocus();
    }

//    public void debug() {
//        String string = "------------------------------------------";
//        string += "\nHover Index : " + hoverIndex;
//        string += "\nList Hover Index : " + hoverListIndex;
//        string += "\nList of tasks ids : " + Arrays.toString(taskLists.stream()
//                .map(c -> c.id)
//                .toArray());
//        string += "\nList of cards in list with id " + hoverListIndex +
//        " : " + Arrays.toString(parseListOfTasks()
//                .stream().map(c -> c + " ").toArray());
//        string += "\n------------------------------------------------";
//        System.out.println(string);
//    }

    private void ensureCardIsDisplayed(int index) {
        BoardList bl = lists.get(hoverListIndex);
        double topY = (listHolder.getHeight()-scrollPane.getHeight()) * scrollPane.getVvalue();
        double taskTopY = bl.getTasks().get(index).getRoot().getLayoutY()
            + bl.getTasks().get(index).getRoot().getHeight() - 20;
        if (taskTopY < topY) {
            taskTopY /= listHolder.getHeight() - scrollPane.getHeight();
            scrollPane.setVvalue(taskTopY);
        }
        double bottomY = (listHolder.getHeight()-scrollPane.getHeight())*scrollPane.getVvalue()
            + scrollPane.getHeight();
        double taskBottomY = bl.getTasks().get(index).getRoot().getLayoutY()
            + bl.getTasks().get(index).getRoot().getHeight() + 100;
        if (bottomY < taskBottomY) {
            taskBottomY -= scrollPane.getHeight();
            taskBottomY /= listHolder.getHeight()-scrollPane.getHeight();
            scrollPane.setVvalue(taskBottomY);
        }
    }

    private void goUpShortcut() {
        if (hoverListIndex == -1 || lists.get(hoverListIndex).getTasks().size() == 0) return;
        int cardsCnt = lists.get(hoverListIndex).getTasks().size();
        if (hoverIndex == -1 || hoverIndex == 0) {
            ensureCardIsDisplayed(cardsCnt - 1);
            hoverIndex = 0;
        } else {
            ensureCardIsDisplayed(hoverIndex - 1);
        }

        setTaskHoverBorder(-1);
        System.out.println("Going up | Card Index " + hoverIndex);
    }

    private void goDownShortcut() {
        if (hoverListIndex == -1 || lists.get(hoverListIndex).getTasks().size() == 0) return;
        int cardCnt = lists.get(hoverListIndex).getTasks().size();
        if(hoverIndex == -1 || hoverIndex == cardCnt - 1) {
            scrollPane.setVvalue(0);
        } else {
            ensureCardIsDisplayed(hoverIndex + 1);
        }

        setTaskHoverBorder(+1);
        System.out.println("Going down | Card Index " + hoverIndex);
    }

    private void resetHoverPaint() {
        if (hoverListIndex == -1) return;
        BoardList bl = lists.get(hoverListIndex);
        bl.getRoot().setStyle(
                "-fx-background-color:  #D9D9D9; -fx-background-radius: 10;");
        bl.getRoot().setPadding(
                new Insets(30,30,30,30));

        if (hoverIndex == -1) return;
        Task t = bl.getTasks().get(hoverIndex);
        t.getBackground().setStroke(Paint.valueOf("white"));
        t.getBackground().setStrokeWidth(0);
    }

    private void setTaskHoverBorder(int delta) {
        Task t;
        if (hoverIndex != -1) {
            t = lists.get(hoverListIndex).getTasks().get(hoverIndex);
            t.getBackground().setStroke(Paint.valueOf("white"));
            t.getBackground().setStrokeWidth(0);
        }
        int cardCnt = lists.get(hoverListIndex).getTasks().size();
        hoverIndex = (hoverIndex + delta + cardCnt) % cardCnt;
        t = lists.get(hoverListIndex).getTasks().get(hoverIndex);
        t.getRoot().requestFocus();
        t.getBackground().setStroke(Paint.valueOf("black"));
        t.getBackground().setStrokeWidth(2);
    }

    /**
     * Loads the tab into the board scene that this is the controller of.
     * @param board the board that should be loaded in.
     * @param parent the tab of which the board is loaded into.
     */
    public void loadBoard(Board board, Tab parent) {
        rootPane.getStyleClass().add(0, board.id.toString());
        listHolder.setSpacing(20);
        // Load board from server
        if(board == null){
            mainCtrl.createPopUp("Connection error!",
                    "The board is either not available or does not exist");
            baseCtrl.closeTab(parent);
            return;
        }

        this.id = board.id;
        parent.setText(board.name);
        scrollPane.requestFocus();
    }

    /**
     * Adds a list inside the board
     * @param listElem the list to be added
     * @param idx the index on which the list should be added
     */
    public void addListOnPosition(BoardList listElem, int idx) {
        resetHover();
        listHolder.getChildren().add(idx, listElem.getRoot());
        lists.add(idx, listElem);
    }

    /**
     * @return Returns the root pane of the board scene.
     */
    public AnchorPane getRootPane() {
        return rootPane;
    }

    /**
     * @return Returns the add list button
     */
    public Button getAddListButton() {
        return addListButton;
    }

    /**
     * @return Returns the  Array list of lists.
     */
    public List<BoardList> getLists() {
        return lists;
    }

    /**
     * @return Returns the UI element that holds the lists.
     */
    public HBox getListHolder() {
        return listHolder;
    }

    /**
     * Remove list
     * @param listId the id of the list
     * @return the removed list
     */
    public BoardList removeList(Long listId) {
        resetHover();
        BoardList listToRemove = null;
        for (BoardList list : lists) {
            if (list.getId() == listId) {
                listToRemove = list;
                break;
            }
        }
        if (listToRemove != null) {
            lists.remove(listToRemove);
            listHolder.getChildren().remove(listToRemove.getRoot());
        }
        return listToRemove;
    }

    /**
     * Relocates the visual element of the list with its new position
     * @param listId the id of the list scene to be relocated
     * @param index the new index of the scene
     */
    public void relocateList(Long listId, Integer index) {
        resetHover();
        BoardList list = removeList(listId);
        addListOnPosition(list, index);
    }

    /**
     * Sets the hover in initial state
     */
    public void resetHover() {
        resetHoverPaint();
        hoverIndex = -1;
        hoverListIndex = -1;
    }

    /**
     * @return the id of the board
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the focus of the user to the scroll pane in order to
     */
    public void setScrollPaneFocus() {
        scrollPane.requestFocus();
    }

    /**
     * Shows the dark filter over the board scene during editing of a card
     * @param flag whether the filter should be active
     */
    public void setDarkBackground(boolean flag) {
        editPopUp.setVisible(flag);
    }

    /**
     * Sets a flag that the color picker is active or not.
     * @param colorPickerActive true / false
     */
    public void setColorPickerActive(boolean colorPickerActive) {
        this.colorPickerActive = colorPickerActive;
    }

    /**
     * Makes the color picker disappear when the user clicks on
     * the screen.
     * @param event Mouse click
     */
    public void makeColorPickerDisappearOnClick(MouseEvent event) {
        colorPickerStackPane.setVisible(false);
    }

    /**
     * Hides the color picker
     */
    public void hideColorPicker() {
        colorPickerStackPane.setVisible(false);
    }
}
