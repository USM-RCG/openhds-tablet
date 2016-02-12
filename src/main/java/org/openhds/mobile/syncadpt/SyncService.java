package org.openhds.mobile.syncadpt;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {

    private static final Object CREATE_LOCK = new Object();
    private static SyncAdapter adapter;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (CREATE_LOCK) {
            if (adapter == null) {
                adapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return adapter.getSyncAdapterBinder();
    }

}
