package org.openhds.mobile.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.ChecklistFragment;
import org.openhds.mobile.fragment.SupervisorActionFragment;
import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.search.IndexingService;
import org.openhds.mobile.utilities.LoginUtils;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.List;

import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.requiresApproval;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.LoginUtils.launchLogin;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;
import static org.openhds.mobile.utilities.SyncUtils.installAccount;

public class SupervisorActivity extends AppCompatActivity implements SupervisorActionFragment.ActionListener {

    private static final String TAG = SupervisorActivity.class.getSimpleName();

    private ChecklistFragment checklistFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTitle(R.string.supervisor_home);
        setContentView(R.layout.supervisor_activity);

        if (savedInstanceState == null) {
            LoginUtils.Login<Supervisor> login = getLogin(Supervisor.class);
            if (login.hasAuthenticatedUser()) {
                Supervisor user = login.getAuthenticatedUser();
                installAccount(this, user.getName(), user.getPassword());
            }
        }

        FragmentManager fragMgr = getSupportFragmentManager();
        checklistFragment = (ChecklistFragment) fragMgr.findFragmentById(R.id.supervisor_checklist_fragment);
        SupervisorActionFragment actionFragment = (SupervisorActionFragment) fragMgr.findFragmentById(R.id.supervisor_action_fragment);
        actionFragment.setActionListener(this);
    }

    @Override
    public void onSendForms() {
        sendApprovedForms();
    }

    @Override
    public void onDeleteForms() {
        checklistFragment.setMode(ChecklistFragment.DELETE_MODE);
    }

    @Override
    public void onApproveForms() {
        checklistFragment.setMode(ChecklistFragment.APPROVE_MODE);
    }

    @Override
    public void onRebuildIndices() {
        IndexingService.queueFullReindex(SupervisorActivity.this);
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
}
