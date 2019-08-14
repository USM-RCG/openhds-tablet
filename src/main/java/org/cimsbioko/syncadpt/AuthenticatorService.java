package org.cimsbioko.syncadpt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {

    private AccountAuthenticator auth;

    @Override
    public void onCreate() {
        auth = new AccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return auth.getIBinder();
    }
}
