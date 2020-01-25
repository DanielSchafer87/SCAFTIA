package Services.IncomingCommunication;

import Controllers.MainWindow_Controller;
import Services.*;
import Services.OutgoingCommunication.OutgoingFileThread;
import Services.OutgoingCommunication.OutgoingThread;
import Services.Utilities.AuthenticationDecoder;
import Services.Utilities.ByteManipulation;
import Services.Utilities.MethodHelper;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static Services.Constants.*;

public class IncomingFileThread extends Thread {
    ServerSocket server;
    String  filePath;
    Hasher hasher;
    Settings settings;

    public IncomingFileThread(ServerSocket server , String filePath ) {
        this.server = server;
        this.filePath = filePath;
        hasher = new Hasher();
        settings = new Settings();
    }

    @Override
    public void run()
    {
        // loop variables
        Socket client = null;
        BufferedReader brIn = null;
        final int ivSize = 16;
        final int macSize = 32;
        byte[] iv = new byte[ivSize];
        byte[] hmac = new byte[macSize];
        String incomingHmacString;
        String hmacString;
        String remoteSocketAddress;
        String incomingIP;
        String incomingMessagePort;
        String neighborMessage;
        String neighborFile;
        String incomingMSG = "";
        String decryptedMessage;
        Boolean isToken = true;

        System.out.println("Listening on " + server.getLocalSocketAddress());

        while (!this.isInterrupted())
        {
            // accept a new client and open the streams
            try{
                client = server.accept();
                remoteSocketAddress = client.getRemoteSocketAddress().toString().substring(1);
                incomingIP = InetAddress.getByName(remoteSocketAddress.split(":")[0].trim()).toString().replace("/", "");
                incomingMessagePort = settings.getAllNeighbors().get(incomingIP);
                neighborMessage = incomingIP + ":" + incomingMessagePort;
                neighborFile = incomingIP + ":" + Globals.FILE_TRANSFER_PORT;
                brIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
            }
            catch (IOException ex)
            {
                // problem accepting - probably we got canceled
                if ( this.isInterrupted())
                {
                    // all done!
                    break;
                }
                else {
                    // something weird happened
                    System.out.println("Error accepting new connetion: " + ex.getMessage());
                    break;
                }
            }

            //begin getting messages
            try {
                incomingMSG = brIn.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //split the incoming message
            String[] split = incomingMSG.split(" ");
            String encryptedMessage = split[0];
            String incomingIV = split[1];
            String incomingHmac = split[2];

            //check hmac
            hmac = hasher.encryptHMAC(ByteManipulation.stringToBytes(encryptedMessage),settings.getMACPassword());
            if(!ByteManipulation.bytesToString(hmac).equals(incomingHmac)){
                System.out.println("bad hmac");
            }

            //decrypt the message
            //first time decrypt the token with private key
            //after that decrypt all the messages with the session key.
            if(Globals.IS_TOKEN) {
                decryptedMessage = hasher.decryptMessage(encryptedMessage, settings.getPrivatePassword(), incomingIV);
                Globals.IS_TOKEN = false;
            }
            else {
                decryptedMessage = hasher.decryptMessage(encryptedMessage, ByteManipulation.bytesToString(Globals.SESSION_KEY), incomingIV);
            }
            //split the decrypted message.
            split = decryptedMessage.split(" ");
            String header = split[0];

            /*
            List of steps (see diagram for more info).
            5. token
            6. challenge message - return random nonce Nb encrypted with session key.
            7. response message - return (Nb-1) encrypted with session key.
            8. OK - return OK encrypted with session key.
            9. send the file.
             */
            switch (header) {
                case AUTH_TOKEN:{
                    String sessionKey = split[1];
                    String sender = split[2];
                    new Thread(() -> { AuthenticationDecoder.DecodeToken(sessionKey, sender); }).start();
                    break;
                }
                case AUTH_CHALLENGE:{
                    String nonceNb = split[1];
                    String _ip = incomingIP;
                    new Thread(() -> { AuthenticationDecoder.DecodeChallenge(nonceNb, _ip); }).start();
                    break;
                }
                case AUTH_RESPONSE:{
                    String nonceNb = split[1];
                    String _ip = incomingIP;
                    new Thread(() -> { AuthenticationDecoder.DecodeResponse(nonceNb, _ip); }).start();
                    break;
                }
                case AUTH_OK:{
                    String _ip = incomingIP;
                    new Thread(() -> { AuthenticationDecoder.EncryptAndSendFile(_ip); }).start();
                    break;
                }
                case AUTH_FILE_TRANSFER:{
                    //start getting the file.
                    try {
                        client = server.accept();
                        //incoming encrypted stream
                        BufferedInputStream fileReader = new BufferedInputStream(client.getInputStream());
                        //dynamic byte array
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        int nRead;
                        //buffer for reading form t the stream
                        byte[] data = new byte[1];
                        //read all the bytes.
                        System.out.println("start getting file");
                        while ((nRead = fileReader.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();
                        //get byte array
                        byte[] byteArray = buffer.toByteArray();

                        //close streams
                        fileReader.close();

                        //the file byte array size after taking out the hmac and the iv.
                        byte[] file = new byte[byteArray.length-ivSize-macSize];

                        //split the data into hmac, iv and file.
                        for (int i=0; i < byteArray.length; i++){
                            if(i <= macSize-1){
                                hmac[i] = byteArray[i];
                            }
                            else if(i <= (macSize+ivSize)-1){
                                iv[i-macSize] = byteArray[i];
                            }
                            else{
                                file[i-ivSize-macSize] = byteArray[i];
                            }
                        }

                        //join arrays.
                        byte[] encryptedFileAndIV = MethodHelper.joinArray(iv,file);
                        //create hmac.
                        byte[] encryptedHMAC = hasher.encryptHMAC(encryptedFileAndIV,settings.getMACPassword());
                        hmacString = ByteManipulation.bytesToString(encryptedHMAC);
                        incomingHmacString = ByteManipulation.bytesToString(hmac);
                        //write to log that file getting is complete
                        Log.write("[INCOMING FILE] " +
                                ByteManipulation.bytesToHex(encryptedHMAC)
                                + " " +
                                ByteManipulation.bytesToHex(iv)
                                + " " +
                                filePath
                                + " [From: " +
                                OnlineNeighbors.GetOnlineNeighborName(neighborMessage) + " (" + neighborFile + ")]");

                        //check hmac
                        if(!hmacString.equals(incomingHmacString)){
                            //display alert to user
                            MainWindow_Controller.FinishedGettingFile(filePath, Constants.MSG_FAILED);
                            //return FAILED to sender
                            new OutgoingThread(neighborMessage, settings.getSharedPassword(), settings.getMACPassword(), Constants.MSG_FAILED).start();
                        }
                        else {
                            //decrypts the file
                            byte[] decryptedFile = hasher.decryptFile(iv,file,ByteManipulation.bytesToString(Globals.SESSION_KEY));
                            //make a file from the byte array
                            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                                fos.write(decryptedFile);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //display alert to user
                            MainWindow_Controller.FinishedGettingFile(filePath, Constants.MSG_ACK);
                            //return ACK to sender
                            new OutgoingThread(neighborMessage, settings.getSharedPassword(), settings.getMACPassword(), Constants.MSG_ACK).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.interrupt();
                    break;
                }
                default:{
                    new Thread(() -> {
                        MainWindow_Controller.PrintMessageToScreen("[Message Decryption Failed]", "");
                    }).start();
                    Log.write("[Message Decryption Failed]" + "header: " + header);
                    break;
                }
            }

        }//end of while

        // shut things down
        try { brIn.close(); } catch (Exception ex) {}
        try { client.close(); } catch (Exception ex) {}
    }

}
