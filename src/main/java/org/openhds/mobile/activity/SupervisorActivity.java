package org.openhds.mobile.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.ChecklistFragment;
import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.search.IndexingService;
import org.openhds.mobile.utilities.LoginUtils;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.List;

import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.requiresApproval;
import static org.openhds.mobile.search.Utils.isSearchEnabled;
import static org.openhds.mobile.utilities.LayoutUtils.makeButton;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.LoginUtils.launchLogin;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;
import static org.openhds.mobile.utilities.SyncUtils.installAccount;

public class SupervisorActivity extends AppCompatActivity {

    private static final String TAG = SupervisorActivity.class.getSimpleName();

    private ChecklistFragment checklistFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTitle(R.string.supervisor_home);
        setContentView(R.layout.supervisor_main);

        LinearLayout supervisorButtonLayout = (LinearLayout) findViewById(R.id.supervisor_activity_options);
        ButtonClickListener buttonClickListener = new ButtonClickListener();

        makeButton(this, -1, R.string.send_forms, R.string.send_forms,
                buttonClickListener, supervisorButtonLayout);

        Button button;
        final int BUTTON_SPACING = 10;

        button = makeButton(this, -1, R.string.delete_forms, R.string.delete_forms,
                buttonClickListener, supervisorButtonLayout);

        ((RelativeLayout.LayoutParams) button.getLayoutParams()).setMargins(0, BUTTON_SPACING, 0, 0);

        button = makeButton(this, -1, R.string.approve_forms, R.string.approve_forms,
                buttonClickListener, supervisorButtonLayout);

        ((RelativeLayout.LayoutParams) button.getLayoutParams()).setMargins(0, BUTTON_SPACING, 0, 0);

        if (isSearchEnabled(this)) {
            button = makeButton(this, -1, R.string.rebuild_search_indices, R.string.rebuild_search_indices,
                    buttonClickListener, supervisorButtonLayout);
            ((RelativeLayout.LayoutParams) button.getLayoutParams()).setMargins(0, BUTTON_SPACING, 0, 0);
        }

        if (savedInstanceState == null) {
            LoginUtils.Login<Supervisor> login = getLogin(Supervisor.class);
            if (login.hasAuthenticatedUser()) {
                Supervisor user = login.getAuthenticatedUser();
                installAccount(this, user.getName(), user.getPassword());
            }
        }

        checklistFragment = (ChecklistFragment) getSupportFragmentManager().findFragmentById(R.id.supervisor_checklist_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getLogin(Supervisor.class).hasAuthenticatedUser()) {
            launchLogin(this, true);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.supervisor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_button:
                getLogin(Supervisor.class).logout(this, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sendApprovedForms() {
        ContentResolver resolver = getContentResolver();
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllUnsentFormInstances(resolver);
        for (FormInstance instance : allFormInstances) {
            try {
                if (requiresApproval(instance.load())) {
                    OdkCollectHelper.setStatusIncomplete(resolver, Uri.parse(instance.getUriString()));
                }
            } catch (IOException e) {
                Log.e(TAG, "failure sending approved forms, form: " + instance.getFilePath(), e);
            }
        }
        showShortToast(this, R.string.launching_odk_collect);
        startActivity(new Intent(Intent.ACTION_EDIT));
    }

    private class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();
            if (tag.equals(R.string.send_forms)) {
                sendApprovedForms();
            } else if (tag.equals(R.string.delete_forms)) {
                checklistFragment.setMode(ChecklistFragment.DELETE_MODE);
            } else if (tag.equals(R.string.approve_forms)) {
                checklistFragment.setMode(ChecklistFragment.APPROVE_MODE);
            } else if (tag.equals(R.string.rebuild_search_indices) && isSearchEnabled(SupervisorActivity.this)) {
                IndexingService.queueFullReindex(SupervisorActivity.this);
            }
        }
    }
}
