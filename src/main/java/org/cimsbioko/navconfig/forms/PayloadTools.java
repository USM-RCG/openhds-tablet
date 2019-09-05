package org.cimsbioko.navconfig.forms;

import org.cimsbioko.model.core.FieldWorker;
import org.cimsbioko.navconfig.UsedByJSConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import static org.cimsbioko.navconfig.forms.KnownFields.*;


public class PayloadTools {

    /**
     * Populates the provided map with the default payload values for the given launch context.
     * Previously, this added much more values, but now only populates the "minimal" payload
     * consisting of: fieldworker uuid and extid, and the date/time the form was launched.
     *
     * @param formPayload the payload to populate
     * @param ctx the launch context from which the form was launched
     */
    @UsedByJSConfig
    public static void addMinimalFormPayload(Map<String, String> formPayload, LaunchContext ctx) {
        FieldWorker fieldWorker = ctx.getCurrentFieldWorker();
        formPayload.put(FIELD_WORKER_EXTID, fieldWorker.getExtId());
        formPayload.put(FIELD_WORKER_UUID, fieldWorker.getUuid());
        formPayload.put(COLLECTION_DATE_TIME, formatTime(Calendar.getInstance()));
    }

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
