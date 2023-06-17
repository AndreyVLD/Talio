package client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

@Singleton
public class ConfigUtils {
    private static final String fileLocation = "resources/config.json";
    /**
     * Method for saving a given Map as a JSON file
     * @param map - the map we want to save
     */
    public void save(Map<String, Object> map){
        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(Paths.get(fileLocation).toFile(),map);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method for loading a JSON file as a Map
     * @return - a Map with the items in the JSON file
     * @throws IOException - if the file is not found
     */
    public Map<String,Object> load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(Paths.get(fileLocation).toFile(),Map.class);
        return map;
    }

    /**
     * Adds the given username to the JSON file
     * @param username - the username we want to save
     */
    public void addUserName(String username){
        try{
            Map<String, Object> map = load();
            map.put("usernameTextField",username);
            save(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides the username of the client
     * @return the username
     * @throws IOException if the file is not found
     */
    public String getUserName(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String,Object> map = mapper.readValue(Paths.get(fileLocation).toFile(),Map.class);
            return (String) map.get("usernameTextField");
        } catch (IOException e) {
            return "";
        }
    }
}
