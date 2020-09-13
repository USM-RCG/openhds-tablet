package org.cimsbioko.activity

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import org.cimsbioko.App
import org.cimsbioko.App.Companion.getApp
import org.cimsbioko.R
import org.cimsbioko.databinding.AuthenticatorActivityBinding
import org.cimsbioko.syncadpt.AuthUtils.register
import org.cimsbioko.syncadpt.Constants
import org.cimsbioko.syncadpt.Constants.KEY_AUTH_TOKEN_TYPE
import org.cimsbioko.syncadpt.Constants.KEY_NEW_ACCOUNT
import org.cimsbioko.utilities.MessageUtils.showLongToast
import org.cimsbioko.utilities.UrlUtils.buildServerUrl
import org.cimsbioko.utilities.UrlUtils.setServerUrl
import org.cimsbioko.utilities.UrlUtils.urlDecode
import java.util.regex.Pattern

class DeviceAuthenticatorActivity : AppCompatActivity(), LoginTaskListener, View.OnKeyListener {

    private lateinit var accountManager: AccountManager
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    private var authResponse: AccountAuthenticatorResponse? = null
    private var tokenType: String? = null
    private var task: LoginTask? = null
    private var authResult: Bundle? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = AuthenticatorActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
            title.setText(R.string.device_login)
            loginButton.setOnClickListener { submit() }
            scanButton.setOnClickListener { scan() }
        }
        usernameEditText = binding.usernameEditText.apply { setOnKeyListener(this@DeviceAuthenticatorActivity) }
        passwordEditText = binding.passwordEditText.apply { setOnKeyListener(this@DeviceAuthenticatorActivity) }
        authResponse = intent
                .getParcelableExtra<AccountAuthenticatorResponse?>(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
                ?.apply { onRequestContinued() }
        tokenType = intent.getStringExtra(KEY_AUTH_TOKEN_TYPE) ?: Constants.AUTHTOKEN_TYPE_DEVICE
        accountManager = AccountManager.get(baseContext)
        intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)?.also { binding.usernameEditText.setText(it) }
    }

    override fun onPause() {
        super.onPause()
        task?.cancel(true)
    }

    private fun scan() {
        IntentIntegrator(this)
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(BarcodeFormat.QR_CODE.toString())
                .setBeepEnabled(false)
                .initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.let { IntentIntegrator.parseActivityResult(requestCode, resultCode, it) }?.also { result ->
            SCAN_PATTERN.matcher(result.contents ?: "").also { m ->
                if (m.matches()) {
                    usernameEditText.setText(urlDecode(m.group(2)))
                    passwordEditText.setText(urlDecode(m.group(3)))
                    urlDecode(m.group(1)).takeIf { buildServerUrl(this, "") != it }?.also { url ->
                        AlertDialog.Builder(this)
                                .setTitle(R.string.update_server_title)
                                .setMessage(getString(R.string.update_server_msg, url))
                                .setPositiveButton(R.string.yes_btn) { _: DialogInterface?, _: Int ->
                                    this@DeviceAuthenticatorActivity.also { ctx ->
                                        setServerUrl(ctx, url)
                                        showLongToast(ctx, R.string.server_updated_msg)
                                    }
                                }
                                .setNegativeButton(R.string.no_btn, null)
                                .show()
                    }
                }
            }
        } ?: super.onActivityResult(requestCode, resultCode, data)
    }

    private fun submit() {
        task = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
                ?.let { accountType -> LoginTask(usernameEditText.text.toString(), passwordEditText.text.toString(), accountType, this) }
                ?.also { it.execute() }
    }

    override fun error(message: String?) {
        message?.also { Toast.makeText(baseContext, it, Toast.LENGTH_SHORT).show() }
    }

    override fun success(result: Intent) {
        val accountName: String? = result.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val accountPassword: String? = result.getStringExtra(AccountManager.KEY_PASSWORD)
        val accountType: String? = result.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        if (accountName != null && accountPassword != null && accountType != null) {
            Account(accountName, accountType).also { acc ->
                if (intent.getBooleanExtra(KEY_NEW_ACCOUNT, false)) {
                    if (accountManager.addAccountExplicitly(acc, accountPassword, null)) {
                        ContentResolver.setIsSyncable(acc, App.AUTHORITY, 1)
                        ContentResolver.setSyncAutomatically(acc, App.AUTHORITY, true)
                        result.getStringExtra(AccountManager.KEY_AUTHTOKEN)?.also { token ->
                            accountManager.setAuthToken(acc, tokenType, token)
                        }
                    } else {
                        Log.e(TAG, "failed to add account")
                    }
                } else {
                    accountManager.setPassword(acc, accountPassword)
                }
            }
            setAccountAuthenticatorResult(result.extras)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            submit()
            return true
        }
        return false
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    private fun setAccountAuthenticatorResult(result: Bundle?) {
        authResult = result
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    override fun finish() {
        authResponse?.also { response ->
            authResult?.also { result -> response.onResult(result) }
                    ?: response.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            authResponse = null
        }
        super.finish()
    }

    companion object {
        private val TAG = DeviceAuthenticatorActivity::class.java.simpleName
        private val SCAN_PATTERN = Pattern.compile("^cimsmcfg://(.*)[?]d=(.*)&s=(.*)$")
    }
}

private interface LoginTaskListener {
    fun error(message: String?)
    fun success(result: Intent)
}

private class LoginTask(
        private val username: String,
        private val password: String,
        private val accountType: String,
        private var listener: LoginTaskListener?
) : AsyncTask<Void?, Void?, Intent>() {

    override fun doInBackground(vararg params: Void?): Intent? = Bundle().apply {
        try {
            putString(AccountManager.KEY_ACCOUNT_NAME, username)
            putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            register(getApp().applicationContext, username, password).also { result ->
                putString(AccountManager.KEY_AUTHTOKEN, result.getString("access_token"))
                putString(AccountManager.KEY_PASSWORD, result.getString("secret"))
            }
        } catch (e: Exception) {
            putString(AccountManager.KEY_ERROR_MESSAGE, e.message)
        }
    }.let { Intent().putExtras(it) }

    override fun onPostExecute(intent: Intent) {
        listener?.also { listener ->
            intent.getStringExtra(AccountManager.KEY_ERROR_MESSAGE)?.also { listener.error(it) }
                    ?: listener.success(intent)
        }
    }

    override fun onCancelled(intent: Intent) {
        listener = null
        super.onCancelled(intent)
    }
}