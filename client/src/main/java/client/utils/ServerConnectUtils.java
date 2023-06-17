package client.utils;

import client.scenes.BaseCtrl;
import client.services.BoardService;

import javax.inject.Inject;

public class ServerConnectUtils {

    private final ServerUtils serverUtils;
    private BoardService boardService;

    private BaseCtrl baseCtrl;

    /**
     * Constructor for the Helper class ServerConnectUtils
     * This class is used as an additional abstraction layer
     * @param serverUtils - Instance of the ServerUtils
     * @param boardService - Instance of BoardService
     * @param baseCtrl - Instance of the BaseCtrl
     */
    @Inject
    public ServerConnectUtils(ServerUtils serverUtils,
                              BoardService boardService, BaseCtrl baseCtrl){
        this.serverUtils = serverUtils;
        this.boardService = boardService;
        this.baseCtrl = baseCtrl;
    }

    /**
     * Method for reinitializing the server when
     * the User modifies it from the Preferences menu
     * @param address - the new Address
     * @param port - the new Port
     */
    public void reinitialize(String address, String port){
        //We reinitialize the actual server(ServerUtils)
        String server = address+":"+port;
        if(!server.equals(serverUtils.getServer())) {
            boardService.closeAll();
            serverUtils.reinitialize(address, port);

            //We close all the already opened boards in the front-end
            int ss =  baseCtrl.getTabPane().getTabs().size();
            baseCtrl.addTab();
            baseCtrl.getTabPane().getTabs().remove(0,ss);

        }

    }
}
