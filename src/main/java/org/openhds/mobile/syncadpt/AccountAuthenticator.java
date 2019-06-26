package org.openhds.mobile.syncadpt;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import static android.accounts.AccountManager.*;
import static org.openhds.mobile.syncadpt.AuthUtils.token;
import static org.openhds.mobile.syncadpt.Constants.AUTHTOKEN_TYPE_DEVICE;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

    private static final String TAG = AccountAuthenticator.class.getSimpleName();

    private Context ctx;

    public AccountAuthenticator(Context ctx) {
        super(ctx);
        this.ctx = ctx;
    }


    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {

        Intent intent = new Intent(ctx, AuthenticatorActivity.class);
        intent.putExtra(KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AuthenticatorActivity.KEY_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.KEY_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {

        if (!authTokenType.equals(AUTHTOKEN_TYPE_DEVICE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "unknown token type " + authTokenType);
            return result;
        }

        AccountManager am = AccountManager.get(ctx);
        String authToken = am.peekAuthToken(account, authTokenType);

        // If no token, get one by logging in with stored user credentials
        if (TextUtils.isEmpty(authToken)) {
            String password = am.getPassword(account);
            if (password != null) {
                try {
                    authToken = token(ctx, account.name, password).getString("access_token");
                } catch (Exception e) {
                    Log.w(TAG, "failed to obtain new token", e);
                }
            }
        }

        // Return the token, if one was obtained
        if (!TextUtils.isEmpty(authToken)) {
            final Bundle result = new Bundle();
            result.putString(KEY_ACCOUNT_NAME, account.name);
            result.putString(KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        } else { // failed to get a token using stored creds, reprompt
            Intent intent = new Intent(ctx, AuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(KEY_ACCOUNT_TYPE, account.type);
            intent.putExtra(KEY_ACCOUNT_NAME, account.name);

            Bundle bundle = new Bundle();
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }
}
