package org.cimsbioko.utilities;

public class EncodingUtils {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * Converts the given byte array to its hexadecimal equivalent.
     *
     * @param bytes the array to convert
     * @return the array contents encoded as a base-16 string
     */
    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
