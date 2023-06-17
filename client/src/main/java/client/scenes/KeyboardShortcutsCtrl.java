package client.scenes;

import client.utils.ConfigUtils;
import client.utils.ServerUtils;
import com.google.inject.Inject;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class KeyboardShortcutsCtrl implements Initializable {
    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private final ConfigUtils configUtils;

    /**
     * Constructor for the Preferences window controller
     * @param server - Instance of the Server
     * @param mainCtrl - Instance of the main Controller
     * @param configUtils - Instance of the configuration file helper method
     */
    @Inject
    public KeyboardShortcutsCtrl(ServerUtils server, MainCtrl mainCtrl, ConfigUtils configUtils) {
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
        // .getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        // keep for later
    }
}
