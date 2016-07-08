package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.ChecklistFragment;
import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.model.form.FormInstance;

import org.openhds.mobile.utilities.LoginUtils;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.List;

import static org.openhds.mobile.utilities.FormUtils.isFormReviewed;
import static org.openhds.mobile.utilities.LayoutUtils.makeButton;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;
import static org.openhds.mobile.utilities.SyncUtils.installAccount;

public class SupervisorActivity extends Activity {

    private static final String TAG = SupervisorActivity.class.getSimpleName();

    private ChecklistFragment checklistFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTitle(R.string.supervisor_home);
        setContentView(R.layout.supervisor_main);

        LinearLayout supervisorButtonLayout = (LinearLayout) findViewById(R.id.supervisor_activity_options);
        ButtonClickListener buttonClickListener = new ButtonClickListener();

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

        if (savedInstanceState == null) {
            LoginUtils.Login<Supervisor> login = getLogin(Supervisor.class);
            if (login.hasAuthenticatedUser()) {
                Supervisor user = login.getAuthenticatedUser();
                installAccount(this, user.getName(), user.getPassword());
            }
        }

        checklistFragment = (ChecklistFragment) getFragmentManager().findFragmentById(R.id.supervisor_checklist_fragment);
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

    private class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();
            if (tag.equals(R.string.send_finalized_forms_label)) {
                sendApprovedForms();
            } else if (tag.equals(R.string.delete_recent_forms_label)) {
                checklistFragment.setMode(ChecklistFragment.DELETE_MODE);
            } else if (tag.equals(R.string.approve_recent_forms_label)) {
                checklistFragment.setMode(ChecklistFragment.APPROVE_MODE);
            }
        }
    }
}
