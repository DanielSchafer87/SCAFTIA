package Services;

public class Constants {

    //Hashing
    public final static String AES = "AES";
    public final static String AES_CTR = "AES/CTR/NoPadding";
    public final static String SHA_256 = "SHA-256";
    public final static String HMAC_SHA256 = "HmacSHA256";

    public final static int SESSION_KEY_SIZE = 32;

    public final static String TOKEN_HEADER = "TOKEN";

    //errors
    public final static String ERROR_UNKNOWN_SENDER = "Unknown Sender";
    public final static String ERROR_UNKNOWN_RECIPIENT = "Unknown Recipient";
    public final static String ERROR_BAD_HMAC = "Bad HMAC";

    //Custom responses
    public final static String INVALID_NONCE = "InvalidNonce";
    public final static String INVALID_TARGET_NAME = "InvalidTargetName";
    public final static String ENCRYPT_WRONG_REQUESTOR = "EncryptWrongRequestor";
    public final static String ENCRYPT_WRONG_RECIPIENT = "EncryptWrongRecipient";
}
