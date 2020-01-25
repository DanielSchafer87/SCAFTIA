package Services.IncomingCommunication;

import Services.Constants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class IncomingStartStop {

    /**
     * The Server socket (the one to listen on)s
     */
    static ServerSocket server;
    static ServerSocket serverFile;

    /**
     * Class variables.
     */
    static IncomingThread incomingThread;
    static IncomingFileThread incomingFileThread;


    /**
     *  Start incoming thread for file transfer
     * @param myPort port for file transfer
     * @param filePath file path of the file to send or username
     */
    public static void startIncoming(int myPort ,String filePath, String incomingType) {
        // start the incoming listener
        serverFile = null;

        try {
            //start incoming messages thread.
            if(incomingType.equals(Constants.INCOMING_MESSAGE))
            {
                server = new ServerSocket(myPort, 50, InetAddress.getByAddress(new byte[] {0,0,0,0}));
                incomingThread = new IncomingThread(server);
                // start it
                incomingThread.start();
            }
            //start incoming files thread.
            else if(incomingType.equals(Constants.INCOMING_FILE))
            {
                serverFile = new ServerSocket(myPort, 50, InetAddress.getByAddress(new byte[] {0,0,0,0}));
                incomingFileThread = new IncomingFileThread(serverFile, filePath);
                // start it
                incomingFileThread.start();
            }
        }
        catch (IllegalArgumentException | IOException | SecurityException ex)
        {
            // something is off
            System.out.println("Error starting to listen on 0.0.0.0:" + myPort + ": " + ex.getMessage());
            System.out.println("Quitting");

            try { serverFile.close(); } catch (Exception e) {}
            //System.exit(1);
        }
    }

    /**
     * Stop incoming thread
     * @param incomingType type of incoming thread to stop general of file transfer.
     */
    public static void stopIncoming(String incomingType)
    {

        //interrupt incoming thread by type
        if(incomingType.equals(Constants.INCOMING_MESSAGE))
        {
            //stop the incoming thread.
            incomingThread.interrupt();
            try { server.close(); } catch (Exception ex) {}

        }
        else if(incomingType.equals(Constants.INCOMING_FILE))
        {
            //stop the incoming thread.
            incomingFileThread.interrupt();
            try { serverFile.close(); } catch (Exception ex) {}
        }

    }


}
