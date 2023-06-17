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
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import commons.Board;
import commons.Card;
import commons.Subtask;
import commons.Tag;
import commons.TaskList;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServerUtils {
    private String server;
    private StompSession session;
    private final Map<String, StompSession.Subscription> subscriptions = new HashMap<>();
    private final HashMap<String, ExecutorService> executors = new HashMap<>();
    private boolean isConnected;


    /**
     * This method is here for loading the
     * serverUtils for the first time from the config file
     * @param configUtils - instance of configUtils
     */
    @Inject
    public ServerUtils(ConfigUtils configUtils){
        Map<String,Object> map;
        //We try to load the file
        try{
            map = configUtils.load();
            String port = (String) map.get("portTextField");
            String address = (String) map.get("serverTextField");
            reinitialize(address,port);
        } catch (Exception e) {
            //If the file is not found we use the default values.
            reinitialize("localhost","8080");
        }
    }

    /**
     * Method for getting the tags that are not deleted by Board ID
     * @param boardId - the ID the board
     * @return - an List of Tags
     */
    public List<Tag> getNotDeletedTagsByBoardId(Long boardId){
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/tags/board/"+boardId)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(new GenericType<List<Tag>>(){
                    });
        }catch (BadRequestException e){
            System.out.println("error");
            return null;
        }
    }

    /**
            * Method for getting all assigned tags for a given card
     * @param cardId the id of the card
     * @return a list of the requested tags
     */
    public List<Tag> getAssignedTagsByCardID(Long cardId) {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/tags/card/" + cardId + "/assigned")
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(new GenericType<List<Tag>>(){
                    });
        } catch (BadRequestException e) {
            return null;
        }
    }

    /**
            * Method for getting all available tags for a given card
     * @param cardId the id of the card
     * @return a list of the requested tags
     */
    public List<Tag> getAvailableTagsByCardID(Long cardId) {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/tags/card/" + cardId + "/available")
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(new GenericType<List<Tag>>(){
                    });
        } catch (BadRequestException e) {
            return null;
        }
    }
    /**
     * Method for getting a Tag based on the ID
     * @param id - the ID of the tag we are looking for
     * @return - the Tag with the given tag
     */

    public Tag getTag(long id) {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/tags/" + id)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(new GenericType<Tag>() {});
        } catch (BadRequestException e) {
            return null;
        }
    }

    /**
     * Method for creating a Tag based on the given input
     * @param tag - the Tag for which we create a server entity
     * @return - The Tag from the server after creation
     */
    public Tag createTag(Tag tag){
        try{
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("/api/tags")
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .post(Entity.entity(tag,APPLICATION_JSON), Tag.class);

        }catch (BadRequestException e){
            return null;
        }
    }

    /**
     * Method that updates a tag with the given id based on the new name and color
     * @param id - the id of the tag we want to update
     * @param name - the new name of the tag
     * @param color - the color the tag we want to update
     * @return - the updated tag
     */
    public Tag updateTag(long id, String name, String color) {
        try {
            Tag updatedTag = new Tag();
            updatedTag.name=name;
            updatedTag.color=color;

            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/tags/" + id)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .post(Entity.entity(updatedTag,APPLICATION_JSON), Tag.class);
        } catch (BadRequestException  e){
            return null;
        }
    }

    /**
     * Deletes a tag from the DB based on the given ID
     * @param id - the ID of the tag we want to delete
     */
    public void deleteTagById(Long id){
        try{
            ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/tags/"+id)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .delete();
        }catch (BadRequestException e){
            return ;
        }
    }



    /**
     * @param id the id of the board.
     * @return the board object that is requested
     */
    public Board getBoard(long id) {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/boards/" + id)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(new GenericType<Board>() {
                    });
        } catch (BadRequestException e){
            return null;
        }
    }

    /**
     * @param board blueprint of the new board.
     * @return the newly created board.
     */
    public Board createBoard(Board board){
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/boards/")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(board, APPLICATION_JSON), Board.class);
    }

    /**
     * @param boardId the id of the board of which you retrieve the lists.
     * @return an List with all the lists of the board.
     */
    public List<TaskList> getListsByBoard(long boardId){
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server).path("api/lists/board/" + boardId)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(new GenericType<List<TaskList>>() {
                    });
        } catch (BadRequestException e){
            return null;
        }
    }

    /**
     * @param boardId the id of the board of which you retrieve the lists.
     * @return an List with all the lists of the board.
     */
    public List<TaskList> getNotDeletedListsByBoard(long boardId){
        try {
            return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/lists/notDeleted/board/" + boardId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<TaskList>>() {
                });
        } catch (BadRequestException e){
            return null;
        }
    }

    /**
     * @param listId the id of the list of which you retrieve the cards.
     * @return an List with all the cards of the list.
     */
    public List<Card> getCardsByList(long listId){
        try {
            return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/cards/taskList/" + listId + "/active")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Card>>() {
                });
        } catch (BadRequestException e){
            return null;
        }
    }

    /**
     * Get request for getting all lists from a certain boardId
     * @param boardId - the boardId to get the lists from
     * @return - the data received
     */
    public List<TaskList> getLists(long boardId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("/api/lists/board/" + boardId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<TaskList>>() {});
    }

    /**
     * Gets a card by id
     * @param id the id of the card
     * @return the card with the requested id
     */
    public Card getCardById(Long id) {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/cards/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Card>>() {
                }).get(0); // in order to avoid using custom (de)serializer the result is List<Card>
        } catch (BadRequestException e){
            return null;
        }
    }

    /**
     * Gets a list by id
     * @param id the id of the list
     * @return the list with the requested id
     */
    public TaskList getListById(Long id) {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/lists/" + id)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(TaskList.class);
        } catch (BadRequestException e){
            return null;
        }
    }

    /**
     * Deletes list by id using a DELETE request to the server
     * @param listId the id of the list to be deleted
     */
    public void deleteListById(Long listId) {
        try {
            ClientBuilder.newClient(new ClientConfig())
                .target(server).path("/api/lists/" + listId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete();
        } catch (BadRequestException e){
        }
    }

    /**
     * Gets subtasks by card id from the server
     * @param cardId the id of the card
     * @return a list containing all not deleted subtasks
     */
    public List<Subtask> getSubtasksByCardId(Long cardId) {
        return ClientBuilder.newClient(new ClientConfig())
            .target(server).path("/api/subtasks/" + cardId)
            .request(APPLICATION_JSON)
            .accept(APPLICATION_JSON)
            .get(new GenericType<List<Subtask>>() {});
    }

    /**
     * POST request for changing the board name
     * @param board - the board with the changed name
     * @return the updated board
     */
    public Board changeBoardName(Board board) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/boards/changeName")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(board, APPLICATION_JSON), Board.class);
    }

    /**
     * POST request for changing the board password
     * @param board - the board with the changed password
     * @return the updated board
     */
    public Board changeBoardPassword(Board board) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(server).path("api/boards/changePassword")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(board, APPLICATION_JSON), Board.class);
    }

    /**
     * Method for connecting to a certain websocket endpoint
     * @param url - the websocket endpoint to connect to
     * @return StompSession - session object to send/receive data
     */
    private StompSession connect(String url) {
        var client = new StandardWebSocketClient();
        var stomp = new WebSocketStompClient(client);

        stomp.setMessageConverter(new MappingJackson2MessageConverter());
        try {
            this.isConnected = true;
            return stomp.connect(url, new StompSessionHandlerAdapter() {}).get();
        } catch (Exception e) {
        }
        this.isConnected = false;
        return null;
    }

    /**
     * Method for registering a specific topic to receive messages from
     * @param dest - Topic to receive messages from
     * @param type - Class of the type we want to work with
     * @param consumer - The assignment target for a lambda expression or method reference to
     *                 execute when receiving data
     * @param <T> - The datatype to use (just calling and setting the type parameter is enough)
     */
    public <T> void registerForMessages(String dest, Class<T> type, Consumer<T> consumer) {
        StompSession.Subscription subscription = session.subscribe(dest, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return type;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                consumer.accept((T) payload);
            }
        });

        subscriptions.put(dest, subscription);
    }

    /**
     * Deregister for messages by destination
     * @param dest the destination
     */
    public void deregisterForMessages(String dest) {
        subscriptions.get(dest).unsubscribe();
        subscriptions.remove(dest);
    }

    /**
     * Method for sending data to the websocket instance
     * @param dest - The destination (/app/*)
     * @param o - The object to send
     */
    public void send(String dest, Object o) {
        session.send(dest, o);
        System.out.println("Sent " + o.getClass().getName() + " to " + dest);
    }

    /**
     * Returns the address and the port of the server
     * @return a String specifying the address and the port of the server
     */
    public String getServer() {
        return server.split("/")[2];
    }

    /**
     * Reinitialize the server utils with a new server address and port
     * @param address the new address of the server
     * @param port the new port of the server
     */
    public void reinitialize(String address, String port) {
        this.server = "http://" + address + ":" + port + "/";
        this.session = connect("ws://" + address + ":" + port + "/websocket");
        subscriptions.forEach((k, s) -> {
            s.unsubscribe();
        });
        executors.forEach((k, e) -> {
            e.shutdownNow();
        });
        subscriptions.clear();
        executors.clear();
    }

    /**
     * Method for registering for long polling of a given rest endpoint
     * @param dest the address of the endpoint
     * @param consumer a consumer object that will be used to return the result
     */
    public void registerForLongPolling(String dest, Consumer consumer) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        executors.put(dest, exec);
        exec.submit(() -> {
            while (!Thread.interrupted()) {
                var res = ClientBuilder.newClient(new ClientConfig())
                    .target(server).path(dest)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(Response.class);
                if (res.getStatus() == 204) {
                    continue;
                }
                var q = res.readEntity(TaskList.class);
                consumer.accept(q);
            }
        });
    }

    /**
     * Deregister for long polling by destination
     * @param dest the destination
     */
    public void deregisterForLongPolling(String dest) {
        executors.get(dest).shutdownNow();
        executors.remove(dest);
    }

    /**
     * @return whether the server is connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Stops long polling
     */
    public void stopLongPolling() {
        for (ExecutorService exec : executors.values()) {
            exec.shutdownNow();
        }
    }
}