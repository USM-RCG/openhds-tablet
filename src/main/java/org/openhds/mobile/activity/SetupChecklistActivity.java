package org.openhds.mobile.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.material.navigation.NavigationView;
import org.openhds.mobile.R;
import org.openhds.mobile.utilities.MessageUtils;
import org.openhds.mobile.utilities.SetupUtils;
import org.openhds.mobile.utilities.SyncUtils;

import static org.openhds.mobile.utilities.SetupUtils.startApp;
import static org.openhds.mobile.utilities.SyncUtils.DATA_INSTALLED_ACTION;

public class SetupChecklistActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int STORAGE_PERMISSION_REQUEST = 1;

    private CheckBox permissionsCheckbox, appsCheckbox, connectCheckbox, dataCheckbox;
    private Button setupButton;
    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_checklist);

        Toolbar toolbar = findViewById(R.id.setup_toolbar);
        NavigationView navView = findViewById(R.id.setup_navigation_view);
        navView.setNavigationItemSelectedListener(this);
        DrawerLayout drawerLayout = findViewById(R.id.setup_drawer_layout);

        permissionsCheckbox = findViewById(R.id.grantPermsCheckbox);
        appsCheckbox = findViewById(R.id.installAppsCheckbox);
        connectCheckbox = findViewById(R.id.serverConnectCheckbox);
        dataCheckbox = findViewById(R.id.dataDownloadCheckbox);
        setupButton = findViewById(R.id.setup_button);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setTitle(R.string.cims_setup);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DATA_INSTALLED_ACTION.equals(intent.getAction())) {
                    MessageUtils.showShortToast(SetupChecklistActivity.this, R.string.sync_database_updated);
                    updateState();
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadcastReceiver, new IntentFilter(DATA_INSTALLED_ACTION));
    }

    private void updateState() {

        final boolean hasPerms = hasPermissions(), hasApps = hasApps(), isConnected = isConnected(), hasData = hasData();

        // update checkboxes
        permissionsCheckbox.setChecked(hasPerms);
        appsCheckbox.setChecked(hasApps);
        connectCheckbox.setChecked(isConnected);
        dataCheckbox.setChecked(hasData);

        // configure button
        if (!hasPerms) {
            setupButton.setText(R.string.fix_permissions);
            setupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetupUtils.askForPermissions(SetupChecklistActivity.this, 1);
                }
            });
        } else if (!hasApps) {
            setupButton.setText(R.string.install_apps);
            setupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetupUtils.promptODKInstall(SetupChecklistActivity.this);
                }
            });
        } else if (!isConnected) {
            setupButton.setText(R.string.attach_to_server);
            setupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetupUtils.getToken(SetupChecklistActivity.this);
                }
            });
        } else if (!hasData) {
            setupButton.setText(R.string.download_data);
            setupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SyncUtils.checkForUpdate(SetupChecklistActivity.this);
                }
            });
        } else {
            setupButton.setText(R.string.next);
            setupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startApp(SetupChecklistActivity.this);
                }
            });
        }
    }

    private boolean hasData() {
        return SetupUtils.isDataAvailable(this);
    }

    private boolean isConnected() {
        return SetupUtils.isAccountInstalled(this);
    }

    private boolean hasApps() {
        return SetupUtils.isODKInstalled(getPackageManager());
    }

    private boolean hasPermissions() {
        return SetupUtils.hasRequiredPermissions(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            updateState();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.configure_server) {
            startActivity(new Intent(this, PreferenceActivity.class));
            return true;
        }
        return false;
    }
}
