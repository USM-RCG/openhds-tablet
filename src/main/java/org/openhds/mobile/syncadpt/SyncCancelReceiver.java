package org.openhds.mobile.syncadpt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.openhds.mobile.utilities.SyncUtils;

public class SyncCancelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SyncUtils.cancelUpdate(context);
    }
}
