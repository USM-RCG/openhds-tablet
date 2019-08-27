package org.cimsbioko.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.cimsbioko.R;
import org.cimsbioko.fragment.navigate.FormListFragment;
import org.cimsbioko.model.core.FieldWorker;
import org.cimsbioko.navconfig.NavigatorModule;
import org.cimsbioko.utilities.ConfigUtils;

import java.util.Iterator;

import static org.cimsbioko.search.Utils.isSearchEnabled;
import static org.cimsbioko.utilities.LayoutUtils.makeTextWithPayload;
import static org.cimsbioko.utilities.LoginUtils.getLogin;
import static org.cimsbioko.utilities.FormsHelper.getAllUnsentFormInstances;

public class FieldWorkerActivity extends AppCompatActivity implements OnClickListener {

    public static final String ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA";

    private FormListFragment formListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fieldworker_activity);
        setTitle(getText(R.string.field_worker_home));

        Toolbar toolbar = findViewById(R.id.fieldworker_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        LinearLayout activitiesLayout = findViewById(R.id.portal_middle_column);
        Iterator<NavigatorModule> moduleIter = ConfigUtils.getActiveModules(this).iterator();
        while (moduleIter.hasNext()) {
            NavigatorModule module = moduleIter.next();
            RelativeLayout layout = makeTextWithPayload(this, module.getLaunchLabel(), module.getLaunchDescription(),
                    module.getName(), this, activitiesLayout, R.drawable.data_selector, null, null, true);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            if (moduleIter.hasNext()) {
                params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.module_button_spacing));
            }
        }

        formListFragment = (FormListFragment) getSupportFragmentManager().findFragmentById(R.id.portal_form_list);
        formListFragment.setHeaderText(R.string.unsent_forms);
        formListFragment.setFindEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fieldworker_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.field_worker_search);
        boolean searchEnabled = isSearchEnabled(this);
        if (searchEnabled) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.field_worker_search));
            SearchableInfo searchInfo = searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class));
            searchView.setSearchableInfo(searchInfo);
        }
        searchMenuItem.setVisible(searchEnabled);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_button:
                getLogin(FieldWorker.class).logout(this, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, HierarchyNavigatorActivity.class);
        String activityName = (String) v.getTag();
        intent.putExtra(ACTIVITY_MODULE_EXTRA, activityName);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFormInstanceListView();
    }

    private void populateFormInstanceListView() {
        formListFragment.populate(getAllUnsentFormInstances());
    }
}
