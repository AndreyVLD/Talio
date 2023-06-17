package client.scenes;

import client.utils.ConfigUtils;
import client.utils.ServerConnectUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;


public class PreferencesCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ConfigUtils configUtils;

    private final ServerConnectUtils serverConnectUtils;

    @FXML
    TreeView<String>treeView;
    @FXML
    GridPane gridPane;
    @FXML
    ScrollPane scrollPane;

    private final Map<String, Node> optionContent = new HashMap<>();
    private final Map<String, Node> allChildren = new HashMap<>();


    /**
     * Constructor for the Preferences window controller
     * @param server - Instance of the Server
     * @param mainCtrl - Instance of the main Controller
     * @param configUtils - Instance of the configuration file helper method
     * @param serverConnectUtils - Instance of the helper class serverConnectUtils
     */
    @Inject
    public PreferencesCtrl(ServerUtils server, MainCtrl mainCtrl,
                           ConfigUtils configUtils, ServerConnectUtils serverConnectUtils) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configUtils=configUtils;
        this.serverConnectUtils = serverConnectUtils;
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
        gridPane.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        //Loading the different Boxes for the several options
        optionContent.put("Server Settings",loadFXML("ServerSettings.fxml"));
        optionContent.put("Customisation",loadFXML("Customisation.fxml"));
        optionContent.put("General Settings",loadFXML("GeneralSettings.fxml"));

        //Setting up the TreeView
        TreeItem<String> root = new TreeItem<>("Settings");
        TreeItem<String> generalSettings = new TreeItem<>("General Settings");
        TreeItem<String> custom = new TreeItem<>("Customisation");
        TreeItem<String> url = new TreeItem<>("Server Settings");
        generalSettings.getChildren().add(url);
        root.getChildren().addAll(generalSettings,custom);
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        // The Preference window will open up with the General Settings selected
        treeView.getSelectionModel().select(generalSettings);
        selectItem();

        /**
         * Dynamically setting the minimum sizes of the Stage
         * Listener for the first computed size of the gridPane. Once we have those sizes we set the
         * minimal size of the stage based on those!
         */
        final boolean[] firstResize = {true};

        gridPane.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if(firstResize[0]){
                // We set this to false, so we get the minimal siz only once based on the
                // first computed value.
                firstResize[0] =false;

                //We need to get the sizes ONLY after the
                // Grid was completely build therefore we delay.
                Platform.runLater(() -> {
                    Stage stage = (Stage) gridPane.getScene().getWindow();
                    stage.setMinWidth(gridPane.getWidth());
                    stage.setMinHeight(gridPane.getHeight());
                });
            }
        });

        helperMethod();
    }

    /**
     * Helper method with some config file functionality
     */
    private void helperMethod() {
        // We load into a Hash Map all pairs formed between: child Nodes and their IDs.
        for(Node node:optionContent.values()){
            allChildren.putAll(getAllChildren(node));
        }

        //When the menu is opened we load.
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A method for the functionality of what happens when a user selects a certain
     * item from the menu.
     */
    public void selectItem(){
        TreeItem<String>item = treeView.getSelectionModel().getSelectedItem();
        if(item !=null && item.getParent() != null){
            if(optionContent.containsKey(item.getValue())){
                Node node = optionContent.get(item.getValue());
                scrollPane.setContent(node);
            }
        }
    }

    /**
     * If the user cancels (presses the cancel Button) we load back again and close the window.
     */
    public void cancel(){
        try{
            load();
        }catch (Exception e){
            e.printStackTrace();
        }
        mainCtrl.getSecondaryStage().close();
    }

    /**
     * If the user presses the apply button
     * we save the fields from the settings and close the window.
     */
    public void apply(){
        save();
    }

    /**
     * Helper method for loading an auxiliary FXML file
     * @param fxmlFileName -  the name of the FXML file
     * @return - a Node constructed on that file.
     */
    private Node loadFXML(String fxmlFileName) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFileName));
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saving the current settings
     */
    private void save() {
        Map<String, Object> map = new HashMap<>();

        for(Node child:allChildren.values()){
            if(child instanceof TextField){
                map.put(child.getId(),((TextField)child).getText());
            }else if(child instanceof ColorPicker){
                map.put(child.getId(),((ColorPicker)child).getValue().toString());
            }
        }
        Map<String, Object> oldMap = new HashMap<>();
        try {
            oldMap = configUtils.load();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        String portOld = (String) oldMap.get("portTextField");
        String addressOld = (String) oldMap.get("serverTextField");

        String port = (String) map.get("portTextField");
        String address = (String) map.get("serverTextField");

        serverConnectUtils.reinitialize(address, port);

        if (!server.isConnected()) {
            serverConnectUtils.reinitialize(addressOld,portOld);
            ((TextField)allChildren.get("portTextField")).setText(portOld);
            ((TextField)allChildren.get("serverTextField")).setText(addressOld);
            mainCtrl.createPopUp("Server Connection Error","A connection cannot be " +
                "established with the given address and port. Please try again!");
        } else {
            configUtils.save(map);
            mainCtrl.getSecondaryStage().close();
        }
    }

    /**
     * Loading the settings from the Config.json file
     * @throws IOException - IOException if the file is not found
     */
    private void load() throws IOException {
        Map<String, Object> map = configUtils.load();

        for(String nodeId:map.keySet()){
            Node node  = allChildren.get(nodeId);
            String content = (String) map.get(nodeId);

            if(node instanceof TextField){

                ((TextField)node).setText(content);

            }else if(node instanceof ColorPicker){

                ((ColorPicker)node).setValue(Color.web(content));
            }
        }

    }

    /**
     * Recursive method for getting all the children of a given node
     * @param node - the root node
     * @return - a hashmap with (Node ID, Node) pairs
     */

    public static Map<String,Node> getAllChildren(Node node) {
        Map<String,Node> children = new HashMap<>();

        if (node != null) {
            children.put(node.getId(),node);

            if (node instanceof Parent) {

                for (Node child : ((Parent)node).getChildrenUnmodifiable()) {
                    children.putAll(getAllChildren(child));
                }
            }
        }
        return children;
    }

}
