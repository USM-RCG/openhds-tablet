package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.DataSelectionFragment;
import org.openhds.mobile.fragment.SearchFragment;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.search.EntityFieldSearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithValueAndLabel;
import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;


public class EntitySearchActivity extends Activity implements DataSelectionFragment.DataSelectionListener {

    public static final String SEARCH_MODULES_KEY = "entitySearchModules";
    public static final String FORM_BINDING_KEY = "entitySearchFormBinding";
    public static final String ORIGINAL_DATA_KEY = "entitySearchOriginalData";

    private SearchFragment searchFragment;
    private DataSelectionFragment selectionFragment;
    private EntityFieldSearch selectedModule;
    private ModuleListAdapter moduleListAdapter;
    private ListView listView;

    private String formBinding;
    private Serializable originalData;
    private ArrayList<EntityFieldSearch> searchModules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.form_search_activity);
        setTitle(R.string.search_database_label);

        searchFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
        selectionFragment = (DataSelectionFragment) getFragmentManager().findFragmentById(R.id.search_selection_fragment);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            searchModules = intent.getParcelableArrayListExtra(SEARCH_MODULES_KEY);
            formBinding = intent.getStringExtra(FORM_BINDING_KEY);
            originalData = intent.getSerializableExtra(ORIGINAL_DATA_KEY);
        } else {
            searchModules = savedInstanceState.getParcelableArrayList(SEARCH_MODULES_KEY);
            formBinding = savedInstanceState.getString(FORM_BINDING_KEY);
            originalData = savedInstanceState.getSerializable(ORIGINAL_DATA_KEY);
        }

        searchFragment.setResultsHandler(new SearchResultsHandler());

        Button doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new DoneButtonListener());

        listView = (ListView) findViewById(R.id.form_search_list_view);
        listView.setOnItemClickListener(new PluginClickListener());

        moduleListAdapter = new ModuleListAdapter(this, 0, searchModules);
        listView.setAdapter(moduleListAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(SEARCH_MODULES_KEY, searchModules);
        savedInstanceState.putString(FORM_BINDING_KEY, formBinding);
        savedInstanceState.putSerializable(ORIGINAL_DATA_KEY, originalData);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDataSelected(DataWrapper data) {
        selectedModule.setValue(data.getUuid());
        moduleListAdapter.notifyDataSetChanged();
    }

    private class ModuleListAdapter extends ArrayAdapter<EntityFieldSearch> {

        private static final int LABEL_COLOR = R.color.Gray;
        private static final int VALUE_COLOR = R.color.DarkGray;
        private static final int MISSING_COLOR = R.color.Red;

        public ModuleListAdapter(Context context, int resource, List<EntityFieldSearch> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            EntityFieldSearch plugin = getItem(position);
            if (convertView == null) {
                convertView = makeLargeTextWithValueAndLabel(EntitySearchActivity.this,
                        plugin.getLabelId(), plugin.getValue(), LABEL_COLOR, VALUE_COLOR, MISSING_COLOR);
            } else {
                configureTextWithValueAndLabel((RelativeLayout) convertView,
                        plugin.getLabelId(), plugin.getValue(), LABEL_COLOR, VALUE_COLOR, MISSING_COLOR);
            }
            convertView.setBackgroundResource(R.drawable.gray_list_item_selector);
            return convertView;
        }
    }

    private class PluginClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedModule = moduleListAdapter.getItem(position);
            searchFragment.configure(selectedModule);
            selectionFragment.clearData();
        }
    }

    private class SearchResultsHandler implements SearchFragment.ResultsHandler {
        @Override
        public void handleSearchResults(List<DataWrapper> dataWrappers) {
            selectionFragment.populateData(dataWrappers);
        }
    }

    private class DoneButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent data = new Intent();
            data.putParcelableArrayListExtra(SEARCH_MODULES_KEY, searchModules);
            data.putExtra(FORM_BINDING_KEY, formBinding);
            data.putExtra(ORIGINAL_DATA_KEY, originalData);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
