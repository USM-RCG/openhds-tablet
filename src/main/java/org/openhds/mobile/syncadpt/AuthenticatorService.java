package org.openhds.mobile.syncadpt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {

    private AuthenticatorStub auth;

    @Override
    public void onCreate() {
        auth = new AuthenticatorStub(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return auth.getIBinder();
    }
}
