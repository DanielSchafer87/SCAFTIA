package Services.IncomingCommunication;

import Controller.SCAFTIA_Controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class IncomingStartStop {

    /**
     * The Server socket (the one to listen on)s
     */
    static ServerSocket server;

    /**
     * Class variables.
     */
    static IncomingThread incomingThread;


    /**
     *  Start incoming thread for file transfer
     * @param myPort port for file transfer
     */
    public static void startIncoming(String ip, int myPort) {

        try {
                server = new ServerSocket(myPort, 50, InetAddress.getByName(ip));
                incomingThread = new IncomingThread(server);
                // start it
                incomingThread.start();
                new Thread(() -> { SCAFTIA_Controller.PrintMessageToScreen("Server started on: " + server.getLocalSocketAddress().toString().replace("/","")); }).start();
        }
        catch (IllegalArgumentException | IOException | SecurityException ex)
        {
            // something is off
            System.out.println("Error starting to listen on " + ip + ":" + myPort + " - " + ex.getMessage());
            System.out.println("Quitting");

            try { server.close(); } catch (Exception e) {}
            //System.exit(1);
        }
    }

    /**
     * Stop incoming thread
     */
    public static void stopIncoming()
    {
        //interrupt incoming thread by type
        //stop the incoming thread.
        incomingThread.interrupt();
        try { server.close(); } catch (Exception ex) {}
        new Thread(() -> { SCAFTIA_Controller.PrintMessageToScreen("Server stopped");}).start();
    }

}
