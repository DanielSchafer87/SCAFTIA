package Services;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    private static final String LOG_FILEPATH = System.getProperty("user.dir");
    private static final String LOG_FILE_NAME = "SCAFT_Log.txt";
    private static final String COMPLETE_LOG_PATH = LOG_FILEPATH + "\\" + LOG_FILE_NAME;

    /**
     * Write to the Log file.
     * @param s String to write to the Log file.
     */
    public static void write(String s){
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        String currentTime = df.format(now);

        FileWriter aWriter = null;
        try {
            aWriter = new FileWriter(COMPLETE_LOG_PATH, true);
            aWriter.write("["+currentTime+"]" + " " + s);
            aWriter.write(System.lineSeparator());
            aWriter.flush();
            aWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
