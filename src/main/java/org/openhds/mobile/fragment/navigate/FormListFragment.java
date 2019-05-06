package org.openhds.mobile.fragment.navigate;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigatorActivity;
import org.openhds.mobile.adapter.FormInstanceAdapter;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.navconfig.HierarchyPath;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.provider.DatabaseAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Arrays.asList;
import static org.openhds.mobile.activity.FieldWorkerActivity.ACTIVITY_MODULE_EXTRA;
import static org.openhds.mobile.activity.HierarchyNavigatorActivity.HIERARCHY_PATH_KEY;
import static org.openhds.mobile.utilities.ConfigUtils.getActiveModuleForBinding;
import static org.openhds.mobile.utilities.ConfigUtils.getActiveModules;
import static org.openhds.mobile.utilities.FormUtils.editIntent;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;
import static org.openhds.mobile.utilities.OdkCollectHelper.deleteFormInstances;


public class FormListFragment extends Fragment {

    private static final String FIND_ENABLED_KEY = "FIND_ENABLED";

    TextView headerView;
    ListView listView;
    FormInstanceAdapter adapter;
    boolean isFindEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        adapter = new FormInstanceAdapter(activity, R.id.form_instance_list_item, new ArrayList<FormInstance>());
        View layout = inflater.inflate(R.layout.form_list_fragment, container, false);
        headerView = layout.findViewById(R.id.form_list_header);
        listView = layout.findViewById(R.id.form_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ClickListener());
        registerForContextMenu(listView);
        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(FIND_ENABLED_KEY, isFindEnabled);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            isFindEnabled = savedInstanceState.getBoolean(FIND_ENABLED_KEY);
        }
    }

    public void setFindEnabled(boolean showFind) {
        isFindEnabled = showFind;
    }

    public void setHeaderText(Integer resourceId) {
        if (resourceId != null) {
            headerView.setText(resourceId);
            headerView.setVisibility(VISIBLE);
        } else {
            headerView.setVisibility(GONE);
        }
    }

    public void populate(List<FormInstance> forms) {
        adapter.populate(forms);
    }

    private FormInstance getItem(int pos) {
        return (FormInstance) listView.getItemAtPosition(pos);  // accounts for offset shifts from added headers
    }

    private void editForm(FormInstance selected) {
        if (selected.canEdit()) {
            Uri uri = Uri.parse(selected.getUriString());
            showShortToast(getActivity(), R.string.launching_odk_collect);
            startActivityForResult(editIntent(uri), 0);
        } else {
            showShortToast(getActivity(), R.string.form_not_editable);
        }
    }

    private void removeForm(FormInstance selected) {
        Context ctx = getActivity();
        if (deleteFormInstances(ctx.getContentResolver(), asList(selected)) == 1) {
            adapter.remove(selected);
            showShortToast(ctx, R.string.deleted);
        }
    }

    private void findForm(FormInstance selected) {
        Context ctx = getActivity();
        String pathStr = DatabaseAdapter.getInstance(ctx).findHierarchyForForm(selected.getFilePath());
        HierarchyPath path = HierarchyPath.fromString(ctx.getContentResolver(), pathStr);
        if (path != null) {
            try {
                // lookup the binding for the form
                Binding binding = FormInstance.getBinding(selected.load());

                // find the module(s) that include the binding, or just include all modules if none
                Collection<NavigatorModule> modules = getActiveModuleForBinding(ctx, binding);
                if (modules.isEmpty()) {
                    modules = getActiveModules(ctx);
                }

                // Launch the navigator using the first relevant module
                if (!modules.isEmpty()) {
                    NavigatorModule firstModule = modules.iterator().next();
                    Intent intent = new Intent(ctx, HierarchyNavigatorActivity.class);
                    intent.putExtra(ACTIVITY_MODULE_EXTRA, firstModule.getName());
                    intent.putExtra(HIERARCHY_PATH_KEY, path);
                    startActivity(intent);
                } else {
                    showShortToast(ctx, R.string.no_active_modules);
                }

            } catch (IOException e) {
                showShortToast(ctx, R.string.form_load_failed);
            }
        } else {
            showShortToast(ctx, R.string.form_not_found);
        }
    }

    private void confirmDelete(final FormInstance selected) {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.delete_forms_dialog_warning)
                .setTitle(R.string.delete_dialog_warning_title)
                .setPositiveButton(R.string.delete_forms, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeForm(selected);
                    }
                })
                .setNegativeButton(R.string.cancel_label, null)
                .create()
                .show();
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
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            FormInstance selected = getItem(info.position);
            if (selected != null) {
                getActivity().getMenuInflater().inflate(R.menu.formlist_menu, menu);
                menu.findItem(R.id.edit_form).setVisible(selected.canEdit());
                menu.findItem(R.id.find_form).setVisible(isFindEnabled);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        FormInstance selected;
        switch (item.getItemId()) {
            case R.id.find_form:
                selected = getItem(info.position);
                if (selected != null) {
                    findForm(selected);
                }
                return true;
            case R.id.delete_form:
                selected = getItem(info.position);
                if (selected != null) {
                    confirmDelete(selected);
                }
                return true;
            case R.id.edit_form:
                selected = getItem(info.position);
                if (selected != null) {
                    editForm(selected);
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
