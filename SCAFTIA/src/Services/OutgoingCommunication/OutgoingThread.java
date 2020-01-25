package Services.OutgoingCommunication;

import Services.Constants;
import Services.Globals;
import Services.Log;
import Services.Utilities.MethodHelper;
import Services.Settings;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class OutgoingThread extends Thread {
    String encryptedMessage = "";
    String sharedPassword = "";
    String header = "";
    String message = "";
    String ivHex;
    String hmacHex;
    Map<String,String> neighbors;
    Settings settings = new Settings();

    /**
     * Constructor for sending a message (including header and message) for multiple neighbors.
     * @param neighbors map of neighbors to get the message.
     * @param sharedPassword the shared password.
     * @param macPassword the hmac password.
     * @param header header of the message (HELLO, MESSAGE, ....)
     * @param message the actual message.
     */
    public OutgoingThread(Map<String,String> neighbors, String sharedPassword, String macPassword,  String header, String message){
        if(message.equals(""))
            encryptedMessage = MethodHelper.CreateMessage(header,sharedPassword, macPassword);
        else
            encryptedMessage = MethodHelper.CreateMessage(header,message,sharedPassword, macPassword);

        ivHex = MethodHelper.ivHex;
        hmacHex = MethodHelper.hmacHex;
        this.sharedPassword = sharedPassword;
        this.neighbors = neighbors;
        this.message = message;
        this.header = header;
    }

    /**
     * Constructor for sending a message (including header and message) to a single neighbor.
     * @param neighbor neighbor to get the message.
     * @param sharedPassword the shared password.
     * @param macPassword the hmac password.
     * @param header header of the message (HELLO, MESSAGE, ....)
     * @param message the actual message.
     */
    public OutgoingThread(String neighbor, String sharedPassword, String macPassword, String header, String message){
        if(message.equals(""))
            encryptedMessage = MethodHelper.CreateMessage(header,sharedPassword, macPassword);
        else
            encryptedMessage = MethodHelper.CreateMessage(header,message,sharedPassword, macPassword);

        ivHex = MethodHelper.ivHex;
        hmacHex = MethodHelper.hmacHex;
        this.sharedPassword = sharedPassword;
        this.message = message;
        this.header = header;

        neighbor = neighbor.replace("/","");
        String[] ipPort = neighbor.split(":");
        neighbors = new HashMap<>();
        this.neighbors.put(ipPort[0],ipPort[1]);
    }

    /**
     * Constructor for sending a message (with header only) for multiple neighbors.
     * @param neighbors map of neighbors to get the message.
     * @param sharedPassword the shared password.
     * @param macPassword the hmac password.
     * @param header header of the message (HELLO, MESSAGE, ....)
     */
    public OutgoingThread(Map<String,String> neighbors, String sharedPassword, String macPassword, String header){
        this(neighbors,sharedPassword,macPassword,header,"");
    }

    /**
     * Constructor for sending a message (with header only) to a single neighbor.
     * @param neighbor neighbor to get the message.
     * @param sharedPassword the shared password.
     * @param macPassword the hmac password.
     * @param header header of the message (HELLO, MESSAGE, ....)
     */
    public OutgoingThread(String neighbor, String sharedPassword, String macPassword, String header){
        this(neighbor,sharedPassword,macPassword,header,"");
    }

    @Override
    public void run() {
        Socket client = null;
        PrintWriter pwOut =  null;
                        for (String neighborIP: neighbors.keySet()) {
                            try {
                            // now open a conversation with the neighbor
                            InetAddress localhost = InetAddress.getLocalHost();
                            InetAddress neighborAddress = InetAddress.getByName(neighborIP);
                            int port = Integer.parseInt(neighbors.get(neighborIP));
                            Log.write("[OUTGOING] " +
                                    hmacHex
                                    + " " +
                                    ivHex
                                    + " " +
                                    header
                                    + " " +
                                    message
                                    + " [From: " +
                                    Globals.MY_USERNAME
                                    + " (" +
                                    (localhost.getHostAddress()).trim() + ":" +settings.getMyPort()
                                    + ") To: (" +
                                    neighborIP + ":" + neighbors.get(neighborIP)
                                    + ")]");
                            // build a socket and it connects automatically
                            client = new Socket(neighborAddress, port);
                            // send the message
                            pwOut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                            pwOut.write(encryptedMessage + "\n");
                            pwOut.flush();
                            } catch (IOException e){
                             //e.printStackTrace();
                            }
                        }
            try {
                if(client != null) {
                    client.close();
                    pwOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
