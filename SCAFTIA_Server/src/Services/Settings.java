package Services;

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Settings {
    private final String INI_FILEPATH = System.getProperty("user.dir");
    private final String SETTINGS_FILE_NAME = "Settings.ini";
    private final String USERS_SECTION = "Users";
    private final String SERVER_IP_PORT_SECTION = "Server IP:Port";
    private final String SERVER_IP_PORT_KEY = "IPPort";
    private final String PASSWORDS_SECTION = "Passwords";
    private final String MAC_PASSWORD_KEY = "MacPassword";
    private final String SHARED_PASSWORD_KEY = "SharedPassword";

    private Map<String,String> usernamesAndPasswords;

    Ini iniSettings;

    /**
     * Constructor
     */
    public Settings(){
        usernamesAndPasswords = new HashMap<>();
        try {
            iniSettings = new Ini(new File(INI_FILEPATH + "\\" + SETTINGS_FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Add a MAC password to the configuration file
     * @param password password
     */
    public void addMacPassword(String password){
        iniSettings.put(PASSWORDS_SECTION,MAC_PASSWORD_KEY,password);
    }

    /**
     *
     * @return
     */
    public String getMacPassword(){
        return iniSettings.get(PASSWORDS_SECTION,MAC_PASSWORD_KEY);
    }

    /**
     * Add a shared password to the configuration file
     * @param password password
     */
    public void addSharedPassword(String password){
        iniSettings.put(PASSWORDS_SECTION,SHARED_PASSWORD_KEY,password);
    }

    /**
     *
     * @return
     */
    public String getSharedPassword(){
        return iniSettings.get(PASSWORDS_SECTION,SHARED_PASSWORD_KEY);
    }

    /**
     * Add a user to the configuration file
     * @param username username
     * @param password password
     */
    public void addUser(String username, String password){
        iniSettings.put(USERS_SECTION,username.toLowerCase(),password);
    }

    /**
     * Get password of a user.
     * @param username username
     */
    public String getUserPassword(String username){
        return iniSettings.get(USERS_SECTION,username.toLowerCase());
    }

    /**
     * Remove a user from the configuration file
     * @param username username
     */
    public void removeUser(String username){
        Profile.Section section = iniSettings.get(USERS_SECTION);
        section.remove(username);
    }

    /**
     * Add ip and port to the configuration file
     * @param Ipport ip:port
     */
    public void addIPPort(String Ipport){
        iniSettings.put(SERVER_IP_PORT_SECTION,SERVER_IP_PORT_KEY,Ipport);
    }

    /**
     *
     * @return
     */
    public String getIPPort(){
        return iniSettings.get(SERVER_IP_PORT_SECTION,SERVER_IP_PORT_KEY);
    }

    /**
     * Save the changes to the configuration file
     */
    public void SaveChanges(){
        try {
            iniSettings.store(iniSettings.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all the neighbors from the configuration file.
     * @return
     */
    public Map<String,String> getAllUsers(){
        Ini.Section section = iniSettings.get(USERS_SECTION);
        usernamesAndPasswords.clear();
        if(section == null){
            iniSettings.add(USERS_SECTION);
            section = iniSettings.get(USERS_SECTION);
        }
        Set<String> neighborsNames = section.keySet();
        if(neighborsNames.size() == 0) {
            return usernamesAndPasswords;
        }

        for (String neighborName:neighborsNames) {
            usernamesAndPasswords.put(neighborName,section.get(neighborName));
        }
        return usernamesAndPasswords;
    }
}
