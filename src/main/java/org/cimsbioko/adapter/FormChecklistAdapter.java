package org.cimsbioko.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import org.cimsbioko.R;
import org.cimsbioko.model.form.FormInstance;
import org.cimsbioko.utilities.LayoutUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.cimsbioko.utilities.FormUtils.editIntent;
import static org.cimsbioko.utilities.MessageUtils.showShortToast;


public class FormChecklistAdapter extends ArrayAdapter<FormInstance> {

    private final List<FormInstance> instances;
    private List<Boolean> checkStates;
    private LayoutInflater inflater;

    public FormChecklistAdapter(Context context, int checklistItemId, List<FormInstance> formInstances) {
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
        ViewGroup itemArea = convertView.findViewById(R.id.form_instance_item_area);
        itemArea.setTag(instance);
        itemArea.setOnClickListener(v -> {
            FormInstance selected = (FormInstance) v.getTag();
            Uri uri = Uri.parse(selected.getUriString());
            showShortToast(getContext(), R.string.launching_form);
            ((Activity) getContext()).startActivityForResult(editIntent(uri), 0);
        });

        // add callback when the checkbox is checked
        CheckBox checkBoxView = convertView.findViewById(R.id.form_instance_check_box);
        checkBoxView.setOnCheckedChangeListener((buttonView, isChecked) -> checkStates.set(position, isChecked));
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
