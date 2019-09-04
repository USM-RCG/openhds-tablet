package org.cimsbioko.activity;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import org.cimsbioko.R;
import org.cimsbioko.fragment.ChecklistFragment;
import org.cimsbioko.fragment.SupervisorActionFragment;
import org.cimsbioko.search.IndexingService;


public class SupervisorActivity extends AppCompatActivity implements SupervisorActionFragment.ActionListener {

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


}
