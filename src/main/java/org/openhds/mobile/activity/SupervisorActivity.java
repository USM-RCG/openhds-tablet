package org.openhds.mobile.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.ChecklistFragment;
import org.openhds.mobile.fragment.SupervisorActionFragment;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.search.IndexingService;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.List;

import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.requiresApproval;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

public class SupervisorActivity extends AppCompatActivity implements SupervisorActionFragment.ActionListener {

    private static final String TAG = SupervisorActivity.class.getSimpleName();

    private ChecklistFragment checklistFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTitle(R.string.supervisor_home);
        setContentView(R.layout.supervisor_activity);

        Toolbar toolbar = findViewById(R.id.supervisor_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
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
