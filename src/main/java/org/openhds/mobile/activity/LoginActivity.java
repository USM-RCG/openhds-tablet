package org.openhds.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.openhds.mobile.R;

import static org.openhds.mobile.utilities.ConfigUtils.getAppFullName;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getAppFullName(this));
        setContentView(R.layout.login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configure_server:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            case R.id.show_supervisor:
                startActivity(new Intent(this, SupervisorActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
