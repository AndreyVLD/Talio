package client.scenes;

import client.utils.ConfigUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PopupCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ConfigUtils configUtils;

    @FXML
    VBox vBox;

    @FXML
    TextField username;

    @FXML
    Label invalid;

    @FXML
    Button submitButton;

    /**
     * Constructor for the Popup window controller
     * @param server - Instance of the Server
     * @param mainCtrl - Instance of the main Controller
     * @param configUtils - Instance of the configuration file helper method
     */
    @Inject
    public PopupCtrl(ServerUtils server, MainCtrl mainCtrl, ConfigUtils configUtils) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.configUtils=configUtils;
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
        invalid.setVisible(false);
        vBox.getStyleClass().add("popup-vbox");
        vBox.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        submitButton.getStyleClass().add("submitButton");
    }

    /**
     * Method for when the submit button is pressed
     */

    public void submit(){
        String name = username.getText();

        // We validate the input here
        if(name.isEmpty()){
            invalid.setVisible(true);
        }else{
            configUtils.addUserName(name);
            invalid.setVisible(false);
            mainCtrl.getSecondaryStage().close();
        }
    }
}
