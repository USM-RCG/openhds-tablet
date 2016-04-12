package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.ChecklistFragment;
import org.openhds.mobile.fragment.DeleteWarningDialogFragment;
import org.openhds.mobile.fragment.DeleteWarningDialogListener;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.fragment.SyncDatabaseFragment;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.FormUtils.isFormReviewed;
import static org.openhds.mobile.utilities.LayoutUtils.makeButton;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;
import static org.openhds.mobile.utilities.SyncUtils.installAccount;

public class SupervisorMainActivity extends Activity implements DeleteWarningDialogListener {

    private static final String TAG = SupervisorMainActivity.class.getSimpleName();

    private static final String CHECKLIST_FRAGMENT_TAG = "checklistFragment";
    private static final String SYNC_FRAGMENT_TAG = "syncDatabaseFragment";
    private static final String PREFERENCE_FRAGMENT_TAG = "preferenceFragment";

    private ChecklistFragment checklistFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supervisor_main);

        LinearLayout supervisorButtonLayout = (LinearLayout) findViewById(R.id.supervisor_activity_options);
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        makeButton(this,
                R.string.search_database_description,
                R.string.search_database_label,
                R.string.search_database_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.send_finalized_forms_description,
                R.string.send_finalized_forms_label,
                R.string.send_finalized_forms_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.delete_recent_forms_description,
                R.string.delete_recent_forms_label,
                R.string.delete_recent_forms_label,
                buttonClickListener,
                supervisorButtonLayout);

        makeButton(this,
                R.string.approve_recent_forms_description,
                R.string.approve_recent_forms_label,
                R.string.approve_recent_forms_label,
                buttonClickListener,
                supervisorButtonLayout);


        if (savedInstanceState == null)  {
            SyncDatabaseFragment syncDatabaseFragment = new SyncDatabaseFragment();
            syncDatabaseFragment.setRetainInstance(true);
            PreferenceFragment preferenceFragment = new LoginPreferenceFragment();
            checklistFragment = new ChecklistFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.supervisor_edit_form_container, checklistFragment, CHECKLIST_FRAGMENT_TAG)
                    .add(R.id.supervisor_auxiliary_container, syncDatabaseFragment, SYNC_FRAGMENT_TAG)
                    .add(R.id.supervisor_activity_options, preferenceFragment, PREFERENCE_FRAGMENT_TAG)
                    .commit();

        } else {
            checklistFragment = (ChecklistFragment) getFragmentManager().findFragmentByTag(CHECKLIST_FRAGMENT_TAG);
        }

        Bundle extras = getIntent().getExtras();
        String username = (String) extras.get(OpeningActivity.USERNAME_KEY);
        String password = (String) extras.get(OpeningActivity.PASSWORD_KEY);
        installAccount(this, username, password);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checklistFragment.resetCurrentMode();
    }

    private void searchDatabase() {
        ArrayList<FormSearchPluginModule> searchPluginModules = new ArrayList<>();
        searchPluginModules.add(SearchUtils.getFieldWorkerPlugin("fieldWorker"));
        searchPluginModules.add(SearchUtils.getIndividualPlugin("individual", R.string.search_individual_label));
        searchPluginModules.add(SearchUtils.getLocationPlugin("location"));
        searchPluginModules.add(SearchUtils.getSocialGroupPlugin("socialGroup"));

        Intent intent = new Intent(this, FormSearchActivity.class);
        intent.putParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY, searchPluginModules);
        startActivity(intent);
    }

    public void sendApprovedForms() {

        List<FormInstance> allFormInstances = OdkCollectHelper.getAllUnsentFormInstances(this.getContentResolver());
        for (FormInstance instance: allFormInstances) {
            try {
                if (!isFormReviewed(instance.getFilePath())) {
                    OdkCollectHelper.setStatusIncomplete(this.getContentResolver(), Uri.parse(instance.getUriString()));
                }
            } catch (IOException e) {
                Log.e(TAG, "failure sending approved forms, form: " + instance.getFilePath(), e);
            }
        }
        showShortToast(this, R.string.launching_odk_collect);
        startActivity(new Intent(Intent.ACTION_EDIT));
    }

    public void createWarningDialog() {
        DeleteWarningDialogFragment warning = new DeleteWarningDialogFragment();
        warning.show(getFragmentManager(), "DeleteWarningDialogFragment");
    }

    public void onDialogPositiveClick(DialogFragment dialogFragment) {

        checklistFragment.processDeleteRequest(false);

    }

    public void onDialogNegativeClick(DialogFragment dialogFragment) {

        //do nothing?

    }

    private class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();
            if (tag.equals(R.string.search_database_label)) {
                searchDatabase();
            } else if (tag.equals(R.string.send_finalized_forms_label)) {
                sendApprovedForms();
            } else if (tag.equals(R.string.delete_recent_forms_label)) {
                checklistFragment.setMode(ChecklistFragment.DELETE_MODE);
            } else if (tag.equals(R.string.approve_recent_forms_label)) {
                checklistFragment.setMode(ChecklistFragment.APPROVE_MODE);
            }
        }
    }
}
