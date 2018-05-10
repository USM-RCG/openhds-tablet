package org.openhds.mobile.utilities;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.openhds.mobile.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class NotificationUtils {

    public static int getNotificationIcon() {
        return (SDK_INT >= LOLLIPOP) ? R.drawable.ic_silhouette : R.drawable.ic_launcher;
    }

    public static int getNotificationColor(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.Blue);
    }
}
