package client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RecentBoardsUtils {
    private static final String fileLocation = "resources/recent_boards.json";
    private ServerUtils server;
    
    /**
     * Initializes the recent board utils
     * @param server injectable field for ServerUtils
     */
    @Inject
    public RecentBoardsUtils(ServerUtils server) {
        this.server = server;
        if (!Paths.get(fileLocation).toFile().exists()) {
            File f = new File(fileLocation);
            try {
                FileWriter fw = new FileWriter(f);
                fw.write("{}");
                fw.flush();
                fw.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Selects all recent boards of the current server
     * @return a Map containing the ids and names of the boards
     */
    public Map<Long, String> getBoards() {

        ObjectMapper mapper = new ObjectMapper();
        Map<Long, String> boards = new HashMap<>();

        try {
            Map<String, String> map = mapper.readValue(Paths.get(fileLocation).toFile(), Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {

                String key = entry.getKey();
                String res[] = key.split("/"); // server and board id

                if (res[0].equals(server.getServer())) {
                    boards.put(Long.parseLong(res[1]), entry.getValue().split("::")[0]);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return boards;
    }

    /**
     * Adds a board in the Json file
     * @param id the id of the new board
     * @param name the name of the new board
     * @param password the password of the board to be saved
     */
    public void addBoard(Long id, String name, String password) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> boards = new HashMap<>();

        try {
            if (password == null) {
                password = "";
            }
            Map<String, String> map = mapper.readValue(
                Paths.get(fileLocation).toFile(), HashMap.class);

            String value = map.get(server.getServer() + "/" + id);
            if (value != null) {
                String res[] = value.split("::");
                if (res.length > 1) {
                    password = res[1];
                }
            }
            map.put(server.getServer() + "/" + id, name + "::" + password);
            mapper.writeValue(new File(fileLocation), map);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method for getting the saved password by boardId from the file
     * @param boardId - the id of the board to try and get the password from
     * @return - null or String (the password)
     */
    public String getPasswordByBoardId(Long boardId) {
        ObjectMapper mapper = new ObjectMapper();
        Map<Long, String> boards = new HashMap<>();

        try {
            Map<String, String> map = mapper.readValue(Paths.get(fileLocation).toFile(), Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {

                String key = entry.getKey();
                String idString = key.split("/")[1];
                String res[] = entry.getValue().split("::"); // board name and password

                if (Long.parseLong(idString) == boardId) {
                    String result = null;
                    try {
                        result = res[1];
                        return result;
                    } catch (IndexOutOfBoundsException ex) {
                        return null;
                    }
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Sets the saved password of a board
     * @param boardId - the id of the board to set it for
     * @param boardPassword - the updated saved password
     */
    public void setPasswordByBoardId(long boardId, String boardPassword) {
        ObjectMapper mapper = new ObjectMapper();

        if (boardPassword == null) {
            boardPassword = "";
        }
        try {
            Map<String, String> map = mapper.readValue(Paths.get(fileLocation).toFile(), Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {

                String key = entry.getKey();
                String idString = key.split("/")[1];
                String res[] = entry.getValue().split("::"); // board name and password

                if (Long.parseLong(idString) == boardId) {
                    entry.setValue(res[0] + "::" + boardPassword);
                }
            }
            mapper.writeValue(new File(fileLocation), map);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Deletes board from recentboards json file by a boardId
     * @param boardId - the id of the board to delete from the file
     */
    public void deleteBoard(Long boardId) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, String> map = mapper.readValue(Paths.get(fileLocation).toFile(), Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String idString = key.split("/")[1];

                if (Long.parseLong(idString) == boardId) {
                    map.remove(entry.getKey());
                    mapper.writeValue(new File(fileLocation), map);
                    return;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
