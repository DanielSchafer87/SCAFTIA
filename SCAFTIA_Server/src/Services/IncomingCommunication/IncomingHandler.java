package Services.IncomingCommunication;

import Controller.SCAFTIA_Controller;
import Services.*;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.Socket;

import static Controller.SCAFTIA_Controller.IsSelectedCustomResponse;
import static Services.Constants.TOKEN_HEADER;

public class IncomingHandler extends Thread {

    Socket client;
    Settings settings;
    Hasher hasher;

    public IncomingHandler(Socket client){
        this.client = client;
        settings = new Settings();
        hasher = new Hasher();
    }

    @Override
    public void run(){
        String incomingHmacString;
        String hmacString;
        String senderPassword;
        String receiverPassword;
        BufferedReader brIn = null;
        PrintWriter pwOut = null;
        String logHeader = "";
        String remoteSocketAddress = "";
        String incomingMSG = "";
        String[] splittedIncomingMSG = {};
        boolean isResponseFake = false;

        try {
            remoteSocketAddress = client.getRemoteSocketAddress().toString().split(":")[0].trim().replace("/", "");
            brIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
            pwOut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException ex) {
            System.out.println("Error accepting new connection or opening streams: " + ex.getMessage());
        }

        //Read incoming message.
        try {
            incomingMSG = brIn.readLine();
        } catch (IOException | NullPointerException ex) {
            // something went wrong here, just ignore this one
        }

        //sort the incoming message
        //sender[0] receiver[1] nonce[2] hmac[3]
        splittedIncomingMSG = incomingMSG.split(" ");
        String sender = splittedIncomingMSG[0];
        String receiver = splittedIncomingMSG[1];
        String nonce = splittedIncomingMSG[2];
        incomingHmacString = splittedIncomingMSG[3];

        //check if the sender exist on the server
        if(settings.getAllUsers().containsKey(sender)) {senderPassword = settings.getUserPassword(sender);}
        else {
            //print failure message to screen
            new Thread(() -> {
                SCAFTIA_Controller.PrintMessageToScreen("Session key request failed (unknown sender: " + sender + ")");
            }).start();
            Log.write(logHeader,remoteSocketAddress,sender,receiver,nonce,Constants.ERROR_UNKNOWN_SENDER,false,false,isResponseFake);
            return;
        }
        //check if the receiver exist on the server
        if(settings.getAllUsers().containsKey(sender)) {receiverPassword = settings.getUserPassword(receiver);}
        else {
            String _receiver = receiver;
            //print failure message to screen
            new Thread(() -> {
                SCAFTIA_Controller.PrintMessageToScreen("Session key request failed (unknown receiver " + _receiver + ")");
            }).start();
            Log.write(logHeader,remoteSocketAddress,sender,receiver,nonce,Constants.ERROR_UNKNOWN_RECIPIENT,false,false,isResponseFake);
            return;
        }

        /*
         * Calculate and check the HMAC
         */
        byte[] senderBytes  = sender.getBytes();
        byte[] receiverBytes = receiver.getBytes();
        byte[] nonceBytes = String.valueOf(nonce).getBytes();
        byte[] combined = MethodHelper.joinArray(senderBytes, receiverBytes, nonceBytes);
        byte[] encryptedHMAC = hasher.encryptHMAC(combined, settings.getMacPassword());
        hmacString = ByteManipulation.bytesToString(encryptedHMAC);
        //check the hmac
        if(!incomingHmacString.equals(hmacString)){
            //log header if the hmac is wrong.
            logHeader = "Invalid Message:";
            //print failure message to screen
            String _receiver = receiver;
            new Thread(() -> { SCAFTIA_Controller.PrintMessageToScreen("Session key request failed (from " + sender + " to " + _receiver +")"); }).start();
            Log.write(logHeader,remoteSocketAddress,sender,receiver,nonce,Constants.ERROR_BAD_HMAC,false,false,isResponseFake);
            return;
        }
        else{
            //log header if the hmac is wrong.
            logHeader = "Valid Message:";
        }

        /*
         * Check if invalid nonce custom responses is selected
         * if selected, generate new nonce.
         */
        if(IsSelectedCustomResponse(Constants.INVALID_NONCE)){
            isResponseFake = true;
            nonce = String.valueOf(MethodHelper.generateNonce());
        }
        /*
         * Check if invalid target name custom responses is selected
         * if selected, select other receiver
         */
        else if(IsSelectedCustomResponse(Constants.INVALID_TARGET_NAME)){
            isResponseFake = true;
            for (String user:settings.getAllUsers().keySet()) {
                /*
                if(!user.equals(sender) && !user.equals(receiver)){
                    receiver = user;
                }
                */
                if(!user.equals(receiver)){
                    receiver = user;
                    break;
                }
            }
        }
        /*
         * Check if Encrypt with Wrong Requestor password responses is selected
         * if selected, generate a random password.
         */
        else if(IsSelectedCustomResponse(Constants.ENCRYPT_WRONG_REQUESTOR)){
            isResponseFake = true;
            String randomPassword = ByteManipulation.bytesToString(hasher.generateSessionKey());
            senderPassword = hasher.encryptPassword(randomPassword);
        }
        /*
         * Check if Encrypt with Wrong Recipient password responses is selected
         * if selected, get password of other user.
         */
        else if(IsSelectedCustomResponse(Constants.ENCRYPT_WRONG_RECIPIENT)){
            isResponseFake = true;
            for (String user:settings.getAllUsers().keySet()) {
                /*
                if(!user.equals(sender) && !user.equals(receiver)){
                    receiverPassword = settings.getUserPassword(user);
                }
                */
                if(!user.equals(receiver)){
                    receiverPassword = settings.getUserPassword(user);
                }
            }
        }



        /*
         * Build the token
         */
        //generate iv for the token encryption.
        IvParameterSpec tokenIV = hasher.generateIV();
        String tokenIVHex = ByteManipulation.bytesToHex(tokenIV.getIV());
        //session key
        byte[] Ks = hasher.generateSessionKey();
        //the token
        String token = TOKEN_HEADER + " " + ByteManipulation.bytesToString(Ks) + " " + sender;
        //encrypt the token.
        byte[] encryptedToken = hasher.encryptMessage(token,receiverPassword,tokenIV);
        //create the hmac for token
        byte[] tokenHMAC = hasher.encryptHMAC(encryptedToken,settings.getMacPassword());

        /*
         * Build the outer message
         */
        String outerMessage =
                ByteManipulation.bytesToString(tokenIV.getIV()) +
                        " " +
                        ByteManipulation.bytesToString(tokenHMAC) +
                        " " +
                        ByteManipulation.bytesToString(encryptedToken) +
                        " " +
                        ByteManipulation.bytesToString(Ks) +
                        " " +
                        nonce +
                        " " +
                        receiver;
        //outer message iv.
        IvParameterSpec outerMessageIV = hasher.generateIV();
        //encrypt the outer message.
        byte[] encryptedOuterMessage = hasher.encryptMessage(outerMessage,senderPassword,outerMessageIV);
        //create the hmac for the outer message.
        byte[] outerMessageHMAC = hasher.encryptHMAC(encryptedOuterMessage,settings.getMacPassword());

        String message =
                        ByteManipulation.bytesToString(outerMessageIV.getIV()) +
                        " " +
                        ByteManipulation.bytesToString(outerMessageHMAC) +
                        " " +
                        ByteManipulation.bytesToString(encryptedOuterMessage);

        //print success message to screen
        String _receiver = receiver;
        new Thread(() -> { SCAFTIA_Controller.PrintMessageToScreen("Session key request successful (from " + sender + " to " + _receiver +")"); }).start();
        Log.write(logHeader,remoteSocketAddress,sender,receiver,nonce,"",false,true,isResponseFake);

        pwOut.write(message + "\n");
        pwOut.flush();

        // shut things down
        try { brIn.close(); } catch (Exception ex) {}
        try { pwOut.close(); } catch (Exception ex) {}
        try { client.close(); } catch (Exception ex) {}
    }
}
