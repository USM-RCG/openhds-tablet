package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.search.EntityFieldSearch;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.repository.search.SearchUtils.getFieldWorkerModule;
import static org.openhds.mobile.repository.search.SearchUtils.getIndividualModule;
import static org.openhds.mobile.repository.search.SearchUtils.getLocationModule;
import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithValueAndLabel;
import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.LoginUtils.launchLogin;


public class EntitySearchActivity extends Activity implements DataSelectionFragment.DataSelectionListener {

    private static final String TAG = EntitySearchActivity.class.getSimpleName();
    private static final String ACTION_EXTERNAL = "org.cims_bioko.ENTITY_SEARCH";
    private static final String UUID = "uuid";
    private static final String VALUE = "value";

    enum Entity {
        INDIVIDUAL, FIELD_WORKER, LOCATION, SOCIAL_GROUP
    }

    public static final String SEARCH_MODULES_KEY = "entitySearchModules";
    public static final String FORM_BINDING_KEY = "entitySearchFormBinding";
    public static final String ORIGINAL_DATA_KEY = "entitySearchOriginalData";
    public static final String ENTITY_KEY = "entity";

    private SearchFragment searchFragment;
    private DataSelectionFragment selectionFragment;
    private EntityFieldSearch selectedSearch;
    private SearchListAdapter searchListAdapter;
    private ListView listView;

    private String formBinding;
    private Serializable originalData;
    private ArrayList<EntityFieldSearch> searches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.form_search_activity);
        setTitle(R.string.search_database_label);

        searchFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
        selectionFragment = (DataSelectionFragment) getFragmentManager().findFragmentById(R.id.search_selection_fragment);
        searches = new ArrayList<>();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (isSearchExternal()) {
                EntityFieldSearch search = loadExternalSearch(intent.getExtras());
                if (search != null) {
                    searches.add(search);
                }
            } else {
                searches = intent.getParcelableArrayListExtra(SEARCH_MODULES_KEY);
                formBinding = intent.getStringExtra(FORM_BINDING_KEY);
                originalData = intent.getSerializableExtra(ORIGINAL_DATA_KEY);
            }
        } else {
            searches = savedInstanceState.getParcelableArrayList(SEARCH_MODULES_KEY);
            formBinding = savedInstanceState.getString(FORM_BINDING_KEY);
            originalData = savedInstanceState.getSerializable(ORIGINAL_DATA_KEY);
        }

        searchFragment.setResultsHandler(new SearchResultsHandler());

        Button doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(new DoneButtonListener());

        listView = (ListView) findViewById(R.id.form_search_list_view);
        listView.setOnItemClickListener(new SearchClickListener());

        searchListAdapter = new SearchListAdapter(this, 0, searches);
        listView.setAdapter(searchListAdapter);

        if (!searches.isEmpty()) {
            setupSearch(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getLogin(FieldWorker.class).hasAuthenticatedUser()) {
            launchLogin(this, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(SEARCH_MODULES_KEY, searches);
        savedInstanceState.putString(FORM_BINDING_KEY, formBinding);
        savedInstanceState.putSerializable(ORIGINAL_DATA_KEY, originalData);
        super.onSaveInstanceState(savedInstanceState);
    }

    private boolean isSearchExternal() {
        return ACTION_EXTERNAL.equals(getIntent().getAction());
    }

    private EntityFieldSearch loadExternalSearch(Bundle extras) {
        String entityStr = extras.getString(ENTITY_KEY);
        try {
            Entity entity = Entity.valueOf(entityStr);
            switch (entity) {
                case INDIVIDUAL:
                    return getIndividualModule(UUID);
                case FIELD_WORKER:
                    return getFieldWorkerModule(UUID);
                case LOCATION:
                    return getLocationModule(UUID);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "invalid search entity " + entityStr);
        }
        return null;
    }

    private void setupSearch(int position) {
        selectedSearch = searchListAdapter.getItem(position);
        searchFragment.configure(selectedSearch);
        selectionFragment.clearData();
    }

    private void setSearchResults(List<DataWrapper> dataWrappers) {
        selectionFragment.populateData(dataWrappers);
    }

    private void selectSearchResult(DataWrapper data) {
        selectedSearch.setValue(data.getUuid());
        searchListAdapter.notifyDataSetChanged();
    }

    private class SearchListAdapter extends ArrayAdapter<EntityFieldSearch> {

        private static final int LABEL_COLOR = R.color.Gray;
        private static final int VALUE_COLOR = R.color.DarkGray;
        private static final int MISSING_COLOR = R.color.Red;

        public SearchListAdapter(Context context, int resource, List<EntityFieldSearch> objects) {
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

    @Override
    public void onDataSelected(DataWrapper data) {
        selectSearchResult(data);
    }

    private class SearchClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setupSearch(position);
        }
    }

    private class SearchResultsHandler implements SearchFragment.ResultsHandler {
        @Override
        public void handleSearchResults(List<DataWrapper> dataWrappers) {
            setSearchResults(dataWrappers);
        }
    }

    private class DoneButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (isSearchExternal() && searches.size() > 0) {
                setExternalResult();
            } else {
                setInternalResult();
            }
            finish();
        }
    }

    /**
     * Sets this {@link Activity}'s result. This conforms to the original search protocol where search results are
     * injected into forms by this application. The {@link Intent} contains 'extra' fields necessary for communicating
     * what may be multiple simultaneous results.
     */
    private void setInternalResult() {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(SEARCH_MODULES_KEY, searches);
        data.putExtra(FORM_BINDING_KEY, formBinding);
        data.putExtra(ORIGINAL_DATA_KEY, originalData);
        setResult(RESULT_OK, data);
    }

    /**
     * Sets this {@link Activity}'s result with an {@link Intent} containing 'extra' fields from the selected entity. If
     * no result was selected, no result will be set and the activity result will appear as a cancel. The result is
     * meant to conform to ODK Collect's external app protocol.
     */
    private void setExternalResult() {
        EntityFieldSearch searchResult = searches.get(0);
        String entityUuid = searchResult.getValue();
        if (entityUuid != null) {
            Intent data = new Intent();
            data.putExtra(UUID, entityUuid);
            data.putExtra(VALUE, entityUuid);
            Gateway gw = searchResult.getGateway();
            Object entity = gw.getFirst(getContentResolver(), gw.findById(entityUuid));
            if (entity != null) {
                for (Field f : entity.getClass().getDeclaredFields()) {
                    String fieldName = f.getName(); // will obfuscators like proguard break this?
                    if (!(UUID.equals(fieldName) || VALUE.equals(fieldName))) {
                        try {
                            f.setAccessible(true);
                            Object fieldValue = f.get(entity);
                            if (fieldValue instanceof Serializable) {
                                data.putExtra(fieldName, (Serializable)fieldValue);
                            }
                        } catch (IllegalAccessException e) {
                            Log.w(TAG, "failed setting " + fieldName);
                        }
                    }
                }
            }
            setResult(RESULT_OK, data);
        }
    }
}
