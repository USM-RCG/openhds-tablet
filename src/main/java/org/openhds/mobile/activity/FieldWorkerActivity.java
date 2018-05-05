package org.openhds.mobile.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.navigate.FormListFragment;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.utilities.ConfigUtils;

import static org.openhds.mobile.search.Utils.isSearchEnabled;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.OdkCollectHelper.getAllUnsentFormInstances;

public class FieldWorkerActivity extends AppCompatActivity implements OnClickListener {

    public static final String ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA";

    private FormListFragment formListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.fieldworker_activity);
        setTitle(getText(R.string.field_worker_home));

        // fill the middle column with a button for each available activity
        LinearLayout activitiesLayout = findViewById(R.id.portal_middle_column);
        for (NavigatorModule module : ConfigUtils.getActiveModules(this)) {
            RelativeLayout layout = makeTextWithPayload(this, module.getLaunchLabel(), module.getLaunchDescription(),
                    module.getName(), this, activitiesLayout, R.drawable.data_selector, null, null, true);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.module_button_spacing));
        }

        formListFragment = (FormListFragment) getSupportFragmentManager().findFragmentById(R.id.portal_form_list);
        formListFragment.setHeaderText(R.string.unsent_forms);
        formListFragment.setFindEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fieldworker_menu, menu);
        menu.findItem(R.id.field_worker_home_menu_button).setVisible(false);

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
        formListFragment.populate(getAllUnsentFormInstances(getContentResolver()));
    }
}
