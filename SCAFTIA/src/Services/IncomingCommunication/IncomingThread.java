package Services.IncomingCommunication;

import Controllers.MainWindow_Controller;
import Services.*;
import Services.OutgoingCommunication.OutgoingThread;
import Services.OutgoingCommunication.OutgoingServer;
import Services.Utilities.ByteManipulation;
import Services.Utilities.MethodHelper;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class IncomingThread extends Thread {

    ServerSocket server;
    Hasher hasher;
    Settings settings;

    public IncomingThread(ServerSocket server) {
        hasher = new Hasher();
        settings = new Settings();
        this.server = server;
    }

    @Override
    public void run()
    {
        // loop variables
        Socket client = null;

        System.out.println("Listening on " + server.getLocalSocketAddress());

        while (!this.isInterrupted()) {
            // accept a new client and open the streams
            try {
                client = server.accept();
            } catch (IOException ex) {
                // problem accepting - probably we got canceled
                if (this.isInterrupted()) {
                    // all done!
                    break;
                } else {
                    // something weird happened
                    System.out.println("Error accepting new connection: " + ex.getMessage());
                    break;
                }
            }

            new IncomingHandler(client).start();
        }
    }

}
