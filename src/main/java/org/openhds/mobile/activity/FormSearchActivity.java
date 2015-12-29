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
import org.openhds.mobile.repository.search.FormSearchPluginModule;

import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithValueAndLabel;
import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;


public class FormSearchActivity extends Activity {

    public static final String FORM_SEARCH_PLUGINS_KEY = "formSearchPlugins";

    private static final String SEARCH_FRAGMENT_TAG = "searchFragment";
    private static final String VALUE_SELECTION_FRAGMENT_TAG = "valueSelectionFragment";

    private SearchFragment searchFragment;
    private DataSelectionFragment dataSelectionFragment;
    private ArrayList<FormSearchPluginModule> formSearchPluginModules;
    private FormSearchPluginModule currentFormSearchPluginModule;
    private SearchPluginListAdapter searchPluginListAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.form_search_activity);
        setTitle(R.string.search_database_label);

        if (null == savedInstanceState) {
            searchFragment = new SearchFragment();
            dataSelectionFragment = new DataSelectionFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.form_search_middle_column, searchFragment, SEARCH_FRAGMENT_TAG)
                    .add(R.id.form_search_right_column, dataSelectionFragment, VALUE_SELECTION_FRAGMENT_TAG)
                    .commit();

            // what does the calling activity need the user to search for?
            formSearchPluginModules = getIntent().getParcelableArrayListExtra(FORM_SEARCH_PLUGINS_KEY);

        } else {
            searchFragment = (SearchFragment) getFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
            dataSelectionFragment = (DataSelectionFragment) getFragmentManager().findFragmentByTag(VALUE_SELECTION_FRAGMENT_TAG);

            // recall pending and completed searches
            formSearchPluginModules = savedInstanceState.getParcelableArrayList(FORM_SEARCH_PLUGINS_KEY);
        }

        dataSelectionFragment.setDataSelectionDrawableId(R.drawable.gray_list_item_selector);
        searchFragment.setResultsHandler(new SearchResultsHandler());
        dataSelectionFragment.setSelectionHandler(new DataSelectionHandler());
    }

    @Override
    protected void onResume() {
        super.onResume();

        Button doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new DoneButtonListener());

        listView = (ListView) findViewById(R.id.form_search_list_view);
        listView.setOnItemClickListener(new PluginClickListener());

        searchFragment.setSearchPluginModules(null);

        populateSearchList();
    }

    private void populateSearchList() {
        searchPluginListAdapter = new SearchPluginListAdapter(this, 0, formSearchPluginModules);
        listView.setAdapter(searchPluginListAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // remember pending and completed searches
        savedInstanceState.putParcelableArrayList(FORM_SEARCH_PLUGINS_KEY, formSearchPluginModules);
        super.onSaveInstanceState(savedInstanceState);
    }

    private class SearchPluginListAdapter extends ArrayAdapter<FormSearchPluginModule> {

        private static final int LABEL_COLOR = R.color.BiokoDataFill;
        private static final int VALUE_COLOR = R.color.BiokoDataBorder;
        private static final int MISSING_COLOR = R.color.UpdateDataFill;

        public SearchPluginListAdapter(Context context, int resource, List<FormSearchPluginModule> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            FormSearchPluginModule plugin = getItem(position);

            if (convertView == null) {
                convertView = makeLargeTextWithValueAndLabel(FormSearchActivity.this,
                        plugin.getLabelId(), plugin.getFieldValue(), LABEL_COLOR, VALUE_COLOR, MISSING_COLOR);
            } else {
                configureTextWithValueAndLabel((RelativeLayout) convertView,
                        plugin.getLabelId(), plugin.getFieldValue(), LABEL_COLOR, VALUE_COLOR, MISSING_COLOR);
            }
            convertView.setBackgroundResource(R.drawable.gray_list_item_selector);
            return convertView;
        }
    }

    private class PluginClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            currentFormSearchPluginModule = searchPluginListAdapter.getItem(position);

            List<FormSearchPluginModule> plugins = new ArrayList<>();
            plugins.add(currentFormSearchPluginModule);
            searchFragment.setSearchPluginModules(plugins);
            dataSelectionFragment.clearData();
        }
    }

    private class SearchResultsHandler implements SearchFragment.ResultsHandler {
        @Override
        public void handleSearchResults(List<DataWrapper> dataWrappers) {
            dataSelectionFragment.populateData(dataWrappers);
        }
    }

    private class DataSelectionHandler implements DataSelectionFragment.SelectionHandler {
        @Override
        public void handleSelectedData(DataWrapper dataWrapper) {
            currentFormSearchPluginModule.setFieldValue(dataWrapper.getUuid());
            searchPluginListAdapter.notifyDataSetChanged();
        }
    }

    private class DoneButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent data = new Intent();
            data.putParcelableArrayListExtra(FORM_SEARCH_PLUGINS_KEY, formSearchPluginModules);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
