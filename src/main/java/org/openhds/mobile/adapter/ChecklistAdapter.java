package org.openhds.mobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.openhds.mobile.R;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.utilities.LayoutUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.openhds.mobile.utilities.FormUtils.editIntent;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;


public class ChecklistAdapter extends ArrayAdapter<FormInstance> {

    private final List<FormInstance> instances;
    private List<Boolean> checkStates;
    private LayoutInflater inflater;

    @SuppressWarnings("unchecked")
    public ChecklistAdapter(Context context, int checklistItemId, List<FormInstance> formInstances) {
        super(context, checklistItemId, formInstances);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        instances = formInstances;
        initCheckStates();
    }

    private void initCheckStates() {
        List<Boolean> newStates = new ArrayList<>(getCount());
        for (int i = 0; i < getCount(); i++) {
            newStates.add(false);

        }
        checkStates = newStates;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_check_item, null);
        }

        // set up the basics to display the form instance info
        FormInstance instance = getItem(position);
        LayoutUtils.configureFormListItem(getContext(), convertView, instance);

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
                checkStates.set(position, isChecked);
            }
        });
        checkBoxView.setChecked(checkStates.get(position));

        return convertView;
    }

    public List<FormInstance> getCheckedInstances() {
        List<FormInstance> checkedForms = new ArrayList<>();
        for (int i = 0; i < checkStates.size(); i++) {
            if (checkStates.get(i)) {
                checkedForms.add(getItem(i));
            }
        }
        return unmodifiableList(checkedForms);
    }
    
    public List<FormInstance> getInstances() {
        return unmodifiableList(instances);
    }

    public boolean removeAll(List<FormInstance> instances) {
        try {
            setNotifyOnChange(false);  // disable auto-notify until we're done
            boolean result = this.instances.removeAll(instances);
            initCheckStates();
            notifyDataSetChanged();  // manually notify
            return result;
        } finally {
            setNotifyOnChange(true);  // re-enable auto-notify
        }
    }
}
