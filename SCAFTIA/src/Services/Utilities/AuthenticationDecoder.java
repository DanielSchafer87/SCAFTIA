package Services.Utilities;

import Controllers.MainWindow_Controller;
import Services.*;
import Services.OutgoingCommunication.OutgoingFileThread;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import static Services.Utilities.MethodHelper.IsSelectedCustomMessage;

public class AuthenticationDecoder {

    public static void DecodeResponseFromServer(String incomingIV,  String incomingHmac, String encryptedMessage){
        Hasher hasher = new Hasher();
        Settings settings = new Settings();
        InetAddress neighborAddress = null;
        String receiverIPPort = "";

        //check outer message hmac
        byte[] hmac = hasher.encryptHMAC(ByteManipulation.stringToBytes(encryptedMessage),settings.getMACPassword());

        if(!ByteManipulation.bytesToString(hmac).equals(incomingHmac)){
            new Thread(() -> {
                MainWindow_Controller.PrintMessageToScreen("[Bad Message From Server]", "");
            }).start();
            Log.write("[Invalid Outer Message]" + " " + ByteManipulation.bytesToHex(ByteManipulation.stringToBytes(incomingHmac)));
            return;
        }
        Log.write("[Valid Outer Message]" + " " + ByteManipulation.bytesToHex(ByteManipulation.stringToBytes(incomingHmac)));

        //decrypt the outer message
        String decryptedOuterMessage = hasher.decryptMessage(encryptedMessage,settings.getPrivatePassword(),incomingIV);
        String[] split = decryptedOuterMessage.split(" ");

        //check if the nonce appears in the plaintext of the outer message.
        if(!decryptedOuterMessage.contains(String.valueOf(Globals.NONCEa)))
        {
            new Thread(() -> {
                MainWindow_Controller.PrintMessageToScreen("[Bad Nonce From Server]", "");
            }).start();
            Log.write("[Invalid Nonce in Outer Message]" + " " + Globals.NONCEa);
            return;
        }
        Log.write("[Valid Nonce in Outer Message]" + " " + Globals.NONCEa);

        String tokenIV = split[0];
        String tokenHmac = split[1];
        String encryptedToken = split[2];
        Globals.SESSION_KEY = ByteManipulation.stringToBytes(split[3]);
        String receiver = split[5];
        String messageToReceiver = "";

        if(IsSelectedCustomMessage(Constants.RANDOM_TOKEN)){
            /*
             * Build a random token
             */
            //generate iv for the token encryption.
            IvParameterSpec randomTokenIV = hasher.generateIV();
            //session key
            byte[] Ks = hasher.generateSessionKey();
            //encrypt the token.
            byte[] encryptedRandomToken = hasher.generateSessionKey();
            //create the hmac for token
            byte[] randomTokenHMAC = hasher.encryptHMAC(encryptedRandomToken,settings.getMACPassword());

            messageToReceiver = ByteManipulation.bytesToString(encryptedRandomToken) + " " + ByteManipulation.bytesToString(randomTokenIV.getIV()) + " " + ByteManipulation.bytesToString(randomTokenHMAC);
        }
        else {
            //build message to the receiver
            messageToReceiver = encryptedToken + " " + tokenIV + " " + tokenHmac;
        }


        //check if toggles of invalid requestor or invalid receiver are selected.
        if(IsSelectedCustomMessage(Constants.TOKEN_TO_WRONG_USER)){
            Map<String,String> allNeighbors = settings.getAllNeighbors();
            for (String key: allNeighbors.keySet())
            {
                if(!key.equals(receiver)){
                    receiverIPPort = allNeighbors.get(key);
                }
            }
        }
        else {

            receiverIPPort = OnlineNeighbors.GetOnlineNeighborIPPort(receiver);
            if (receiverIPPort.equals("")) {
                new Thread(() -> {
                    MainWindow_Controller.PrintMessageToScreen("[Invalid Receiver of Token]", "");
                }).start();
                Log.write("[Invalid Receiver of Token]" + " " + receiver);
                return;
            }
        }

        try {
            neighborAddress = InetAddress.getByName(receiverIPPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        SendResponse(neighborAddress,messageToReceiver);
    }

    public static void DecodeToken(String sessionKey, String sender){
        InetAddress neighborAddress = null;

        Globals.SESSION_KEY = ByteManipulation.stringToBytes(sessionKey);

        //create a new nonce (Nb)
        Globals.NONCEb = MethodHelper.generateNonce();
        //build the challenge message
        String challengeMessage = Constants.AUTH_CHALLENGE + " " + String.valueOf(Globals.NONCEb);

        String messageToSend = BuildMessage(challengeMessage, Constants.AUTH_CHALLENGE);

        try {
            neighborAddress = InetAddress.getByName(OnlineNeighbors.GetOnlineNeighborIPPort(sender));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        SendResponse(neighborAddress,messageToSend);
    }

    public static void DecodeChallenge(String nonceNbString, String ip){
        InetAddress neighborAddress = null;
        long nonceNb = Long.valueOf(nonceNbString);
        Globals.NONCEb = nonceNb;

        if(IsSelectedCustomMessage(Constants.ENCRYPT_WITH_WRONG_NUMERICAL_RESPONSE)){
            nonceNb = MethodHelper.generateNonce();
        }
        else{
            nonceNb  = nonceNb - 1;
        }

        //build the challenge message
        String responseMessage = Constants.AUTH_RESPONSE + " " + String.valueOf(nonceNb);

        String messageToSend = BuildMessage(responseMessage, Constants.AUTH_RESPONSE);

        try {
            neighborAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        SendResponse(neighborAddress,messageToSend);
    }

    public static void DecodeResponse(String newNonceNbString, String ip) {
        InetAddress neighborAddress = null;
        long newNonce = Long.valueOf(newNonceNbString);
        long nonce = Globals.NONCEb - 1;

        if(newNonce != nonce){
            new Thread(() -> {
                MainWindow_Controller.PrintMessageToScreen("[Invalid Token]", "");
            }).start();
            Log.write("[Invalid Token]" + "actual Nb-1: " + nonce + ", received " + newNonce);
            return;
        }

        //build the challenge message
        String responseMessage = Constants.AUTH_OK;

        String messageToSend = BuildMessage(responseMessage, Constants.AUTH_OK);

        try {
            neighborAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        SendResponse(neighborAddress,messageToSend);
    }

    public static void EncryptAndSendFile(String neighborIP){
        InetAddress neighborAddress = null;
        Settings settings = new Settings();
        Hasher hasher = new Hasher();

        String neighbor = neighborIP+":"+Globals.FILE_TRANSFER_PORT;
        IvParameterSpec iv = hasher.generateIV();
        byte[] encryptedFile = {};
        if(IsSelectedCustomMessage(Constants.ENCRYPT_FILE_WITH_WRONG_KEY)) {
            encryptedFile = hasher.encryptFile(Globals.FILE_PATH_TO_SEND, settings.getPrivatePassword(), iv);
        }
        else {
            encryptedFile = hasher.encryptFile(Globals.FILE_PATH_TO_SEND, ByteManipulation.bytesToString(Globals.SESSION_KEY), iv);
        }
        byte[] ivBytes = iv.getIV();
        byte[] encryptedFileAndIV = MethodHelper.joinArray(ivBytes,encryptedFile);
        byte[] encryptedHMAC = hasher.encryptHMAC(encryptedFileAndIV,settings.getMACPassword());
        byte[] dataToSend = MethodHelper.joinArray(encryptedHMAC,encryptedFileAndIV);

        Log.write("[OUTGOING FILE] " +
                ByteManipulation.bytesToHex(encryptedHMAC)
                + " " +
                ByteManipulation.bytesToHex(ivBytes)
                + " " +
                Globals.FILE_NAME_TO_SEND
                + " [To: " +
                OnlineNeighbors.GetOnlineNeighborName(neighbor.split(":")[0]+":"+OnlineNeighbors.GetOnlineNeighborsIPPort().get(neighbor.split(":")[0])) + " (" + neighbor + ")]");

        //build the challenge message
        String responseMessage = Constants.AUTH_FILE_TRANSFER;
        String messageToSend = BuildMessage(responseMessage, Constants.AUTH_FILE_TRANSFER);
        try {
            neighborAddress = InetAddress.getByName(neighborIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        SendResponse(neighborAddress,messageToSend);

        //send the file.
        new OutgoingFileThread(neighbor , dataToSend).start();
    }

    private static String BuildMessage(String message, String messageType){
        Hasher hasher = new Hasher();
        Settings settings = new Settings();

        //create iv
        IvParameterSpec messageIV = hasher.generateIV();
        //encrypt the message with the session key

        byte[] encryptedMessage = {};

        //when Nb is first encrypted with the sesssion key
        if(IsSelectedCustomMessage(Constants.ENCRYPT_NONCE_WITH_WRONG_KEY) && messageType.equals(Constants.AUTH_CHALLENGE)) {
            encryptedMessage = hasher.encryptMessage(message, ByteManipulation.bytesToString(hasher.generateSessionKey()), messageIV);
        }
        //when Nb is first encrypted with the sesssion key
        else if(IsSelectedCustomMessage(Constants.ENCRYPT_WITH_WRONG_KEY) && messageType.equals(Constants.AUTH_RESPONSE)) {
            encryptedMessage = hasher.encryptMessage(message, settings.getPrivatePassword(), messageIV);
        }
        else {
            encryptedMessage = hasher.encryptMessage(message, ByteManipulation.bytesToString(Globals.SESSION_KEY), messageIV);
        }

        //create hmac
        byte[] messageHmac = hasher.encryptHMAC(encryptedMessage,settings.getMACPassword());

        String messageToSend = ByteManipulation.bytesToString(encryptedMessage)
                + " " +
                ByteManipulation.bytesToString(messageIV.getIV())
                + " " +
                ByteManipulation.bytesToString(messageHmac);

        return messageToSend;
    }

    private static void SendResponse(InetAddress neighborAddress ,String message){
        Socket client = null;
        PrintWriter pwOut = null;

        // build a socket and it connects automatically
        try {
            client = new Socket(neighborAddress, Globals.FILE_TRANSFER_PORT);
            pwOut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pwOut.write(message + "\n");
        pwOut.flush();

        if(client != null) {
            try {
                client.close();
                pwOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
