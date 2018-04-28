package org.openhds.mobile.utilities;

import android.graphics.Color;
import android.os.Build;

import org.openhds.mobile.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class NotificationUtils {

    private static int NOTIFICATION_COLOR = Color.argb(255, 0, 128, 0);

    public static int getNotificationIcon() {
        return (SDK_INT >= LOLLIPOP) ? R.drawable.ic_silhouette : R.drawable.ic_launcher;
    }

    public static int getNotificationColor() {
        return NOTIFICATION_COLOR;
    }
}
