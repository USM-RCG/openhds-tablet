package org.openhds.mobile.fragment.navigate;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.openhds.mobile.R;
import org.openhds.mobile.adapter.FormInstanceAdapter;
import org.openhds.mobile.model.form.FormInstance;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.openhds.mobile.utilities.FormUtils.editIntent;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;
import static org.openhds.mobile.utilities.OdkCollectHelper.deleteFormInstances;


public class FormListFragment extends Fragment {

    ListView listView;
    FormInstanceAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        adapter = new FormInstanceAdapter(activity, R.id.form_instance_list_item, new ArrayList<FormInstance>());
        View layout = inflater.inflate(R.layout.form_list_fragment, container, false);
        listView = (ListView) layout.findViewById(R.id.form_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ClickListener());
        registerForContextMenu(listView);
        return layout;
    }

    public void addHeaderView(View header) {
        if (listView != null) {
            listView.addHeaderView(header, null, false);
        }
    }

    public void populate(List<FormInstance> forms) {
        adapter.populate(forms);
    }

    private FormInstance getItem(int pos) {
        return (FormInstance) listView.getItemAtPosition(pos);  // accounts for offset shifts from added headers
    }

    private void editForm(FormInstance selected) {
        Uri uri = Uri.parse(selected.getUriString());
        showShortToast(getActivity(), R.string.launching_odk_collect);
        startActivityForResult(editIntent(uri), 0);
    }

    private void removeForm(FormInstance selected) {
        Context ctx = getActivity();
        if (deleteFormInstances(ctx.getContentResolver(), asList(selected)) == 1) {
            adapter.remove(selected);
            showShortToast(ctx, R.string.deleted);
        }
    }

    private class ClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FormInstance selected = getItem(position);
            if (selected != null) {
                editForm(selected);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.form_list) {
            getActivity().getMenuInflater().inflate(R.menu.form_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        FormInstance selected = getItem(info.position);
        if (selected != null) {
            switch (item.getItemId()) {
                case R.id.delete_form:
                    removeForm(selected);
                    return true;
                case R.id.edit_form:
                    editForm(selected);
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }
}
