package Services.Utilities;

import java.util.regex.Pattern;

public class IpPortValidator {

    //IP and Port pattren
    private static final String IP_PORT_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5]):" +
                    "([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

    private static final Pattern PATTERN;

    static {
        PATTERN = Pattern.compile(IP_PORT_PATTERN);
    }

    /**
     * Validate that the s in in a valid from of IP:Port (i.e. 123.123.123.123:444)
     * @param s IP:Port format String
     * @return
     */
    public static boolean validate(final String s) {
        return PATTERN.matcher(s).matches();
    }

}
