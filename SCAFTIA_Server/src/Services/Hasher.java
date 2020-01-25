package Services;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;


public class Hasher {

    private static Cipher cipher;
    private static MessageDigest messageDigest;
    private static Mac hmac;

    /**
     * Constructor
     */
    public Hasher(){
        try {
            //create a message digest with SHA-256 algorithm
            messageDigest = MessageDigest.getInstance(Constants.SHA_256);
            //create a cipher instance with AES/CTR/NoPadding algorithm
            cipher = Cipher.getInstance(Constants.AES_CTR);
            //create mac instance with HmacSHA256 algorithm
            hmac = Mac.getInstance(Constants.HMAC_SHA256);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate a random 128-bit initialization vector (IV)
     * @return ivParams
     */
    public IvParameterSpec generateIV(){
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[cipher.getBlockSize()];
        randomSecureRandom.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        return ivParams;
    }

    /**
     * Generate a random 256-bit session key
     * @return session key
     */
    public byte[] generateSessionKey(){
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] sessionKey = new byte[Constants.SESSION_KEY_SIZE];
        randomSecureRandom.nextBytes(sessionKey);
        return sessionKey;
    }

    /**
     * IvParameterSpec from IV in String.
     * @param iv initialization vector in string
     * @return
     */
    public IvParameterSpec ivFromString(String iv){
        byte[] ivBytes = ByteManipulation.stringToBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(ivBytes);
        return ivParams;
    }

    /**
     * (1) Converting the password to bytes using the UTF8 encoding
     * (2) Hashing the resulting bytes with SHA2-256 hash algorithm.
     * @param password a password that is provided by the user.
     * @return 256 output bits in SHA2-256 encryption.
     */
    public String encryptPassword(String password){
        byte[] passwordInBytes = password.getBytes(StandardCharsets.UTF_8);
        String encryptedPassword = ByteManipulation.bytesToString(messageDigest.digest(passwordInBytes));
        return encryptedPassword;
    }

    /**
     * (1) Converting the password to bytes using the UTF8 encoding
     * (2) Hashing the resulting bytes with SHA2-256 hash algorithm.
     * @param password a password that is provided by the user.
     * @param algorithm the algorithm that is used the encrypt the password
     * @return 256 output bits as the key for the AES cipher
     */
    private SecretKey encryptPassword(String password, String algorithm){
        byte[] passwordInBytes = password.getBytes(StandardCharsets.UTF_8);
        SecretKey digestedPassword = new SecretKeySpec(messageDigest.digest(passwordInBytes),algorithm);

        return digestedPassword;
    }

    /**
     * Encrypt a message with the AES/CTR algorithm using the
     * IV and a password that is provided by the user.
     * @param message a message to encrypt.
     * @param privatePassword private password of the user.
     * @param iv a password that is provided by the user.
     * @return byte array - encrypted message
     */
    public byte[] encryptMessage(String message, String privatePassword, IvParameterSpec iv){
        //get the encrypted shared password.
        SecretKey key = new SecretKeySpec(ByteManipulation.stringToBytes(privatePassword),Constants.AES);

        try {
            //initialize the Cipher in Encrypt mode with the
            //encrypted password and the IV.
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        //turn the message from String to Byte[]
        byte[] messageInBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessage = {};
        try {
            //encrypt the message.
            encryptedMessage = cipher.doFinal(messageInBytes);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        //return message;
        return encryptedMessage;
    }


    /**
     * Decrypt a message with the AES/CTR algorithm using the
     * IV and a password that is provided by the user.
     * @param message an encrypted message to decrypt
     * @param password a password that is provided by the user.
     * @param iv the IV that was used to encrypt the message.
     * @return decrypted message.
     */
    public String decryptMessage(String message, String password, String iv){
        //get the encrypted shared password.
        SecretKey key = encryptPassword(password, Constants.AES);
        //get the IV that the message was encrypted with
        //from String to IvParameterSpec
        IvParameterSpec ivParams = ivFromString(iv);

        try {
            //initialize the Cipher in Decrypt mode with the
            //encrypted password and the IV that was used to
            //encrypt the message.
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        //turn the message from String to Byte[]
        byte[] messageInBytes = ByteManipulation.stringToBytes(message);
        byte[] decryptedMessage = {};
        try {
            //decrypt the message.
            decryptedMessage = cipher.doFinal(messageInBytes);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        try {
            //turn the encrypted message from Byte[] to String.
            message = new String(decryptedMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Create HMAC for a byte array using the Hmac-SHA256 algorithm.
     * @param data data to create the HMAC on
     * @param macPassword hmac password
     * @return byte array - hmac
     */
    public byte[] encryptHMAC(byte[] data, String macPassword){
        byte[] encryptedData;
        //get the encrypted hmac password.
        SecretKey key = encryptPassword(macPassword, Constants.HMAC_SHA256);

        try {
            //initialize the key.
            hmac.init(key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //create the hmac.
        encryptedData = hmac.doFinal(data);

        return encryptedData;
    }

}
