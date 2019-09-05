package org.cimsbioko.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.cimsbioko.R;
import org.cimsbioko.adapter.ChecklistAdapter;
import org.cimsbioko.model.form.FormInstance;
import org.cimsbioko.utilities.FormsHelper;

import java.util.List;

import static org.cimsbioko.utilities.FormsHelper.deleteFormInstances;

public class ManageFormsFragment extends Fragment {

    private final String TAG = ManageFormsFragment.class.getSimpleName();

    private ChecklistAdapter adapter;

    private AlertDialog deleteConfirmDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout fragmentLayout = (RelativeLayout) inflater.inflate(R.layout.manage_forms_fragment, container, false);
        ListView listView = fragmentLayout.findViewById(R.id.manage_forms_fragment_listview);
        List<FormInstance> formInstances = FormsHelper.getAllUnsentFormInstances();

        adapter = new ChecklistAdapter(getActivity(), R.id.form_instance_check_item_orange, formInstances);
        listView.setAdapter(adapter);

        TextView headerView = fragmentLayout.findViewById(R.id.manage_forms_fragment_listview_header);
        headerView.setText(R.string.unsent_forms);

        Button primaryListButton = fragmentLayout.findViewById(R.id.manage_forms_fragment_primary_button);
        primaryListButton.setOnClickListener(new ButtonListener());

        primaryListButton.setText(R.string.delete_button_label);
        primaryListButton.setTag(R.string.delete_button_label);
        primaryListButton.setVisibility(View.VISIBLE);

        deleteConfirmDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.delete_forms_dialog_warning)
                .setTitle(R.string.delete_dialog_warning_title)
                .setPositiveButton(R.string.delete_forms, (dialogInterface, i) -> deleteSelected())
                .setNegativeButton(R.string.cancel_label, null)
                .create();

        return fragmentLayout;
    }

    private void deleteSelected() {
        List<FormInstance> formsToDelete = adapter.getCheckedInstances();
        int deleted = deleteFormInstances(formsToDelete);
        if (deleted != formsToDelete.size()) {
            Log.w(TAG, String.format("wrong number of forms deleted: expected %d, got %d", formsToDelete.size(), deleted));
        }
        adapter.removeAll(formsToDelete);
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (adapter.getCheckedInstances().size() > 0) {
                deleteConfirmDialog.show();
            }
        }
    }
}

