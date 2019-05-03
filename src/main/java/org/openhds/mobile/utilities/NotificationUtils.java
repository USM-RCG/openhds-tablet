package org.openhds.mobile.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import org.openhds.mobile.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;

public class NotificationUtils {

    public static final String SYNC_CHANNEL_ID = "Sync";

    public static int getNotificationIcon() {
        return (SDK_INT >= LOLLIPOP) ? R.drawable.ic_silhouette : R.drawable.ic_launcher;
    }

    public static int getNotificationColor(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.Blue);
    }

    public static NotificationManager getNotificationManager(Context ctx) {
        return (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void createChannels(Context ctx) {
        if (SDK_INT >= O) {
            NotificationManager manager = getNotificationManager(ctx);
            NotificationChannel syncChannel =  new NotificationChannel(SYNC_CHANNEL_ID,
                    ctx.getString(R.string.sync_notifications_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(syncChannel);
        }
    }
}
