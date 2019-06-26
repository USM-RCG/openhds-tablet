package org.openhds.mobile.syncadpt;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import org.openhds.mobile.R;

import static android.accounts.AccountManager.*;
import static android.widget.Toast.LENGTH_SHORT;
import static org.openhds.mobile.syncadpt.AuthUtils.register;

public class AuthenticatorActivity extends AccountAuthenticatorActivity implements LoginTaskListener {

    public static final String KEY_AUTH_TOKEN_TYPE = "tokenType";
    public static final String KEY_NEW_ACCOUNT = "newAccount";

    private AccountManager accountManager;
    private String tokenType;
    private LoginTask task;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.generic_login_fragment);
        accountManager = get(getBaseContext());

        TextView title = findViewById(R.id.titleTextView);
        title.setText("Login");

        String accountName = getIntent().getStringExtra(KEY_ACCOUNT_NAME);

        tokenType = getIntent().getStringExtra(KEY_AUTH_TOKEN_TYPE);
        if (tokenType == null)
            tokenType = Constants.AUTHTOKEN_TYPE_DEVICE;

        if (accountName != null) {
            ((TextView) findViewById(R.id.usernameEditText)).setText(accountName);
        }

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != null) {
            task.cancel(true);
        }
    }

    private void submit() {
        String username = ((TextView) findViewById(R.id.usernameEditText)).getText().toString();
        String password = ((TextView) findViewById(R.id.passwordEditText)).getText().toString();
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
            accountManager.addAccountExplicitly(account, accountPassword, null);
            accountManager.setAuthToken(account, tokenType, token);
        } else {
            accountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
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

