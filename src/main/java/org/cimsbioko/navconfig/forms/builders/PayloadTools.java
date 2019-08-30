package org.cimsbioko.navconfig.forms.builders;

import org.cimsbioko.model.core.FieldWorker;
import org.cimsbioko.navconfig.forms.LaunchContext;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import static org.cimsbioko.navconfig.forms.KnownFields.*;
import static org.cimsbioko.navconfig.forms.KnownValues.FORM_NEEDS_REVIEW;
import static org.cimsbioko.navconfig.forms.KnownValues.FORM_NO_REVIEW_NEEDED;

public class PayloadTools {

    /**
     * Convenience method for updating a form payload to require review before upload.
     * @param formPayload the payload to mark as requiring review
     * @see PayloadTools#flagForReview(Map, boolean)
     */
    public static void flagForReview(Map<String, String> formPayload) {
        flagForReview(formPayload, true);
    }

    /**
     * Set the review attribute of a form payload.
     * @param formPayload the payload to update
     * @param shouldReview true for requiring review, false for no review required
     */
    public static void flagForReview(Map<String, String> formPayload, boolean shouldReview) {
        formPayload.put(NEEDS_REVIEW,
                shouldReview ? FORM_NEEDS_REVIEW : FORM_NO_REVIEW_NEEDED);
    }

    /**
     * A convenience method: determines whether the form at the specified path contains a special value denoting that
     * the form requires approval by a supervisor.
     *
     * The sentinel value denoting this was inverted when it was first implemented: needsReview = 1
     * indicates the form *does not* need approval, while any other value (or lack of a value) means
     * the form *does* need approval. However, it now defaults to approval only when the value matches.
     *
     * @param formPayload payload loaded from a form instance
     * @return true if the form contains an element named 'needsReview' with text value of '0git ll', false otherwise
     * @throws IOException
     */
    public static boolean requiresApproval(Map<String, String> formPayload) {
        return FORM_NEEDS_REVIEW.equalsIgnoreCase(formPayload.get(NEEDS_REVIEW));
    }

    /**
     * Populates the provided map with the default payload values for the given launch context.
     * Previously, this added much more values, but now only populates the "minimal" payload
     * consisting of: fieldworker uuid and extid, and the date/time the form was launched.
     *
     * @param formPayload the payload to populate
     * @param ctx the launch context from which the form was launched
     */
    public static void addMinimalFormPayload(Map<String, String> formPayload, LaunchContext ctx) {
        FieldWorker fieldWorker = ctx.getCurrentFieldWorker();
        formPayload.put(FIELD_WORKER_EXTID, fieldWorker.getExtId());
        formPayload.put(FIELD_WORKER_UUID, fieldWorker.getUuid());
        formPayload.put(COLLECTION_DATE_TIME, formatTime(Calendar.getInstance()));
    }

    public static String formatTime(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
    }

    public static String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    public static String formatBuilding(int building, boolean includePrefix) {
        return String.format("%s%03d", includePrefix? "E" : "", building);
    }

    public static String formatFloor(int floor, boolean includePrefix) {
        return String.format("%s%02d", includePrefix? "P" : "", floor);
    }
}
