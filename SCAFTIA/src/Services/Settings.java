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
    private final String NEIGHBORS_SECTION = "Neighbors";
    private final String SHARED_PASSWORD_SECTION = "SharedPassword";
    private final String MAC_PASSWORD_SECTION = "MACPassword";
    private final String PRIVATE_PASSWORD_SECTION = "PrivatePassword";
    private final String SERVER_SECTION = "Server";
    private final String SERVER_ADDRESS_KEY = "ServerAddress";
    private final String SHARED_PASSWORD_KEY = "Password";
    private final String MY_PORT_SECTION = "MyPort";
    private final String PORT_KEY = "Port";

    private Map<String,String> neighborsAndIP;

    Ini iniSettings;

    /**
     * Constructor
     */
    public Settings(){
        neighborsAndIP = new HashMap<>();
        try {
            iniSettings = new Ini(new File(INI_FILEPATH + "\\" + SETTINGS_FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a neighbor to the configuration file
     * @param ip neighbor name
     * @param port IP:Port
     */
    public void addNeighbor(String ip, String port){
        iniSettings.put(NEIGHBORS_SECTION,ip,port);
    }

    /**
     * Remove a neighbor from the configuration file
     * @param neighbor
     */
    public void removeNeighbor(String neighbor){
        Profile.Section section = iniSettings.get(NEIGHBORS_SECTION);
        section.remove(neighbor);
    }

    /**
     * Get the shared password from the configuration file
     * @return shared password
     */
    public String getSharedPassword(){
        return iniSettings.get(SHARED_PASSWORD_SECTION,SHARED_PASSWORD_KEY);
    }

    /**
     * Set the shared password to the configuration file
     * @param password shared password
     */
    public void setSharedPassword(String password){
        iniSettings.put(SHARED_PASSWORD_SECTION,SHARED_PASSWORD_KEY,password);
    }

    /**
     * Get the private password from the configuration file
     * @return shared password
     */
    public String getPrivatePassword(){
        return iniSettings.get(PRIVATE_PASSWORD_SECTION,SHARED_PASSWORD_KEY);
    }

    /**
     * Set the private password to the configuration file
     * @param password password
     */
    public void setPrivatePassword(String password){
        iniSettings.put(PRIVATE_PASSWORD_SECTION,SHARED_PASSWORD_KEY,password);
    }

    /**
     * Set the server address
     * @param serverIPPort ip:port of the server
     */
    public void setServerAddress(String serverIPPort){
        iniSettings.put(SERVER_SECTION,SERVER_ADDRESS_KEY,serverIPPort);
    }

    /**
     * Get the server ip:port
     * @return server ip:port
     */
    public String getServerAddress(){
        return iniSettings.get(SERVER_SECTION,SERVER_ADDRESS_KEY);
    }

    /**
     * Get the mac password from the configuration file
     * @return shared password
     */
    public String getMACPassword(){
        return iniSettings.get(MAC_PASSWORD_SECTION,SHARED_PASSWORD_KEY);
    }

    /**
     * Set the mac password to the configuration file
     * @param password shared password
     */
    public void setMACPassword(String password){
        iniSettings.put(MAC_PASSWORD_SECTION,SHARED_PASSWORD_KEY,password);
    }

    /**
     * Get my port
     * @return port
     */
    public String getMyPort(){
        return iniSettings.get(MY_PORT_SECTION,PORT_KEY);
    }

    /**
     * Set my port
     * @param port port
     */
    public void setMyPort(String port){
        iniSettings.put(MY_PORT_SECTION,PORT_KEY,port);
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
    public Map<String,String> getAllNeighbors(){
        Ini.Section section = iniSettings.get(NEIGHBORS_SECTION);
        neighborsAndIP.clear();
        if(section == null){
            iniSettings.add(NEIGHBORS_SECTION);
            section = iniSettings.get(NEIGHBORS_SECTION);
        }
        Set<String> neighborsNames = section.keySet();
        if(neighborsNames.size() == 0) {
            return neighborsAndIP;
        }

        for (String neighborName:neighborsNames) {
            neighborsAndIP.put(neighborName,section.get(neighborName));
        }
        return neighborsAndIP;
    }
}
