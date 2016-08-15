package org.openhds.mobile.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openhds.mobile.navconfig.HierarchyPath;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.navconfig.db.DefaultQueryHelper;
import org.openhds.mobile.navconfig.db.QueryHelper;
import org.openhds.mobile.repository.DataWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static org.apache.lucene.util.ReaderUtil.getMergedFieldInfos;
import static org.openhds.mobile.activity.FieldWorkerActivity.ACTIVITY_MODULE_EXTRA;
import static org.openhds.mobile.activity.HierarchyNavigatorActivity.HIERARCHY_PATH_KEY;
import static org.openhds.mobile.utilities.SyncUtils.close;

public class SearchableActivity extends ListActivity {

    private static final String TAG = SearchableActivity.class.getSimpleName();

    private static final Pattern ID_PATTERN = Pattern.compile("(?i)m\\d+(s\\d+(e\\d+(p\\d+)?)?)?");
    private static final ExecutorService execService = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.list_content);
        final ListView listView = (ListView) findViewById(android.R.id.list);

        final Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
            performSearch(query);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                DataWrapper clickedItem = (DataWrapper)listView.getItemAtPosition(position);

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
                    if (intent.hasExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA)) {
                        moduleToLaunch = intent.getStringExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA);
                    } else {
                        NavigatorModule firstModule = NavigatorConfig.getInstance().getModules().iterator().next();
                        moduleToLaunch = firstModule.getName();
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
        });
    }

    private void performSearch(final String query) {

        setProgressBarIndeterminateVisibility(true);

        Runnable search = new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "received query: " + query);

                File indexFile = new File(getFilesDir(), "search-index");
                final List<DataWrapper> items = new ArrayList<>();
                try {
                    long start = System.currentTimeMillis();
                    Directory indexDir = FSDirectory.open(indexFile);
                    IndexReader indexReader = IndexReader.open(indexDir);
                    IndexSearcher searcher = new IndexSearcher(indexReader);
                    try {
                        String[] split = query.split("\\s+");
                        // construct a query over all known fields
                        BooleanQuery boolQuery = new BooleanQuery();
                        for (FieldInfo fieldInfo : getMergedFieldInfos(indexReader)) {
                            switch (fieldInfo.name) {
                                case "uuid":
                                case "level":
                                    // do not query these fields directly
                                    break;
                                case "extId":
                                    for (String part : split) {
                                        if (ID_PATTERN.matcher(part).matches()) {
                                            boolQuery.add(
                                                    new WildcardQuery(new Term(fieldInfo.name, query + "*")),
                                                    BooleanClause.Occur.SHOULD);
                                        }
                                    }
                                    break;
                                case "name":
                                    for (String part : split) {
                                        if (!ID_PATTERN.matcher(part).matches()) {
                                            boolQuery.add(new FuzzyQuery(new Term(fieldInfo.name, part)), BooleanClause.Occur.SHOULD);
                                        }
                                    }
                                    break;
                                default:
                                    boolQuery.add(new TermQuery(new Term(fieldInfo.name, query)), BooleanClause.Occur.SHOULD);
                            }
                        }

                        // perform the search
                        Log.i(TAG, "searching: " + boolQuery.toString());
                        long searchStart = System.currentTimeMillis();
                        TopDocs topDocs = searcher.search(boolQuery, 100);

                        long finish = System.currentTimeMillis();
                        Log.i(TAG, "found " + topDocs.totalHits + " hits in " + (finish - searchStart) + "ms, "
                                + (finish - start) + " ms total");
                        QueryHelper helper = DefaultQueryHelper.getInstance();
                        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                            Document doc = searcher.doc(scoreDoc.doc);
                            String uuid = doc.get("uuid"), level = doc.get("level");
                            DataWrapper item = helper.get(getContentResolver(), level, uuid);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    } finally {
                        close(searcher, indexReader, indexDir);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "search error", e);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setListAdapter(new ResultAdapter(SearchableActivity.this, items));
                        setProgressBarIndeterminateVisibility(false);
                    }
                });
            }
        };

        execService.submit(search);
    }

}

class ResultAdapter extends ArrayAdapter<DataWrapper> {

    private LayoutInflater inflater;

    public ResultAdapter(Context context, List<DataWrapper> objects) {
        super(context, -1, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.two_line_list_item, null);
        }

        DataWrapper item = getItem(position);

        TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
        TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);

        text1.setText(item.getName());
        text2.setText(item.getExtId());

        return convertView;
    }
}