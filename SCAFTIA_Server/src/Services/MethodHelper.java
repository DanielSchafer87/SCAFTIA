package Services;

import java.util.Date;

public class MethodHelper {

    public static long generateNonce()
    {
        long dateTimeString = new Date().getTime();
        return dateTimeString;
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

}
