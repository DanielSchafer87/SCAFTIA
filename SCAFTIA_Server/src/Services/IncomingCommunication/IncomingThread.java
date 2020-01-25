package Services.IncomingCommunication;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class IncomingThread extends Thread {

    ServerSocket server;

    public IncomingThread(ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run()
    {
        // loop variables
        Socket client = null;

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
