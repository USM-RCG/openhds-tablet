package org.openhds.mobile.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.openhds.mobile.R;
import org.openhds.mobile.navconfig.HierarchyPath;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.navconfig.db.DefaultQueryHelper;
import org.openhds.mobile.navconfig.db.QueryHelper;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.search.SearchJob;
import org.openhds.mobile.search.SearchQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import static org.openhds.mobile.activity.FieldWorkerActivity.ACTIVITY_MODULE_EXTRA;
import static org.openhds.mobile.activity.HierarchyNavigatorActivity.HIERARCHY_PATH_KEY;

public class SearchableActivity extends ListActivity {

    private static final String TAG = SearchableActivity.class.getSimpleName();

    private static final Pattern ID_PATTERN = Pattern.compile("(?i)m\\d+(s\\d+(e\\d+(p\\d+)?)?)?");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{7,}");

    private SearchQueue searchQueue;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.list_content);
        ListView listView = (ListView) findViewById(android.R.id.list);

        searchQueue = new SearchQueue();
        handler = new Handler();

        final Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            performSearch(buildQuery(query));
        }

        String moduleName = intent.getStringExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA);
        listView.setOnItemClickListener(new ItemClickListener(listView, moduleName));
    }

    @Override
    protected void onDestroy() {
        searchQueue.shutdown();
        super.onDestroy();
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

            DataWrapper clickedItem = (DataWrapper) listView.getItemAtPosition(position);

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
                if (fromModule != null) {
                    moduleToLaunch = fromModule;
                } else {
                    NavigatorModule firstModule = NavigatorConfig.getInstance().getModules().iterator().next();
                    moduleToLaunch = firstModule.getName();
                }

                listView.getContext();

                Intent intent = new Intent(listView.getContext(), HierarchyNavigatorActivity.class);
                intent.putExtra(ACTIVITY_MODULE_EXTRA, moduleToLaunch);
                intent.putExtra(HIERARCHY_PATH_KEY, path);
                startActivity(intent);
            }
        }

        private DataWrapper getParent(DataWrapper item) {
            return DefaultQueryHelper.getInstance().getParent(getContentResolver(), item.getCategory(), item.getUuid());
        }
    }

    private BooleanQuery buildQuery(String query) {
        BooleanQuery boolQuery = new BooleanQuery();
        for (String part : query.split("\\s+")) {
            if (ID_PATTERN.matcher(part).matches()) {
                boolQuery.add(new WildcardQuery(new Term("extId", part + "*")), BooleanClause.Occur.SHOULD);
            } else if (PHONE_PATTERN.matcher(part).matches()) {
                boolQuery.add(new FuzzyQuery(new Term("phone", part), .85f), BooleanClause.Occur.SHOULD);
            } else {
                boolQuery.add(new FuzzyQuery(new Term("name", part)), BooleanClause.Occur.SHOULD);
            }
        }
        return boolQuery;
    }

    private void performSearch(Query query) {
        setProgressBarIndeterminateVisibility(true);
        searchQueue.queue(new BoundedSearch(query, 100));
    }

    private void handleSearchResults(List<DataWrapper> results) {
        setListAdapter(new ResultsAdapter(this, results));
        setProgressBarIndeterminateVisibility(false);
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

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
            TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);

            switch (item.getCategory()) {
                case "household":
                    icon.setImageResource(R.drawable.location_logo);
                    break;
                case "individual":
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