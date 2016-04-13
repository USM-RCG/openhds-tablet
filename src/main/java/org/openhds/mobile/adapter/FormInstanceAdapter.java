package org.openhds.mobile.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.openhds.mobile.R;
import org.openhds.mobile.model.form.FormInstance;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureFormListItem;

public class FormInstanceAdapter extends ArrayAdapter<FormInstance> {

    private List<FormInstance> instances;
    private LayoutInflater inflater;

    @SuppressWarnings("unchecked")
    public FormInstanceAdapter(Context context, int resource, List<FormInstance> instances) {
        super(context, resource, instances);
        this.instances = instances;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_list_item, null);
        }
        configureFormListItem(getContext(), convertView, instances.get(position));
        return convertView;
    }

    public void populate(List<FormInstance> formsForPath) {
        instances.clear();
        instances.addAll(formsForPath);
        notifyDataSetChanged();
    }
}
