package org.cimsbioko.fragment.navigate;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.cimsbioko.R;
import org.cimsbioko.activity.HierarchyNavigatorActivity;
import org.cimsbioko.navconfig.HierarchyPath;
import org.cimsbioko.navconfig.NavigatorModule;
import org.cimsbioko.provider.DatabaseAdapter;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.utilities.ConfigUtils;
import org.cimsbioko.utilities.MessageUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.cimsbioko.activity.FieldWorkerActivity.ACTIVITY_MODULE_EXTRA;
import static org.cimsbioko.activity.HierarchyNavigatorActivity.HIERARCHY_PATH_KEY;
import static org.cimsbioko.utilities.LayoutUtils.configureTextWithPayload;
import static org.cimsbioko.utilities.LayoutUtils.makeTextWithPayload;


public class FavoritesFragment extends Fragment {

    private ListView list;
    private View progressLayout;
    private View listLayout;
    private FavoriteLoader loader;
    private FavoriteAdapter dataAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.favorites_fragment, container, false);
        list = inflated.findViewById(R.id.favorites_list);
        dataAdapter = new FavoriteAdapter(getActivity());
        list.setAdapter(dataAdapter);
        list.setOnItemClickListener(new ClickListener());
        registerForContextMenu(list);
        progressLayout = inflated.findViewById(R.id.progress_container);
        listLayout = inflated.findViewById(R.id.list_container);
        return inflated;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelLoad();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Activity activity = getActivity();
        if (v.getId() == R.id.favorites_list) {
            activity.getMenuInflater().inflate(R.menu.favorite_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DataWrapper selected;
        switch (item.getItemId()) {
            case R.id.find_favorite:
                selected = getItem(info.position);
                if (selected != null) {
                    selectFavorite(selected);
                }
                return true;
            case R.id.forget_favorite:
                selected = getItem(info.position);
                if (selected != null) {
                    Context ctx = getActivity();
                    DatabaseAdapter.getInstance(ctx).removeFavorite(selected);
                    MessageUtils.showShortToast(ctx, R.string.removed_favorite);
                    loadData();
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private DataWrapper getItem(int position) {
        return (DataWrapper) list.getItemAtPosition(position);
    }

    private void selectFavorite(DataWrapper selected) {
        Context ctx = getActivity();
        HierarchyPath path = HierarchyPath.fromString(ctx.getContentResolver(), selected.getHierarchyId());
        if (path != null) {
            Collection<NavigatorModule> activeModules = ConfigUtils.getActiveModules(ctx);
            if (!activeModules.isEmpty()) {
                NavigatorModule firstModule = activeModules.iterator().next();
                Intent intent = new Intent(ctx, HierarchyNavigatorActivity.class);
                intent.putExtra(ACTIVITY_MODULE_EXTRA, firstModule.getName());
                intent.putExtra(HIERARCHY_PATH_KEY, path);
                startActivity(intent);
            } else {
                MessageUtils.showShortToast(ctx, R.string.no_active_modules);
            }
        }
    }

    private void showLoading(boolean loading) {
        progressLayout.setVisibility(loading? VISIBLE : GONE);
        listLayout.setVisibility(loading? GONE : VISIBLE);
    }

    private void loadData() {
        loader = new FavoriteLoader();
        loader.execute();
    }

    private void cancelLoad() {
        loader.cancel(true);
    }

    private class ClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DataWrapper selected = getItem(position);
            if (selected != null) {
                selectFavorite(selected);
            }
        }
    }

    private class FavoriteAdapter extends ArrayAdapter<DataWrapper> {

        FavoriteAdapter(Context context) {
            super(context, R.layout.generic_list_item_white_text);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DataWrapper dataWrapper = getItem(position);
            if (convertView == null) {
                convertView = makeTextWithPayload(getActivity(), null, null, null,
                        null, null, R.drawable.data_selector, null, null, false);
            }

            if (dataWrapper != null) {
                configureTextWithPayload(getActivity(), (RelativeLayout) convertView, dataWrapper.getName(), dataWrapper.getExtId(),
                        dataWrapper.getStringsPayload(), dataWrapper.getStringIdsPayload(), false);
            }

            return convertView;
        }
    }

    private class FavoriteLoader extends AsyncTask<Void, Void, List<DataWrapper>> {

        @Override
        protected void onPreExecute() {
            showLoading(true);
            dataAdapter.clear();
        }

        @Override
        protected void onCancelled() {
            showLoading(false);
            super.onCancelled();
        }

        @Override
        protected List<DataWrapper> doInBackground(Void... params) {
            Context ctx = getActivity();
            ContentResolver resolver = ctx.getContentResolver();
            DatabaseAdapter db = DatabaseAdapter.getInstance(ctx);
            List<String> favoriteIds = db.getFavoriteIds();
            List<DataWrapper> hydratedFavorites = new ArrayList<>(favoriteIds.size());
            for (int i = 0; !isCancelled() && i < favoriteIds.size(); i++) {
                String itemId = favoriteIds.get(i);
                DataWrapper item = DataWrapper.getByHierarchyId(resolver, itemId);
                if (item != null) {
                    hydratedFavorites.add(item);
                } else {
                    db.removeFavorite(itemId);
                }
            }
            return hydratedFavorites;
        }

        @Override
        protected void onPostExecute(List<DataWrapper> dataWrappers) {
            dataAdapter.addAll(dataWrappers);
            list.setAdapter(dataAdapter);
            showLoading(false);
        }
    }
}
