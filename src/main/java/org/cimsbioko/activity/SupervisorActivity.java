package org.cimsbioko.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import org.cimsbioko.R;
import org.cimsbioko.fragment.ChecklistFragment;
import org.cimsbioko.fragment.SupervisorActionFragment;
import org.cimsbioko.model.form.FormInstance;
import org.cimsbioko.search.IndexingService;
import org.cimsbioko.utilities.FormsHelper;

import java.io.IOException;
import java.util.List;

import static org.cimsbioko.navconfig.forms.builders.PayloadTools.requiresApproval;
import static org.cimsbioko.utilities.MessageUtils.showShortToast;

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
        List<FormInstance> allFormInstances = FormsHelper.getAllUnsentFormInstances();
        for (FormInstance instance : allFormInstances) {
            try {
                if (requiresApproval(instance.load())) {
                    FormsHelper.setStatusIncomplete(Uri.parse(instance.getUriString()));
                }
            } catch (IOException e) {
                Log.e(TAG, "failure sending approved forms, form: " + instance.getFilePath(), e);
            }
        }
        showShortToast(this, R.string.launching_form);
        startActivity(new Intent(Intent.ACTION_EDIT));
    }
}
