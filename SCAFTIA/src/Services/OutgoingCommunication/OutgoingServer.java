package Services.OutgoingCommunication;

import Controllers.MainWindow_Controller;
import Services.Constants;
import Services.Globals;
import Services.Log;
import Services.Settings;
import Services.Utilities.AuthenticationDecoder;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class OutgoingServer extends Thread{

    String IP = "";
    String message;
    int Port;

    public OutgoingServer(String message){
        Settings settings = new Settings();
        String[] ipPort = settings.getServerAddress().split(":");
        this.IP = ipPort[0];
        this.Port = Integer.parseInt(ipPort[1]);
        this.message = message;
    }

    @Override
    public void run() {
        Socket client = null;
        PrintWriter pwOut =  null;
        BufferedReader brIn = null;
        String response;
        try {
            // now open a conversation with the server
            InetAddress neighborAddress = InetAddress.getByName(IP);
            // build a socket and it connects automatically
            client = new Socket(neighborAddress, Port);
            //close the connection if the server does not respond within 30 sec.
            client.setSoTimeout(Constants.SERVER_LISTENER_TIMEOUT);
            // send the message
            pwOut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
            brIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
            pwOut.write(message + "\n");
            pwOut.flush();

            response = brIn.readLine();
            //split the response
            String[] split = response.split(" ");
            new Thread(() -> { AuthenticationDecoder.DecodeResponseFromServer(split[0], split[1], split[2]); }).start();

        } catch (Exception e){
            new Thread(() -> {
                MainWindow_Controller.PrintMessageToScreen("[Authentication Server is Not Available]", "");
            }).start();
            Log.write("[File Transfer Failed - Authentication Server is Not Available]");
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
