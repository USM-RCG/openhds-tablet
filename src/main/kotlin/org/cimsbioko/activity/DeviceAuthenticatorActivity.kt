package org.cimsbioko.activity

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cimsbioko.App
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

data class AccountInfo(
        val username: String,
        val password: String,
        val type: String,
        val token: String
)

sealed class LoginResult {
    data class Success(val info: AccountInfo) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class DeviceAuthenticatorViewModel : ViewModel() {

    // Ensure login is processed only once, even if device is rotated
    private val resultChannel = Channel<LoginResult>()
    val loginResultFlow = resultChannel.receiveAsFlow()

    fun login(ctx: Context, username: String, password: String, accountType: String) {
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) { register(ctx.applicationContext, username, password) }
            }.let { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let { json ->
                        val token: String? = json.getString("access_token")
                        val newPassword: String? = json.getString("secret")
                        if (token != null && newPassword != null) {
                            LoginResult.Success(
                                    AccountInfo(
                                            username = username,
                                            password = newPassword,
                                            type = accountType,
                                            token = token
                                    )
                            )
                        } else LoginResult.Error("JSON result lacks one or more required fields: access_token, secret")
                    }
                } else result.exceptionOrNull()?.message?.let { LoginResult.Error(it) }
            }?.also { resultChannel.send(it) }
        }
    }
}

class DeviceAuthenticatorActivity : AppCompatActivity(), View.OnKeyListener {

    private val viewModel: DeviceAuthenticatorViewModel by viewModels()

    private lateinit var accountManager: AccountManager
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    private var authResponse: AccountAuthenticatorResponse? = null
    private var tokenType: String? = null
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.loginResultFlow
                        .onEach { handleLoginResult(it) }
                        .collect()
            }
        }
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
                    usernameEditText.setText(urlDecode(m.group(2)!!))
                    passwordEditText.setText(urlDecode(m.group(3)!!))
                    urlDecode(m.group(1)!!).takeIf { buildServerUrl(this, "") != it }?.also { url ->
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
        val username = usernameEditText.text.toString()
        val type = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        val password = passwordEditText.text.toString()
        if (type != null) viewModel.login(this, username, password, type)
    }

    private fun handleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Success -> onLoginSuccess(result.info)
            is LoginResult.Error -> onLoginError(result.message)
        }
    }

    private fun onLoginError(message: String?) {
        message?.also { Toast.makeText(baseContext, it, Toast.LENGTH_SHORT).show() }
    }

    private fun onLoginSuccess(info: AccountInfo) {
        Account(info.username, info.type).also { acc ->
            if (intent.getBooleanExtra(KEY_NEW_ACCOUNT, false)) {
                if (accountManager.addAccountExplicitly(acc, info.password, null)) {
                    ContentResolver.setIsSyncable(acc, App.AUTHORITY, 1)
                    ContentResolver.setSyncAutomatically(acc, App.AUTHORITY, true)
                    accountManager.setAuthToken(acc, tokenType, info.token)
                } else Log.e(TAG, "failed to add account")
            } else accountManager.setPassword(acc, info.password)
        }
        val result: Intent = info.toAuthenticatorResult()
        setAccountAuthenticatorResult(result.extras)
        setResult(Activity.RESULT_OK, result)
        finish()
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
            authResult?.also { result -> response.onResult(result) } ?: response.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            authResponse = null
        }
        super.finish()
    }

    companion object {
        private val TAG = DeviceAuthenticatorActivity::class.java.simpleName
        private val SCAN_PATTERN = Pattern.compile("^cimsmcfg://(.*)[?]d=(.*)&s=(.*)$")
    }
}

private fun AccountInfo.toAuthenticatorResult(): Intent {
    val info = this
    return Intent().apply {
        putExtras(
                Bundle().apply {
                    putString(AccountManager.KEY_ACCOUNT_NAME, info.username)
                    putString(AccountManager.KEY_ACCOUNT_TYPE, info.type)
                    putString(AccountManager.KEY_AUTHTOKEN, info.token)
                    putString(AccountManager.KEY_PASSWORD, info.password)
                }
        )
    }
}