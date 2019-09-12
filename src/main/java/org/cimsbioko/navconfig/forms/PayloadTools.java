package org.cimsbioko.navconfig.forms;

import org.cimsbioko.navconfig.UsedByJSConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PayloadTools {

    @UsedByJSConfig
    public static String formatTime(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
    }

    @UsedByJSConfig
    public static String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    @UsedByJSConfig
    public static String formatBuilding(int building, boolean includePrefix) {
        return String.format("%s%03d", includePrefix? "E" : "", building);
    }

    @UsedByJSConfig
    public static String formatFloor(int floor, boolean includePrefix) {
        return String.format("%s%02d", includePrefix? "P" : "", floor);
    }
}
