package org.cimsbioko.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import org.cimsbioko.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE;
import static org.cimsbioko.utilities.ConfigUtils.getAppFullName;

public class LoginActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Toolbar toolbar = findViewById(R.id.login_toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.login_drawer_layout);
        NavigationView navView = findViewById(R.id.login_navigation_view);
        navView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        setTitle(getAppFullName(this));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Account[] accounts = AccountManager.get(this).getAccountsByType(ACCOUNT_TYPE);
        if (navView.getHeaderCount() > 0) {
            boolean attached = accounts.length > 0;
            View headerView = navView.getHeaderView(0);
            TextView textView = headerView.findViewById(R.id.nav_header_text);
            textView.setText(attached ? accounts[0].name : getString(R.string.app_name));
            textView.setVisibility(attached ? VISIBLE : GONE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.configure_server:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            case R.id.show_supervisor:
                startActivity(new Intent(this, SupervisorActivity.class));
                return true;
            default:
                return false;
        }
    }
}
