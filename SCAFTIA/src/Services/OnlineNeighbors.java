package Services;

import Controllers.MainWindow_Controller;

import java.util.HashMap;
import java.util.Map;

public class OnlineNeighbors {

    private static Map<String,String> onlineNeighbors = new HashMap<>();
    private static Map<String,String> badHmacOnlineNeighbors = new HashMap<>();
    private static Map<String,String> allNeighbors = new HashMap<>();

    /**
     * set the updated neighbors list
     * @param neighbors neighbors list
     */
    public static void SetNeighborsList(Map<String,String> neighbors){
        allNeighbors = neighbors;
    }

    /**
     * Clear the list of online neighbors
     */
    public static void ClearOnlineNeighbors(){
        onlineNeighbors.clear();
    }

    /**
     * Clear the list of online neighbors
     */
    public static void ClearBadHmacOnlineNeighbors(){
        badHmacOnlineNeighbors.clear();
    }

    /**
     * Get the list of online neighbors by IP:Port
     * @return map of Key:IP Value:Port
     */
    public static Map<String,String> GetOnlineNeighborsIPPort(){
        Map<String,String> neighbors = new HashMap<>();
        for(Map.Entry<String,String> item: onlineNeighbors.entrySet()){
            String[] ipPort = item.getValue().split(":");
            neighbors.put(ipPort[0],ipPort[1]);
        }
        return neighbors;
    }

    /**
     * Get the list of online neighbors by IP:Port
     * @return map of Key:IP Value:Port
     */
    public static String GetOnlineNeighborIPPort(String name){
        for(Map.Entry<String,String> item: onlineNeighbors.entrySet()){
            if(item.getKey().toLowerCase().equals(name))
                return item.getValue().split(":")[0];
        }
        return "";
    }

    /**
     * Get the list of online neighbors by IP:Port
     * @return map of Key:IP Value:Port
     */
    public static Map<String,String> GetBadHmacOnlineNeighborsIPPort(){
        Map<String,String> neighbors = new HashMap<>();
        for(Map.Entry<String,String> item: badHmacOnlineNeighbors.entrySet()){
            String[] ipPort = item.getValue().split(":");
            neighbors.put(ipPort[0],ipPort[1]);
        }
        return neighbors;
    }

      /**
     * Get the list of online neighbors
     * @return
     */
    public static Map<String,String> GetOnlineNeighbors(){
        return onlineNeighbors;
    }

    /**
     * Check if a neighbor is online.
     * @param neighbor neighbor IP:Port
     * @return
     */
    public static boolean isNeighborOnline(String neighbor){
        neighbor = neighbor.replace("/","");
        if(onlineNeighbors.containsValue(neighbor)){
            return true;
        }

        return false;
    }

    /**
     * Check if a neighbor is is on the neighbors list.
     * @param neighbor neighbor IP
     * @return
     */
    public static boolean isANeighbor(String neighbor){
        neighbor = neighbor.replace("/","");
        if(allNeighbors.containsKey(neighbor)){
            return true;
        }

        return false;
    }

    /**
     * Add a neighbor to the online list
     * @param neighbor Neighbor IP:Port
     */
    public static void addOnlineNeighbors(String neighbor, String username) {
        neighbor = neighbor.replace("/", "");
        String[] ipPort = neighbor.split(":");
        for (Map.Entry<String, String> item : allNeighbors.entrySet()) {
            if (item.getKey().equals(ipPort[0]) && !onlineNeighbors.containsValue(neighbor)) {
                onlineNeighbors.put(username, neighbor);
            }
        }
    }

    /**
     * Add a neighbor to the online list
     * @param neighbor Neighbor IP:Port
     */
    public static void addBadHmacOnlineNeighbors(String neighbor, String username) {
        neighbor = neighbor.replace("/", "");
        String[] ipPort = neighbor.split(":");
        for (Map.Entry<String, String> item : allNeighbors.entrySet()) {
            if (item.getKey().equals(ipPort[0]) && !badHmacOnlineNeighbors.containsValue(neighbor)) {
                badHmacOnlineNeighbors.put(username, neighbor);
            }
        }
    }

    /**
     * Remove a neighbor from the online list
     * @param neighbor Neighbor IP:Port
     */
    public static void removeBadHmacOnlineNeighbors(String neighbor){
        neighbor = neighbor.replace("/","");
        for (Map.Entry<String,String> item:badHmacOnlineNeighbors.entrySet()) {
            if(item.getValue().equals(neighbor)){
                badHmacOnlineNeighbors.remove(item.getKey(),item.getValue());
                break;
            }
        }
    }

    /**
     * Remove a neighbor from the online list
     * @param neighbor Neighbor IP:Port
     */
    public static void removeOnlineNeighbors(String neighbor){
        neighbor = neighbor.replace("/","");
        for (Map.Entry<String,String> item:onlineNeighbors.entrySet()) {
            if(item.getValue().equals(neighbor)){
                onlineNeighbors.remove(item.getKey(),item.getValue());
                break;
            }
        }
    }

    /**
     * Get a name of an online neighbor.
     * @param ipPort IP:Port
     * @return
     */
    public static String GetOnlineNeighborName(String ipPort)
    {
        ipPort = ipPort.replace("/","");

        for (Map.Entry<String,String> item:onlineNeighbors.entrySet()) {
            if(item.getValue().equals(ipPort)){
                return item.getKey();
            }
        }

        return ipPort;
    }
}
