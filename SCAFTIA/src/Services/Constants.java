package Services;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public final static int SERVER_LISTENER_TIMEOUT = 30000;
    public final static int SESSION_KEY_SIZE = 32;

    public final static String MSG_HELLO = "HELLO";
    public final static String MSG_SENDFILE = "SENDFILE";
    public final static String MSG_BYE = "BYE";
    public final static String MSG_OK = "OK";
    public final static String MSG_NO = "NO";
    public final static String MSG_MESSAGE = "MESSAGE";
    public final static String MSG_ACK = "ACK";
    public final static String MSG_FAILED = "FAILED";
    public final static String MSG_SERVER_RESPONSE = "SERVER_RESPONSE";


    //HELLO types.
    public final static String HELLO_FIRST = "First";
    public final static String HELLO_SECOND = "Second";

    //Hashing
    public final static String AES = "AES";
    public final static String AES_CTR = "AES/CTR/NoPadding";
    public final static String SHA_256 = "SHA-256";
    public final static String HMAC_SHA256 = "HmacSHA256";

    //Incoming types
    public final static String INCOMING_FILE = "FILE";
    public final static String INCOMING_MESSAGE = "MESSAGE";

    public static final String AUTH_TOKEN = "TOKEN";
    public static final String AUTH_CHALLENGE = "CHALLENGE";
    public static final String AUTH_RESPONSE = "RESPONSE";
    public static final String AUTH_OK = "OK";
    public static final String AUTH_FILE_TRANSFER = "FILE_TRANSFER";

    public final static String TOKEN_HEADER = "TOKEN";

    //custom responses
    public static final String INAVLID_REQUESTOR = "InvalidRequestor";
    public static final String INVALID_RECEIVER =  "InvalidReceiver";
    public static final String TOKEN_TO_WRONG_USER = "TokenToWrongUser";
    public static final String RANDOM_TOKEN = "RandomToken";
    public static final String ENCRYPT_NONCE_WITH_WRONG_KEY = "EncryptNonceWithWrongKey";
    public static final String ENCRYPT_WITH_WRONG_KEY = "EncryptWithWrongKey";
    public static final String ENCRYPT_WITH_WRONG_NUMERICAL_RESPONSE = "EncryptWithWrongNumericalResponse";
    public static final String ENCRYPT_FILE_WITH_WRONG_KEY = "EncryptFileWithWrongKey";

    public static final Map<String, Boolean> customMessagesToggles  = new HashMap<String, Boolean>() {{
        put(INAVLID_REQUESTOR, false);
        put(INVALID_RECEIVER, false);
        put(TOKEN_TO_WRONG_USER, false);
        put(RANDOM_TOKEN, false);
        put(ENCRYPT_NONCE_WITH_WRONG_KEY, false);
        put(ENCRYPT_WITH_WRONG_KEY, false);
        put(ENCRYPT_WITH_WRONG_NUMERICAL_RESPONSE, false);
        put(ENCRYPT_FILE_WITH_WRONG_KEY, false);
    }};
}
