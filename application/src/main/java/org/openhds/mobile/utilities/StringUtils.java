package org.openhds.mobile.utilities;


public class StringUtils {

    public static String join(String separator, String... strings) {
        if (strings == null)
            return null;
        if (strings.length == 0)
            return "";
        StringBuilder b = new StringBuilder(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            b.append(separator);
            b.append(strings[i]);
        }
        return b.toString();
    }
}
