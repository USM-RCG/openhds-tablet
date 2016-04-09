package org.openhds.mobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.openhds.mobile.R;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.utilities.LayoutUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.FormUtils.editIntent;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;


public class ChecklistAdapter extends ArrayAdapter {

    private final String TAG = ChecklistAdapter.class.getName();

    private List<FormInstance> formInstanceList;
    private List<Boolean> checkList;
    private LayoutInflater inflater;

    @SuppressWarnings("unchecked")
    public ChecklistAdapter(Context context, int checklistItemId, List<FormInstance> formInstances) {
        super(context, checklistItemId, formInstances);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.formInstanceList = formInstances;
        initializeCheckBoxes(this.formInstanceList);
    }

    private void initializeCheckBoxes(List<FormInstance> instances) {
        ArrayList<Boolean> newCheckList = new ArrayList<>();
        for (int i = 0; i < instances.size(); i++) {
            newCheckList.add(false);
        }
        this.checkList = newCheckList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_check_item, null);
        }

        // set up the basics to display the form instance info
        FormInstance instance = formInstanceList.get(position);
        try {
            LayoutUtils.configureFormListItem(super.getContext(), convertView, instance);

            // add callback when the form instance info is pressed
            ViewGroup itemArea = (ViewGroup) convertView.findViewById(R.id.form_instance_item_area);
            itemArea.setTag(instance);
            itemArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FormInstance selected = (FormInstance) v.getTag();
                    Uri uri = Uri.parse(selected.getUriString());
                    showShortToast(getContext(), R.string.launching_odk_collect);
                    ((Activity) getContext()).startActivityForResult(editIntent(uri), 0);
                }
            });

            // add callback when the checkbox is checked
            CheckBox checkBoxView = (CheckBox) convertView.findViewById(R.id.form_instance_check_box);
            checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkList.set(position, isChecked);
                }
            });
            checkBoxView.setChecked(checkList.get(position));
        } catch (IOException e) {
            Log.e(TAG, "failed to setup checklist item", e);
        }
        return convertView;
    }

    public List<FormInstance> getCheckedForms() {

        List<FormInstance> checkedForms = new ArrayList<>();

        for (int i = 0; i < checkList.size(); i++) {
            if (checkList.get(i)) {
                FormInstance formInstance = formInstanceList.get(i);
                checkedForms.add(formInstance);
            }
        }

        return checkedForms;
    }

    public List<FormInstance> getFormInstanceList() {
        return formInstanceList;
    }

    public void resetFormInstanceList(List<FormInstance> formInstanceList) {
        this.formInstanceList = formInstanceList;
        initializeCheckBoxes(this.formInstanceList);
        notifyDataSetChanged();
    }
}
