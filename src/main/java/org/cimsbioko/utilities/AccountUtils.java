package org.cimsbioko.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import org.cimsbioko.App;
import org.cimsbioko.syncadpt.Constants;

import java.io.IOException;

public class AccountUtils {

    public static AccountManager getAccountManager() {
        return AccountManager.get(App.getApp());
    }

    private static Account[] getAccounts() {
        return getAccountManager().getAccountsByType(Constants.ACCOUNT_TYPE);
    }

    public static Account getFirstAccount() {
        Account[] accts = getAccounts();
        return accts.length > 0 ? accts[0] : null;
    }

    public static String getToken() throws AuthenticatorException, OperationCanceledException, IOException {
        return getAccountManager().blockingGetAuthToken(getFirstAccount(), Constants.AUTHTOKEN_TYPE_DEVICE, true);
    }
}
