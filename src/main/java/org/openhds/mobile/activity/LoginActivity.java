package org.openhds.mobile.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import org.openhds.mobile.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.openhds.mobile.utilities.ConfigUtils.getAppFullName;

public class LoginActivity extends Activity {

    public static final String USERNAME_KEY = "usernameKey";
    public static final String PASSWORD_KEY = "passwordKey";

    private final String PREF_STATE_KEY = "prefStateKey";

    private FrameLayout prefsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getAppFullName(this));
        setContentView(R.layout.opening_activity);
        prefsLayout = (FrameLayout) findViewById(R.id.login_pref_container);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(PREF_STATE_KEY, prefsLayout.getVisibility());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        showPrefs(savedInstanceState.getInt(PREF_STATE_KEY) == VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        togglePrefs();
        return true;
    }

    private void togglePrefs() {
        showPrefs(prefsLayout.getVisibility() != VISIBLE);
    }

    private void showPrefs(boolean show) {
        prefsLayout.setVisibility(show ? VISIBLE : GONE);
    }
}
