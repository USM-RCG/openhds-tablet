package org.cimsbioko.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
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
import org.cimsbioko.R;
import org.cimsbioko.utilities.MessageUtils;
import org.cimsbioko.utilities.SetupUtils;
import org.cimsbioko.utilities.SyncUtils;

import static org.cimsbioko.utilities.SetupUtils.CAMPAIGN_DOWNLOADED_ACTION;
import static org.cimsbioko.utilities.SetupUtils.startApp;
import static org.cimsbioko.utilities.SyncUtils.DATA_INSTALLED_ACTION;

public class SetupChecklistActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int STORAGE_PERMISSION_REQUEST = 1;

    private CheckBox permissionsCheckbox, appsCheckbox, connectCheckbox, configCheckbox, dataCheckbox, formsCheckbox;
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
        configCheckbox = findViewById(R.id.configDownloadCheckbox);
        dataCheckbox = findViewById(R.id.dataDownloadCheckbox);
        formsCheckbox = findViewById(R.id.formsCheckbox);
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
                } else if (CAMPAIGN_DOWNLOADED_ACTION.equals(intent.getAction())) {
                    MessageUtils.showShortToast(SetupChecklistActivity.this, "Campaign downloaded");
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
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(broadcastReceiver, new IntentFilter(DATA_INSTALLED_ACTION));
        lbm.registerReceiver(broadcastReceiver, new IntentFilter(CAMPAIGN_DOWNLOADED_ACTION));
    }

    private void updateState() {

        final boolean hasPerms = hasPermissions(), hasApps = hasApps(), isConnected = isConnected(),
                hasData = hasData(), hasConfig = hasConfig(), hasForms = hasForms();

        // update checkboxes
        permissionsCheckbox.setChecked(hasPerms);
        appsCheckbox.setChecked(hasApps);
        connectCheckbox.setChecked(isConnected);
        configCheckbox.setChecked(hasConfig);
        dataCheckbox.setChecked(hasData);
        formsCheckbox.setChecked(hasForms);

        // configure button
        if (!hasPerms) {
            setupButton.setText(R.string.fix_permissions);
            setupButton.setOnClickListener(v -> SetupUtils.askForPermissions(this, 1));
        } else if (!hasApps) {
            setupButton.setText(R.string.install_apps);
            setupButton.setOnClickListener(v -> SetupUtils.promptFormsAppInstall(this));
        } else if (!isConnected) {
            setupButton.setText(R.string.attach_to_server);
            setupButton.setOnClickListener(v -> SetupUtils.getToken(this, null));
        } else if (!hasConfig) {
            setupButton.setText(R.string.download_campaign);
            setupButton.setOnClickListener(v -> SetupUtils.downloadConfig(this));
        } else if (!hasData) {
            setupButton.setText(R.string.download_data);
            setupButton.setOnClickListener(v -> SyncUtils.checkForUpdate());
        } else if (!hasForms) {
            setupButton.setText(R.string.download_forms);
            setupButton.setOnClickListener(v -> SetupUtils.downloadForms(this));
        } else {
            setupButton.setText(R.string.next);
            setupButton.setOnClickListener(v -> startApp(this));
        }
    }

    private boolean hasData() {
        return SetupUtils.isDataAvailable(this);
    }

    private boolean hasConfig() { return SetupUtils.isConfigAvailable(); }

    private boolean isConnected() {
        return SetupUtils.isAccountInstalled();
    }

    private boolean hasApps() {
        return SetupUtils.isFormsAppInstalled(getPackageManager());
    }

    private boolean hasPermissions() {
        return SetupUtils.hasRequiredPermissions(this);
    }

    private boolean hasForms() {
        return SetupUtils.hasCampaignForms();
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
        if (item.getItemId() == R.id.configure_settings) {
            startActivity(new Intent(this, PreferenceActivity.class));
            return true;
        }
        return false;
    }
}
