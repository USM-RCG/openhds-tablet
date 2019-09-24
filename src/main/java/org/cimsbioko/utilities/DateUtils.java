package org.cimsbioko.utilities;

import org.cimsbioko.navconfig.UsedByJSConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    @UsedByJSConfig
    public static String formatTime(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    @UsedByJSConfig
    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

}
