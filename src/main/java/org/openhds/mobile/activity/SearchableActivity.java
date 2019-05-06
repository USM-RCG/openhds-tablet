package org.openhds.mobile.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.openhds.mobile.R;
import org.openhds.mobile.navconfig.BiokoHierarchy;
import org.openhds.mobile.navconfig.HierarchyPath;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.navconfig.db.DefaultQueryHelper;
import org.openhds.mobile.navconfig.db.QueryHelper;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.search.SearchJob;
import org.openhds.mobile.search.SearchQueue;
import org.openhds.mobile.utilities.ConfigUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Arrays.asList;
import static org.openhds.mobile.activity.FieldWorkerActivity.ACTIVITY_MODULE_EXTRA;
import static org.openhds.mobile.activity.HierarchyNavigatorActivity.HIERARCHY_PATH_KEY;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

public class SearchableActivity extends AppCompatActivity {

    private static final String TAG = SearchableActivity.class.getSimpleName();

    private static final String ACTION_ENTITY_LOOKUP = "com.github.cimsbioko.ENTITY_LOOKUP";

    private static final String ADVANCED_SET_KEY = "advanced_set";
    private static final String ALLOW_TOGGLE_KEY = "allow_toggle";
    private static final String ENABLED_LEVELS_KEY = "levels";

    private static final Pattern ID_PATTERN = Pattern.compile("(?i)m\\d+(s\\d+(e\\d+(p\\d+)?)?)?");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{7,}");

    public static final int ADVANCED_POS = 1;
    public static final int BASIC_POS = 0;

    private SearchQueue searchQueue;
    private Handler handler;

    private View listContainer, progressContainer;
    private ListView listView;
    private EditText basicQuery;
    private EditText advancedQuery;
    private boolean advancedSelected;
    private Button searchButton;
    private ToggleButton hierarchyToggle;
    private ToggleButton locationToggle;
    private ToggleButton individualToggle;

    private Set<String> enabledLevels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        setTitle(R.string.search_label);

        listContainer = findViewById(R.id.list_container);
        basicQuery = listContainer.findViewById(R.id.basic_query_text);
        advancedQuery = listContainer.findViewById(R.id.advanced_query_text);
        searchButton = listContainer.findViewById(R.id.search_button);
        listView = listContainer.findViewById(android.R.id.list);
        progressContainer = findViewById(R.id.progress_container);

        basicQuery.addTextChangedListener(new BasicQueryTranslator());

        final Intent intent = getIntent();

        NavigatorConfig config = NavigatorConfig.getInstance();

        boolean androidSearch = Intent.ACTION_SEARCH.equals(intent.getAction());
        boolean allowToggle = androidSearch || "true".equals(intent.getStringExtra(ALLOW_TOGGLE_KEY));
        String enabledLevelsRaw = intent.getStringExtra(ENABLED_LEVELS_KEY);
        enabledLevels = new HashSet<>();
        if (enabledLevelsRaw == null) {
            enabledLevels.addAll(config.getLevels());
        } else {
            enabledLevels.addAll(asList(enabledLevelsRaw.split("[|]")));
        }

        String moduleName = intent.getStringExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA);
        listView.setOnItemClickListener(new ItemClickListener(listView, moduleName));

        SearchOnEnterKeyHandler searchOnEnterKeyHandler = new SearchOnEnterKeyHandler();
        searchButton.setOnClickListener(new SearchOnClickHandler());
        basicQuery.setOnKeyListener(searchOnEnterKeyHandler);
        advancedQuery.setOnKeyListener(searchOnEnterKeyHandler);

        hierarchyToggle = findViewById(R.id.hierarchy_toggle);
        setToggleImage(hierarchyToggle, R.drawable.sm_hierarchy_logo);
        locationToggle = findViewById(R.id.location_toggle);
        setToggleImage(locationToggle, R.drawable.sm_location_logo);
        individualToggle = findViewById(R.id.individual_toggle);
        setToggleImage(individualToggle, R.drawable.sm_individual_logo);

        EntityToggleHandler toggleHandler = new EntityToggleHandler();
        for (ToggleButton tb : asList(hierarchyToggle, locationToggle, individualToggle)) {
            tb.setOnCheckedChangeListener(toggleHandler);
        }

        Set<String> enabledAdminLevels = new HashSet<>();
        enabledAdminLevels.addAll(enabledLevels);
        enabledAdminLevels.retainAll(config.getAdminLevels());
        boolean adminLevelsEnabled = !enabledAdminLevels.isEmpty();
        hierarchyToggle.setChecked(adminLevelsEnabled);
        hierarchyToggle.setEnabled(allowToggle && adminLevelsEnabled);

        boolean householdLevelEnabled = enabledLevels.contains(BiokoHierarchy.HOUSEHOLD);
        locationToggle.setChecked(householdLevelEnabled);
        locationToggle.setEnabled(allowToggle && householdLevelEnabled);

        boolean individualLevelEnabled = enabledLevels.contains(BiokoHierarchy.INDIVIDUAL);
        individualToggle.setChecked(individualLevelEnabled);
        individualToggle.setEnabled(allowToggle && individualLevelEnabled);

        searchQueue = new SearchQueue();
        handler = new Handler();

        if (savedInstanceState != null) {
            advancedSelected = savedInstanceState.getBoolean(ADVANCED_SET_KEY);
        }

        if (androidSearch) {
            String origSearch = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            basicQuery.setText(origSearch);
            doSearch();
        }
    }

    private void setToggleImage(ToggleButton button, int drawableId) {
        ImageSpan imageSpan = new ImageSpan(this, drawableId);
        SpannableString content = new SpannableString("X");
        content.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        button.setText(content);
        button.setTextOn(content);
        button.setTextOff(content);
    }

    private Set<String> getSelectedLevels() {

        Set<String> result = new HashSet<>();

        if (hierarchyToggle.isChecked()) {
            result.addAll(NavigatorConfig.getInstance().getAdminLevels());
        }
        if (locationToggle.isChecked()) {
            result.add(BiokoHierarchy.HOUSEHOLD);
        }
        if (individualToggle.isChecked()) {
            result.add(BiokoHierarchy.INDIVIDUAL);
        }

        result.retainAll(enabledLevels);

        return result;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ADVANCED_SET_KEY, advancedSelected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search_type);
        Spinner spinner = (Spinner) item.getActionView();
        spinner.setOnItemSelectedListener(new SearchTypeSelectionHandler());
        spinner.setSelection(advancedSelected ? ADVANCED_POS : BASIC_POS);
        return true;
    }

    @Override
    protected void onDestroy() {
        searchQueue.shutdown();
        super.onDestroy();
    }

    private void doSearch() {
        try {
            executeQuery(addLevelClause(parseLuceneQuery(advancedQuery.getText().toString())));
        } catch (QueryNodeException e) {
            Log.e(TAG, "bad query", e);
            listView.setAdapter(new ResultsAdapter(this, new ArrayList<DataWrapper>()));
        }
    }

    private class EntityToggleHandler implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                ensureOneChecked(buttonView);
            }
        }

        private void ensureOneChecked(CompoundButton uncheckedButton) {
            int checkedCount = 0;
            for (ToggleButton tb : asList(hierarchyToggle, locationToggle, individualToggle)) {
                if (tb.isChecked()) {
                    checkedCount++;
                }
            }
            if (checkedCount <= 0) {
                uncheckedButton.setChecked(true);
            }
        }
    }

    private class SearchTypeSelectionHandler implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            advancedSelected = position == ADVANCED_POS;
            updateQueryViews();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

        private void updateQueryViews() {
            basicQuery.setVisibility(advancedSelected ? GONE : VISIBLE);
            advancedQuery.setVisibility(advancedSelected ? VISIBLE : GONE);
        }
    }

    private class SearchOnClickHandler implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            doSearch();
        }
    }

    private class SearchOnEnterKeyHandler implements EditText.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        searchButton.performClick();
                        return true;
                }
            }
            return false;
        }
    }

    private class BasicQueryTranslator implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            advancedQuery.setText(buildLuceneQuery(basicQuery.getText().toString()).toString());
        }
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {

        private ListView listView;
        private String fromModule;

        public ItemClickListener(ListView listView) {
            this.listView = listView;
        }

        public ItemClickListener(ListView listView, String fromModule) {
            this(listView);
            this.fromModule = fromModule;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            DataWrapper selected = (DataWrapper) listView.getItemAtPosition(position);

            switch (getIntent().getAction()) {
                case Intent.ACTION_SEARCH:
                    viewInHierarchy(selected);
                    break;
                case ACTION_ENTITY_LOOKUP:
                    finishWithResult(selected);
                    break;
            }
        }

        private void finishWithResult(DataWrapper selected) {
            if (selected != null) {
                Intent result = new Intent();
                result.putExtra("value", selected.getUuid());
                result.putExtra("name", selected.getName());
                result.putExtra("uuid", selected.getUuid());
                result.putExtra("category", selected.getCategory());
                result.putExtra("extId", selected.getExtId());
                result.putExtra("hierarchyId", selected.getHierarchyId());
                setResult(RESULT_OK, result);
            }
            finish();
        }

        private void viewInHierarchy(DataWrapper clickedItem) {
            Stack<DataWrapper> revPath = new Stack<>();
            revPath.push(clickedItem);
            for (DataWrapper item = getParent(clickedItem); item != null; item = getParent(item)) {
                revPath.push(item);
            }

            if (!revPath.isEmpty()) {
                HierarchyPath path = new HierarchyPath();
                while (!revPath.isEmpty()) {
                    DataWrapper item = revPath.pop();
                    path.down(item.getCategory(), item);
                }

                String moduleToLaunch;
                Collection<NavigatorModule> activeModules = ConfigUtils.getActiveModules(SearchableActivity.this);
                if (fromModule != null) {
                    moduleToLaunch = fromModule;
                } else if (!activeModules.isEmpty()) {
                    NavigatorModule firstModule = activeModules.iterator().next();
                    moduleToLaunch = firstModule.getName();
                } else {
                    showLongToast(SearchableActivity.this, R.string.no_active_modules);
                    return;
                }

                Intent intent = new Intent(SearchableActivity.this, HierarchyNavigatorActivity.class);
                intent.putExtra(ACTIVITY_MODULE_EXTRA, moduleToLaunch);
                intent.putExtra(HIERARCHY_PATH_KEY, path);
                startActivity(intent);
            }
        }

        private DataWrapper getParent(DataWrapper item) {
            return DefaultQueryHelper.getInstance().getParent(getContentResolver(), item.getCategory(), item.getUuid());
        }
    }

    private Query buildLuceneQuery(String query) {
        BooleanQuery result = new BooleanQuery();
        for (String part : query.split("\\s+")) {
            if (ID_PATTERN.matcher(part).matches()) {
                result.add(new WildcardQuery(new Term("extId", part + "*")), BooleanClause.Occur.SHOULD);
            } else if (PHONE_PATTERN.matcher(part).matches()) {
                result.add(new FuzzyQuery(new Term("phone", part), 1), BooleanClause.Occur.SHOULD);
            } else {
                result.add(new FuzzyQuery(new Term("name", part), 1), BooleanClause.Occur.SHOULD);
                result.add(new FuzzyQuery(new Term("extId", part), 1), BooleanClause.Occur.SHOULD);
            }
        }
        return result;
    }

    private Query addLevelClause(Query orig) {
        BooleanQuery result = new BooleanQuery();
        result.add(orig, BooleanClause.Occur.MUST);
        Set<String> levels = getSelectedLevels();
        if (!levels.isEmpty()) {
            BooleanQuery levelQuery = new BooleanQuery();
            for (String level : levels) {
                levelQuery.add(new TermQuery(new Term("level", level)), BooleanClause.Occur.SHOULD);
            }
            result.add(levelQuery, BooleanClause.Occur.MUST);
        }
        return result;
    }

    private Query parseLuceneQuery(String query) throws QueryNodeException {
        StandardQueryParser parser = new StandardQueryParser();
        parser.setAllowLeadingWildcard(true);
        return parser.parse(query, "name");
    }

    private void showLoading(boolean loading) {
        progressContainer.setVisibility(loading ? VISIBLE : GONE);
        listContainer.setVisibility(loading ? GONE : VISIBLE);
    }

    private void executeQuery(Query query) {
        showLoading(true);
        searchQueue.queue(new BoundedSearch(query, 100));
    }

    private void handleSearchResults(List<DataWrapper> results) {
        listView.setAdapter(new ResultsAdapter(this, results));
        showLoading(false);
        (advancedSelected? advancedQuery : basicQuery).requestFocus();
    }

    private class BoundedSearch extends SearchJob {

        private final List<DataWrapper> items = new ArrayList<>();
        private Query query;
        private int limit;

        BoundedSearch(Query query, int limit) {
            super(SearchableActivity.this);
            this.query = query;
            this.limit = limit;
        }

        @Override
        public void performSearch(IndexSearcher searcher) throws IOException {
            Log.i(TAG, "searching: " + query.toString());
            items.clear();
            TopDocs topDocs = searcher.search(query, limit);
            QueryHelper helper = DefaultQueryHelper.getInstance();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String uuid = doc.get("uuid"), level = doc.get("level");
                DataWrapper item = helper.get(getContentResolver(), level, uuid);
                if (item != null) {
                    items.add(item);
                }
            }
        }

        @Override
        protected void postResult() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    handleSearchResults(items);
                }
            });
        }

        @Override
        public void handleException(Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showLoading(false);
                }
            });
            super.handleException(e);
        }
    }

    private static class ResultsAdapter extends ArrayAdapter<DataWrapper> {

        private LayoutInflater inflater;

        public ResultsAdapter(Context context, List<DataWrapper> objects) {
            super(context, -1, objects);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.search_result, null);
            }

            DataWrapper item = getItem(position);

            ImageView icon = convertView.findViewById(R.id.icon);
            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            switch (item.getCategory()) {
                case BiokoHierarchy.HOUSEHOLD:
                    icon.setImageResource(R.drawable.location_logo);
                    break;
                case BiokoHierarchy.INDIVIDUAL:
                    icon.setImageResource(R.drawable.individual_logo);
                    break;
                default:
                    icon.setImageResource(R.drawable.hierarchy_logo);
            }
            text1.setText(item.getName());
            text2.setText(item.getExtId());

            return convertView;
        }
    }
}