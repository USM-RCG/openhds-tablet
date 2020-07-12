package org.cimsbioko.utilities

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import org.cimsbioko.App
import org.cimsbioko.syncadpt.Constants
import java.io.IOException

object AccountUtils {

    private val accountManager: AccountManager
        get() = AccountManager.get(App.getApp())

    private val accounts: Array<Account>
        get() = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE)

    @JvmStatic
    val firstAccount: Account?
        get() {
            return accounts.firstOrNull()
        }

    @JvmStatic
    @get:Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    val token: String
        get() = accountManager.blockingGetAuthToken(firstAccount, Constants.AUTHTOKEN_TYPE_DEVICE, true)

}