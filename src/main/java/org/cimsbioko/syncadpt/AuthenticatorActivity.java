package org.cimsbioko.syncadpt;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.cimsbioko.R;
import org.cimsbioko.utilities.UrlUtils;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.accounts.AccountManager.*;
import static android.content.ContentResolver.setIsSyncable;
import static android.content.ContentResolver.setSyncAutomatically;
import static android.widget.Toast.LENGTH_SHORT;
import static org.cimsbioko.App.AUTHORITY;
import static org.cimsbioko.syncadpt.AuthUtils.register;
import static org.cimsbioko.utilities.MessageUtils.showLongToast;
import static org.cimsbioko.utilities.UrlUtils.setServerUrl;
import static org.cimsbioko.utilities.UrlUtils.urlDecode;

public class AuthenticatorActivity extends AppCompatActivity implements LoginTaskListener, View.OnKeyListener {

    public static final String KEY_AUTH_TOKEN_TYPE = "tokenType";
    public static final String KEY_NEW_ACCOUNT = "newAccount";
    private static final String TAG = AuthenticatorActivity.class.getSimpleName();
    private static final Pattern SCAN_PATTERN = Pattern.compile("^cimsmcfg://(.*)[?]d=(.*)&s=(.*)$");

    private AccountManager accountManager;
    private String tokenType;
    private LoginTask task;

    private EditText usernameEditText;
    private EditText passwordEditText;

    private AccountAuthenticatorResponse authResponse;
    private Bundle authResult;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.authenticator_activity);
        accountManager = get(getBaseContext());

        authResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (authResponse != null) {
            authResponse.onRequestContinued();
        }

        TextView title = findViewById(R.id.title);
        title.setText(R.string.device_login);

        tokenType = getIntent().getStringExtra(KEY_AUTH_TOKEN_TYPE);
        if (tokenType == null)
            tokenType = Constants.AUTHTOKEN_TYPE_DEVICE;

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameEditText.setOnKeyListener(this);
        passwordEditText.setOnKeyListener(this);

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        findViewById(R.id.scanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });

        String accountName = getIntent().getStringExtra(KEY_ACCOUNT_NAME);

        if (accountName != null) {
            ((TextView) findViewById(R.id.usernameEditText)).setText(accountName);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != null) {
            task.cancel(true);
        }
    }

    private void scan() {
        new IntentIntegrator(this)
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(BarcodeFormat.QR_CODE.toString())
                .setBeepEnabled(false)
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String resultContents = result.getContents();
            Matcher m = SCAN_PATTERN.matcher(resultContents == null? "" : resultContents);
            if (m.matches()) {
                final String url = urlDecode(m.group(1)), name = urlDecode(m.group(2)), secret = urlDecode(m.group(3));
                usernameEditText.setText(name);
                passwordEditText.setText(secret);
                if (!UrlUtils.buildServerUrl(this, "").equals(url)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Update Server")
                            .setMessage("The scanned configuration uses a different server. Update to " + url + "?")
                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Context ctx = AuthenticatorActivity.this;
                                    setServerUrl(ctx, url);
                                    showLongToast(ctx, "Server updated");
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void submit() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String accountType = getIntent().getStringExtra(KEY_ACCOUNT_TYPE);
        task = new LoginTask(getApplicationContext(), username, password, accountType, this);
        task.execute();
    }

    public void error(String message) {
        Toast.makeText(getBaseContext(), message, LENGTH_SHORT).show();
    }

    public void success(Intent intent) {

        String accountName = intent.getStringExtra(KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(KEY_PASSWORD);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(KEY_NEW_ACCOUNT, false)) {
            String token = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            if (accountManager.addAccountExplicitly(account, accountPassword, null)) {
                setIsSyncable(account, AUTHORITY, 1);
                setSyncAutomatically(account, AUTHORITY, true);
                accountManager.setAuthToken(account, tokenType, token);
            } else {
                Log.e(TAG, "failed to add account");
            }
        } else {
            accountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            submit();
            return true;
        }
        return false;
    }

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        authResult = result;
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (authResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (authResult != null) {
                authResponse.onResult(authResult);
            } else {
                authResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            authResponse = null;
        }
        super.finish();
    }
}


interface LoginTaskListener {

    void error(String message);

    void success(Intent result);
}


class LoginTask extends AsyncTask<Void, Void, Intent> {

    private final Context ctx;
    private final String username;
    private final String password;
    private final String accountType;
    private LoginTaskListener listener;

    public LoginTask(Context ctx, String username, String password, String accountType, LoginTaskListener listener) {
        this.ctx = ctx;
        this.username = username;
        this.password = password;
        this.accountType = accountType;
        this.listener = listener;
    }

    @Override
    protected Intent doInBackground(Void... params) {

        Bundle data = new Bundle();

        try {
            JSONObject result = register(ctx, username, password);
            data.putString(KEY_ACCOUNT_NAME, username);
            data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
            data.putString(AccountManager.KEY_AUTHTOKEN, result.getString("access_token"));
            data.putString(KEY_PASSWORD, result.getString("secret"));
        } catch (Exception e) {
            data.putString(KEY_ERROR_MESSAGE, e.getMessage());
        }

        Intent res = new Intent();
        res.putExtras(data);
        return res;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        if (listener != null) {
            if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                listener.error(intent.getStringExtra(KEY_ERROR_MESSAGE));
            } else {
                listener.success(intent);
            }
        }
    }

    @Override
    protected void onCancelled(Intent intent) {
        listener = null;
        super.onCancelled(intent);
    }
}

