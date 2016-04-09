package org.openhds.mobile.fragment.navigate;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.openhds.mobile.R;
import org.openhds.mobile.adapter.FormInstanceAdapter;
import org.openhds.mobile.model.form.FormInstance;

import java.util.List;

import static org.openhds.mobile.utilities.FormUtils.editIntent;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;


public class ViewPathFormsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_form_fragment, container, false);
    }

    public void populateRecentFormInstanceListView(List<FormInstance> formsForPath) {
        FormInstanceAdapter adapter = new FormInstanceAdapter(getActivity(), R.id.form_instance_list_item, formsForPath.toArray());
        ListView instanceList = (ListView) getActivity().findViewById(R.id.path_forms_form_right_column);
        instanceList.setAdapter(adapter);
        instanceList.setOnItemClickListener(new ClickListener());
    }

    private void launchEdit(FormInstance selected) {
        Uri uri = Uri.parse(selected.getUriString());
        showShortToast(getActivity(), R.string.launching_odk_collect);
        startActivityForResult(editIntent(uri), 0);
    }

    private class ClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            FormInstance selected = (FormInstance) listView.getAdapter().getItem(position);
            if (selected != null) {
                launchEdit(selected);
            }
        }
    }
}
