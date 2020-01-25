package Services;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    private static final String LOG_FILEPATH = System.getProperty("user.dir");
    private static final String LOG_FILE_NAME = "SCAFTIA_Server_Log.txt";
    private static final String COMPLETE_LOG_PATH = LOG_FILEPATH + "\\" + LOG_FILE_NAME;


    /**
     * Write to the Log file.
     * log must have the following information about every message received:
     * @param logHeader Whether the message's HMAC was valid (valid/invalid)
     * @param senderIP The sender's IP and port
     * @param senderName The sender's declared name
     * @param recipientName The intended recipient's name
     * @param nonce The nonce (NA) sent
     * @param error A textual description of any error which occurred in processing (ex. unknown sender, unknown recipient, etc.)
     * @param isEncrypted Whether the request message was encrypted (encrypted/not encrypted)
     * @param isResponseSentBack Whether a response was sent back
     * @param isResponseFake Whether the response sent. back was intentionally incorrect.
     */
    public static void write(String logHeader, String senderIP, String senderName, String recipientName, String nonce, String error ,boolean isEncrypted, boolean isResponseSentBack, boolean isResponseFake){
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        String currentTime = df.format(now);

        String encryption = isEncrypted ? "Yes" : "No";
        String sentResponse = isResponseSentBack ? "Sent" : "Net Sent";
        String fakeResponse = isResponseFake ? "Fake" : "Not Fake";
        error = error == "" ? "No Error" : error;

        FileWriter aWriter = null;
        try {
            aWriter = new FileWriter(COMPLETE_LOG_PATH, true);
            aWriter.write("["+currentTime+"] " + logHeader + ":" + System.lineSeparator() +
                            "\tSender: " +          senderName + " (" + senderIP +")" + System.lineSeparator() +
                            "\tRecipient: " +       recipientName + System.lineSeparator() +
                            "\tNonce: " +           nonce + System.lineSeparator() +
                            "\tError: " +           error + System.lineSeparator() +
                            "\tEncryption: " +      encryption + System.lineSeparator() +
                            "\tResponse Sent: " +   sentResponse + System.lineSeparator() +
                            "\tFake Response: " +   fakeResponse + System.lineSeparator());
            aWriter.flush();
            aWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
