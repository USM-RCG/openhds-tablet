package org.openhds.mobile.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.adapter.ChecklistAdapter;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.navconfig.ProjectFormFields;
import org.openhds.mobile.navconfig.ProjectResources;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.requiresApproval;
import static org.openhds.mobile.utilities.FormUtils.updateFormElement;
import static org.openhds.mobile.utilities.OdkCollectHelper.deleteFormInstances;

public class ChecklistFragment extends Fragment {

    private final String TAG = ChecklistFragment.class.getSimpleName();

    private static final String MODE_BUNDLE_KEY = "checklistFragmentMode";
    public static final String DELETE_MODE = "delete";
    public static final String APPROVE_MODE = "approve";

    private String currentMode;

    private ListView listView;
    private ChecklistAdapter adapter;
    private TextView headerView;

    private RelativeLayout fragmentLayout;

    private Button primaryListButton;
    private Button secondaryListButton;

    private AlertDialog deleteConfirmDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLayout = (RelativeLayout) inflater.inflate(R.layout.supervisor_edit_form_fragment_layout, container, false);
        listView = (ListView) fragmentLayout.findViewById(R.id.checklist_fragment_listview);
        setupApproveMode();
        deleteConfirmDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.delete_forms_dialog_warning)
                .setTitle(R.string.delete_dialog_warning_title)
                .setPositiveButton(R.string.delete_form_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteSelected();
                    }
                })
                .setNegativeButton(R.string.cancel_label, null)
                .create();
        return fragmentLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MODE_BUNDLE_KEY, currentMode);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            setMode(savedInstanceState.getString(MODE_BUNDLE_KEY));
        }
    }

    public void setMode(String mode) {
        if (ChecklistFragment.DELETE_MODE.equals(mode)) {
            setupDeleteMode();
        } else if (ChecklistFragment.APPROVE_MODE.equals(mode)) {
            setupApproveMode();
        }
    }

    private void setupDeleteMode() {
        currentMode = ChecklistFragment.DELETE_MODE;
        adapter = setupDeleteAdapter();
        listView.setAdapter(adapter);

        if (null == headerView) {
            headerView = (TextView) fragmentLayout.findViewById(R.id.checklist_fragment_listview_header);
        }
        headerView.setText(R.string.delete_unsent_forms);
        headerView.setBackgroundResource(R.drawable.form_list_header_red);

        if (null == primaryListButton) {
            primaryListButton = (Button) fragmentLayout.findViewById(R.id.checklist_fragment_primary_button);
            primaryListButton.setOnClickListener(new ButtonListener());
        }
        primaryListButton.setText(R.string.delete_button_label);
        primaryListButton.setTag(R.string.delete_button_label);
        primaryListButton.setVisibility(View.VISIBLE);
        primaryListButton.setBackgroundResource(R.drawable.form_selector_red);

        if (null == secondaryListButton) {
            secondaryListButton = (Button) fragmentLayout.findViewById(R.id.checklist_fragment_secondary_button);
            secondaryListButton.setOnClickListener(new ButtonListener());
        }

        secondaryListButton.setVisibility(View.INVISIBLE);
    }

    private ChecklistAdapter setupDeleteAdapter() {
        List<FormInstance> formInstances = OdkCollectHelper.getAllUnsentFormInstances(getActivity().getContentResolver());
        return new ChecklistAdapter(getActivity(), R.id.form_instance_check_item_orange, formInstances);
    }

    private void setupApproveMode() {
        currentMode = ChecklistFragment.APPROVE_MODE;
        adapter = setupApproveAdapter();
        listView.setAdapter(adapter);

        if (null == headerView) {
            headerView = (TextView) fragmentLayout.findViewById(R.id.checklist_fragment_listview_header);
        }
        headerView.setText(R.string.approve_unsent_forms);
        headerView.setBackgroundResource(R.drawable.form_list_header);

        if (null == primaryListButton) {
            primaryListButton = (Button) fragmentLayout.findViewById(R.id.checklist_fragment_primary_button);
            primaryListButton.setOnClickListener(new ButtonListener());
        }

        primaryListButton.setText(R.string.supervisor_approve_selected);
        primaryListButton.setTag(R.string.supervisor_approve_selected);
        primaryListButton.setVisibility(View.VISIBLE);
        primaryListButton.setBackgroundResource(R.drawable.form_selector);

        if (null == secondaryListButton) {
            secondaryListButton = (Button) fragmentLayout.findViewById(R.id.checklist_fragment_secondary_button);
            secondaryListButton.setOnClickListener(new ButtonListener());
        }

        secondaryListButton.setText(R.string.supervisor_approve_all);
        secondaryListButton.setTag(R.string.supervisor_approve_all);
        secondaryListButton.setVisibility(View.VISIBLE);
        secondaryListButton.setBackgroundResource(R.drawable.form_selector);
    }

    private ChecklistAdapter setupApproveAdapter() {
        List<FormInstance> formInstances = OdkCollectHelper.getAllUnsentFormInstances(getActivity().getContentResolver());
        List<FormInstance> needApproval = new ArrayList<>();
        for (FormInstance instance : formInstances) {
            try {
                if (requiresApproval(instance.load())) {
                    needApproval.add(instance);
                }
            } catch (IOException e) {
                Log.e(TAG, "failure during approval setup: " + e.getMessage());
            }
        }
        return new ChecklistAdapter(getActivity(), R.id.form_instance_check_item_orange, needApproval);
    }

    public void deleteSelected() {
        List<FormInstance> formsToDelete = adapter.getCheckedInstances();
        int deleted = deleteFormInstances(getActivity().getContentResolver(), formsToDelete);
        if (deleted != formsToDelete.size()) {
            Log.w(TAG, String.format("wrong number of forms deleted: expected %d, got %d", formsToDelete.size(), deleted));
        }
        adapter.removeAll(formsToDelete);
    }

    private void approveSelected() {
        adapter.removeAll(approveForms(adapter.getCheckedInstances()));
    }

    private void approveAll() {
        adapter.removeAll(approveForms(adapter.getInstances()));
    }

    private List<FormInstance> approveForms(List<FormInstance> forms) {
        List<FormInstance> approved = new ArrayList<>();
        for (FormInstance instance : forms) {
            try {
                if (requiresApproval(instance.load())) {
                    approveForm(instance);
                    approved.add(instance);
                }
            } catch (IOException e) {
                Log.e(TAG, "failed to mark form approved: " + e.getMessage());
            }
        }
        return approved;
    }

    private void approveForm(FormInstance instance) throws IOException {
        updateFormElement(ProjectFormFields.General.NEEDS_REVIEW, ProjectResources.General.FORM_NO_REVIEW_NEEDED, instance.getFilePath());
        OdkCollectHelper.setStatusComplete(getActivity().getContentResolver(), Uri.parse(instance.getUriString()));
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();
            if (tag.equals(R.string.delete_button_label) && adapter.getCheckedInstances().size() > 0) {
                deleteConfirmDialog.show();
            } else if (tag.equals(R.string.supervisor_approve_selected)) {
                approveSelected();
            } else if(tag.equals(R.string.supervisor_approve_all)) {
                approveAll();
            }
        }
    }
}

