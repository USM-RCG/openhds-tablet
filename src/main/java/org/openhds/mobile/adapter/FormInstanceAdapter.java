package org.openhds.mobile.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.form.FormHelper;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.LayoutUtils;

import java.io.File;
import java.util.Map;

public class FormInstanceAdapter extends ArrayAdapter {

    private Object[] formInstances;
    private LayoutInflater inflater;


    public FormInstanceAdapter(Context context, int resource, Object[] formInstances) {
        super(context, resource, formInstances);
        this.formInstances = formInstances;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.form_instance_list_item_orange, null);
        }

        FormInstance instance = (FormInstance) formInstances[position];
        LayoutUtils.configureFormListItem(super.getContext(), convertView, instance);
        return convertView;
    }

}
