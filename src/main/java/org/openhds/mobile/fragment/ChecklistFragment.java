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

import static org.openhds.mobile.utilities.FormUtils.getFormElement;
import static org.openhds.mobile.utilities.FormUtils.updateFormElement;
import static org.openhds.mobile.utilities.OdkCollectHelper.deleteFormInstances;

public class ChecklistFragment extends Fragment {

    private final String TAG = ChecklistFragment.class.getSimpleName();

    public static String DELETE_MODE = "delete";
    public static String APPROVE_MODE = "approve";

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

    public void resetCurrentMode() {
        setMode(currentMode);
    }

    public void setMode(String mode) {
        if (mode.equalsIgnoreCase(ChecklistFragment.DELETE_MODE)) {
            setupDeleteMode();
        } else if (mode.equalsIgnoreCase(ChecklistFragment.APPROVE_MODE)) {
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
        headerView.setText(R.string.checklist_fragment_listview_header_delete);
        headerView.setBackgroundResource(R.drawable.form_list_header_drawable_red);

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
        headerView.setText(R.string.checklist_fragment_listview_header_approve);
        headerView.setBackgroundResource(R.drawable.form_list_header_drawable_orange);

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
                String needsReview = getFormElement(ProjectFormFields.General.NEEDS_REVIEW, instance.getFilePath());
                if (ProjectResources.General.FORM_NEEDS_REVIEW.equalsIgnoreCase(needsReview)) {
                    needApproval.add(instance);
                }
            } catch (IOException e) {
                Log.e(TAG, "failure during approval setup: " + e.getMessage());
            }
        }

        return new ChecklistAdapter(getActivity(), R.id.form_instance_check_item_orange, needApproval);
    }

    public void deleteSelected() {
        List<FormInstance> formsToDelete = adapter.getCheckedForms(), allForms = adapter.getFormInstanceList();
        int deleted = deleteFormInstances(getActivity().getContentResolver(), formsToDelete);
        if (deleted != formsToDelete.size()) {
            Log.w(TAG, String.format("wrong number of forms deleted: expected %d, got %d", formsToDelete.size(), deleted));
        }
        if (allForms.removeAll(formsToDelete)) {
            adapter.resetFormInstanceList(allForms);
        }
    }

    private void processApproveSelectedRequest() {
        List<FormInstance> approvedForms = adapter.getCheckedForms();
        approveForms(approvedForms);

        List<FormInstance> allForms = adapter.getFormInstanceList();
        allForms.removeAll(approvedForms);
        adapter.resetFormInstanceList(allForms);
    }

    private void processApproveAllRequest() {
        List<FormInstance> approvedForms = adapter.getFormInstanceList();
        approveForms(approvedForms);
        adapter.resetFormInstanceList(new ArrayList<FormInstance>());
    }

    private void approveForms(List<FormInstance> forms) {
        for (FormInstance instance: forms) {
            try {
                updateFormElement(ProjectFormFields.General.NEEDS_REVIEW, ProjectResources.General.FORM_NO_REVIEW_NEEDED,
                        instance.getFilePath());
                OdkCollectHelper.setStatusComplete(getActivity().getContentResolver(), Uri.parse(instance.getUriString()));
            } catch (IOException e) {
                Log.e(TAG, "failed to mark form approved: " + e.getMessage());
            }
        }
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Integer tag = (Integer) v.getTag();
            if (tag.equals(R.string.delete_button_label)) {
                deleteConfirmDialog.show();
            } else if (tag.equals(R.string.supervisor_approve_selected)) {
                processApproveSelectedRequest();
            } else if(tag.equals(R.string.supervisor_approve_all)) {
                processApproveAllRequest();
            }
        }
    }
}

