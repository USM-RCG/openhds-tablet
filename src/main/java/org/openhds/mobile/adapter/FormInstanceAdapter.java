package org.openhds.mobile.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import org.openhds.mobile.R;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.utilities.LayoutUtils;

import java.io.IOException;

public class FormInstanceAdapter extends ArrayAdapter {

    private final String TAG = FormInstanceAdapter.class.getName();

    private Object[] formInstances;
    private LayoutInflater inflater;

    @SuppressWarnings("unchecked")
    public FormInstanceAdapter(Context context, int resource, Object[] formInstances) {
        super(context, resource, formInstances);
        this.formInstances = formInstances;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_list_item, null);
        }

        FormInstance instance = (FormInstance) formInstances[position];
        try {
            LayoutUtils.configureFormListItem(super.getContext(), convertView, instance);
        } catch (IOException e) {
            Log.e(TAG, "failed to configure form list item", e);
        }
        return convertView;
    }
}
