package Services.IncomingCommunication;

import Controllers.MainWindow_Controller;
import Services.*;
import Services.OutgoingCommunication.OutgoingServer;
import Services.OutgoingCommunication.OutgoingThread;
import Services.Utilities.ByteManipulation;
import Services.Utilities.MethodHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class IncomingHandler extends Thread {

    Socket client;
    Settings settings;
    Hasher hasher;

    public IncomingHandler(Socket client){
        settings = new Settings();
        hasher = new Hasher();
        this.client = client;
    }

    @Override
    public void run(){
        BufferedReader brIn = null;
        String iv = "";
        String incomingIP = null;
        String header = "";
        String ivHex = "";
        String hmacHex;
        String neighbor = "";
        String encryptedMessage = "";
        String incomingHmacString = "";
        String hmacString = "";
        String logHeader = "";
        String remoteSocketAddress = "";
        String incomingMSG;
        String[] splittedIncomingMSG = {};
        byte[] headerBytes = {};
        byte ivBytes[] = {};
        boolean isHmacCorrect = true;
        int incomingPort = 0;

        try {
            remoteSocketAddress = client.getRemoteSocketAddress().toString().substring(1);
            brIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException ex) {
            System.out.println("Error accepting new connection or opening streams: " + ex.getMessage());
        }

        //Read incoming message.
        try {
            incomingMSG = brIn.readLine();
            splittedIncomingMSG = incomingMSG.split(" ");
            iv = splittedIncomingMSG[1];
            ivBytes = ByteManipulation.stringToBytes(iv);
            ivHex = ByteManipulation.bytesToHex(ByteManipulation.stringToBytes(iv));
            headerBytes = ByteManipulation.stringToBytes(splittedIncomingMSG[0]);
            incomingIP = InetAddress.getByName(remoteSocketAddress.split(":")[0].trim()).toString().replace("/", "");
            //incomingPort = client.getLocalPort();
            incomingPort = Integer.valueOf(settings.getAllNeighbors().get(incomingIP));
            neighbor = incomingIP + ":" + incomingPort;
        } catch (IOException | NullPointerException ex) {
            // something went wrong here, just ignore this one
        }

        //decrypted header
        header = hasher.decryptMessage(splittedIncomingMSG[0], settings.getSharedPassword(), iv);

        //calculate hmac
        //header[0] iv[1] hmac[2]
        if (splittedIncomingMSG.length == 3) {
            incomingHmacString = splittedIncomingMSG[2];
            byte[] combined = MethodHelper.joinArray(ivBytes, headerBytes);
            byte[] encryptedHMAC = hasher.encryptHMAC(combined, settings.getMACPassword());
            hmacHex = ByteManipulation.bytesToHex(encryptedHMAC);
            hmacString = ByteManipulation.bytesToString(encryptedHMAC);
        }
        //header[0] iv[1] messgae[2] hmac[3]
        else {
            encryptedMessage = splittedIncomingMSG[2];
            incomingHmacString = splittedIncomingMSG[3];
            byte[] messageBytes = ByteManipulation.stringToBytes(encryptedMessage);
            byte[] combined = MethodHelper.joinArray(ivBytes, headerBytes, messageBytes);
            byte[] encryptedHMAC = hasher.encryptHMAC(combined, settings.getMACPassword());
            hmacHex = ByteManipulation.bytesToHex(encryptedHMAC);
            hmacString = ByteManipulation.bytesToString(encryptedHMAC);
        }

        //check hmac
        if (!hmacString.equals(incomingHmacString)) {
            //set hmac to false
            isHmacCorrect = false;
            //log header if the hmac is wrong.
            logHeader = "[INCOMING - Invalid]";
            String _neighbor = neighbor;
            new Thread(() -> {
                MainWindow_Controller.PrintMessageToScreen("[Bad Message]", _neighbor);
            }).start();
        }
        else{
            //log header if the hmac is correct.
            logHeader = "[INCOMING - Valid]";
            //set hmac flag to true
            isHmacCorrect = true;
        }

        //Check if the incoming message starts with HELLO
        //or HELLO[0] iv[1] username - First / Second[2] hmac[3]
        if (header.equals(Constants.MSG_HELLO)) {

            String decryptMessage = hasher.decryptMessage(encryptedMessage, settings.getSharedPassword(), iv);
            String username = decryptMessage.split(" ")[0];
            String firstOrSecond = decryptMessage.split(" ")[1];

            //hello that requires an HELLO back
            if (firstOrSecond.equals(Constants.HELLO_FIRST)) {
                try {
                    //send back HELLO
                    new OutgoingThread(neighbor, settings.getSharedPassword(), settings.getMACPassword(), Constants.MSG_HELLO, Globals.MY_USERNAME + " " + Constants.HELLO_SECOND).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //do not add "neighbor" that are on the neighbors list.
            if(OnlineNeighbors.isANeighbor(incomingIP)) {
                //if HMAC is correct add the neighbor to GUI
                if (isHmacCorrect) {
                    //add the neighbor to the online list.
                    OnlineNeighbors.addOnlineNeighbors(neighbor, username);
                    new Thread(() -> {
                        MainWindow_Controller.updateOnlineNeighbors();
                    }).start();
                } else {
                    OnlineNeighbors.addBadHmacOnlineNeighbors(neighbor, username);
                }

                Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " " + username + " " + firstOrSecond + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
            }
        }
        //Check if the incoming message starts with BYE
        else if (header.equals(Constants.MSG_BYE)) {

            //if HMAC is correct add the neighbor to GUI
            if(isHmacCorrect) {
                //check if the neighbor is online before removing
                if (OnlineNeighbors.isNeighborOnline(neighbor)) {
                    OnlineNeighbors.removeOnlineNeighbors(neighbor);
                }
                new Thread(() -> {
                    MainWindow_Controller.updateOnlineNeighbors();
                }).start();
            }
            else{
                OnlineNeighbors.removeBadHmacOnlineNeighbors(neighbor);
            }

            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }
        //Check if the incoming message starts with MESSAGE
        // MESSAGE[0] iv[1] enctyptedMessage[2]
        else if (header.equals(Constants.MSG_MESSAGE)) {
            //display message only if the hmac is correct
            String decryptedMessage = hasher.decryptMessage(encryptedMessage, settings.getSharedPassword(), iv);

            //display content of the message only if the hmac is correct
            if(isHmacCorrect) {
                String _neighbor = neighbor;
                new Thread(() -> {
                    MainWindow_Controller.PrintMessageToScreen(decryptedMessage, _neighbor);
                }).start();
            }

            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " " + decryptedMessage + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }
        //Check if the incoming message starts with SENDFILE
        // SENDFILE[0] iv[1] encryptedFilename[2]
        else if (header.equals(Constants.MSG_SENDFILE)) {
            String decryptedFilename = hasher.decryptMessage(encryptedMessage, settings.getSharedPassword(), iv);
            String _neighbor = neighbor;
            new Thread(() -> {
                MainWindow_Controller.SaveFile(decryptedFilename, _neighbor);
            }).start();
            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " " + decryptedFilename + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }
        //Check if the incoming message starts with OK
        // OK[0] iv[1] newPortForFileTransfer[2]
        else if (header.equals(Constants.MSG_OK)) {
            String decryptedNewPortForFileTransfer = hasher.decryptMessage(encryptedMessage, settings.getSharedPassword(), iv);
            String fromNeighbor = OnlineNeighbors.GetOnlineNeighborName(neighbor);
            Globals.FILE_TRANSFER_PORT = Integer.parseInt(decryptedNewPortForFileTransfer);

            Globals.IS_TOKEN = false;
            IncomingStartStop.startIncoming(Globals.FILE_TRANSFER_PORT,"" , Constants.INCOMING_FILE);

            String message = MethodHelper.CreateMessageForServer(Globals.MY_USERNAME,fromNeighbor,settings.getMACPassword());
            new OutgoingServer(message).start();

            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " " + decryptedNewPortForFileTransfer + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }
        //Check if the incoming message starts with NO
        // NO[0]
        else if (header.equals(Constants.MSG_NO)) {
            String _neighbor = neighbor;
            new Thread(() -> {
                MainWindow_Controller.DeclineSendFile(_neighbor);
            }).start();

            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }
        //Check if the incoming message starts with ACK
        else if (header.equals(Constants.MSG_ACK)) {
            new Thread(() -> {
                MainWindow_Controller.FinishedSendingFile(Constants.MSG_ACK);
            }).start();

            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }
        //Check if the incoming message starts with FAILED
        else if (header.equals(Constants.MSG_FAILED)) {
            new Thread(() -> {
                MainWindow_Controller.FinishedSendingFile(Constants.MSG_FAILED);
            }).start();

            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }
        //Check if the incoming message starts with FAILED
        else if (header.equals(Constants.MSG_SERVER_RESPONSE)) {
            Log.write(logHeader + " " + hmacHex + " " + ivHex + " " + header + " [From: " + OnlineNeighbors.GetOnlineNeighborName(neighbor) + " (" + neighbor + ")]");
        }

        // shut things down
        try { brIn.close(); } catch (Exception ex) {}
        try { client.close(); } catch (Exception ex) {}
    }
}
