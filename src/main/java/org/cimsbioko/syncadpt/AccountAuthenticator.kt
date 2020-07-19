package org.cimsbioko.syncadpt

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import org.cimsbioko.activity.DeviceAuthenticatorActivity
import org.cimsbioko.syncadpt.AuthUtils.token
import org.cimsbioko.syncadpt.Constants.AUTHTOKEN_TYPE_DEVICE

class AccountAuthenticator(private val ctx: Context) : AbstractAccountAuthenticator(ctx) {

    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String,
                            requiredFeatures: Array<String>, options: Bundle): Bundle =
            Intent(ctx, DeviceAuthenticatorActivity::class.java)
                    .apply {
                        putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
                        putExtra(DeviceAuthenticatorActivity.KEY_AUTH_TOKEN_TYPE, authTokenType)
                        putExtra(DeviceAuthenticatorActivity.KEY_NEW_ACCOUNT, true)
                        putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                    }.let {
                        Bundle().apply { putParcelable(AccountManager.KEY_INTENT, it) }
                    }

    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {

        if (authTokenType != AUTHTOKEN_TYPE_DEVICE) {
            return Bundle().apply { putString(AccountManager.KEY_ERROR_MESSAGE, "unknown token type $authTokenType") }
        }

        val am = AccountManager.get(ctx)
        var authToken = am.peekAuthToken(account, authTokenType)

        // If no token, get one by logging in with stored user credentials
        if (TextUtils.isEmpty(authToken)) {
            am.getPassword(account)?.let {
                try {
                    authToken = token(ctx, account.name, it).getString("access_token")
                } catch (e: Exception) {
                    Log.w(TAG, "failed to obtain new token", e)
                }
            }
        }

        // Return the token, if one was obtained
        return if (!TextUtils.isEmpty(authToken)) {
            Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, authToken)
            }
        } else { // failed to get a token using stored creds, reprompt
            Intent(ctx, DeviceAuthenticatorActivity::class.java)
                    .apply {
                        putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                        putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                        putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name)
                    }.let {
                        Bundle().apply { putParcelable(AccountManager.KEY_INTENT, it) }
                    }
        }
    }

    override fun getAuthTokenLabel(authTokenType: String) = null
    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle) = null
    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle) = null
    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>) =
            Bundle().apply { putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false) }

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String) = throw UnsupportedOperationException()

    companion object {
        private val TAG = AccountAuthenticator::class.java.simpleName
    }
}