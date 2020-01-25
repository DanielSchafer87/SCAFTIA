package Services.Utilities;

import Services.Constants;
import Services.Globals;
import Services.Hasher;

import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

public  class MethodHelper {

    public static String ivHex;
    public static String hmacHex;

    /**
     * Get a random number between 2 numbers
     * @param min
     * @param max
     * @return
     */
    public static int GetRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * Create an encrypted message with just header.
     * @param header the header
     * @param sharedPassword shared password
     * @return an encrypted message according to the format.
     */
    public static String CreateMessage(String header, String sharedPassword, String macPassword){
        Hasher hasher = new Hasher();
        //get a random iv.
        IvParameterSpec iv = hasher.generateIV();
        ivHex = ByteManipulation.bytesToHex(iv.getIV());
        //encrypt the header.
        byte[] encryptedHeader = hasher.encryptMessage(header,sharedPassword,iv);
        //get the iv in bytes.
        byte[] ivBytes = iv.getIV();

        //combine the iv and the encrypted header into one array.
        byte[] combined = joinArray(ivBytes,encryptedHeader);
        //create hmac for the combined array.
        byte[] encryptedHMAC = hasher.encryptHMAC(combined,macPassword);

        hmacHex = ByteManipulation.bytesToHex(encryptedHMAC);

        //create strings for all the arrays.
        String ivString = ByteManipulation.bytesToString(ivBytes);
        String headerString = ByteManipulation.bytesToString(encryptedHeader);
        String hmacString = ByteManipulation.bytesToString(encryptedHMAC);

        return headerString + " " + ivString + " " + hmacString;
    }

    /**
     * Create an encrypted message with header and a message.
     * @param header header
     * @param message message
     * @param sharedPassword shared password
     * @return an encrypted message according to the format.
     */
    public static String CreateMessage(String header, String message, String sharedPassword, String macPassword){
        Hasher hasher = new Hasher();
        byte[] encryptedHeader = {};
        byte[] combined = {};
        String headerString = "";

        //get a random iv.
        IvParameterSpec iv = hasher.generateIV();
        ivHex = ByteManipulation.bytesToHex(iv.getIV());
        //encrypt the message.
        byte[] encryptedMessage = hasher.encryptMessage(message, sharedPassword, iv);
        //get the iv in bytes.
        byte[] ivBytes = iv.getIV();

        encryptedHeader = hasher.encryptMessage(header, sharedPassword, iv);
        headerString = ByteManipulation.bytesToString(encryptedHeader);
        //combine the 3 byte arrays (iv,header and message).
        combined = joinArray(ivBytes, encryptedHeader, encryptedMessage);

        //create the hmac for the combined array.
        byte[] encryptedHMAC = hasher.encryptHMAC(combined,macPassword);

        hmacHex = ByteManipulation.bytesToHex(encryptedHMAC);

        //create strings for all the arrays.
        String ivString = ByteManipulation.bytesToString(ivBytes);
        String messageString = ByteManipulation.bytesToString(encryptedMessage);
        String hmacString = ByteManipulation.bytesToString(encryptedHMAC);

        return headerString + " " + ivString + " " + messageString + " " + hmacString;
    }

    public static String CreateMessageForServer(String sender, String receiver, String macPassword){
        Hasher hasher = new Hasher();

        sender = sender.toLowerCase();
        receiver = receiver.toLowerCase();
        Globals.NONCEa = generateNonce();
        byte[] senderBytes  = sender.getBytes();
        byte[] receiverBytes = receiver.getBytes();
        byte[] nonceBytes = String.valueOf(Globals.NONCEa).getBytes();

        byte[] combined = joinArray(senderBytes,receiverBytes,nonceBytes);

        byte[] encryptedHMAC = hasher.encryptHMAC(combined,macPassword);

        String hmacString = ByteManipulation.bytesToString(encryptedHMAC);

        //check if toggles of invalid requestor or invalid receiver are selected.
        if(IsSelectedCustomMessage(Constants.INAVLID_REQUESTOR)){
            sender = Constants.INAVLID_REQUESTOR;
        }
        else if(IsSelectedCustomMessage(Constants.INVALID_RECEIVER)){
            receiver = Constants.INVALID_RECEIVER;
        }

        return sender + " " + receiver + " " + Globals.NONCEa + " " + hmacString;
    }

    /**
     * join byte[] arrays into a single byte[] array
     * @param arrays arrays to join
     * @return a joined byte[] array
     */
    public static byte[] joinArray(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        final byte[] result = new byte[length];

        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    public static long generateNonce()
    {
        long dateTimeString = new Date().getTime();
        return dateTimeString;
    }

    public static boolean IsSelectedCustomMessage(String toggleName){
        return Constants.customMessagesToggles.get(toggleName);
    }

}
