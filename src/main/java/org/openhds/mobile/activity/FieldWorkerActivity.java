package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.navigate.FormListFragment;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.utilities.ConfigUtils;

import java.util.Set;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceBool;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.OdkCollectHelper.getAllUnsentFormInstances;

public class FieldWorkerActivity extends Activity implements OnClickListener {

    public static final String ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA";

    public static final int MODULE_SPACING = 5;

    private FormListFragment formListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.portal_activity);
        setTitle(getText(R.string.field_worker_home));

        // fill the middle column with a button for each available activity
        LinearLayout activitiesLayout = (LinearLayout) findViewById(R.id.portal_middle_column);
        NavigatorConfig config = NavigatorConfig.getInstance();
        Set<String> activeModuleNames = ConfigUtils.getMultiSelectPreference(
                this, getString(R.string.active_modules_key), config.getModuleNames());
        for (NavigatorModule module : config.getModules()) {
            if (activeModuleNames.contains(module.getName())) {
                RelativeLayout layout = makeTextWithPayload(this, module.getLaunchLabel(), module.getLaunchDescription(),
                        module.getName(), this, activitiesLayout, R.drawable.data_selector, null, null, true);
                layout.setPadding(10, 10, 10, 10);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                params.setMargins(0, 0, 0, MODULE_SPACING);
            }
        }

        formListFragment = (FormListFragment) getFragmentManager().findFragmentById(R.id.portal_form_list);
        formListFragment.setHeaderText(R.string.unsent_forms);
        formListFragment.setFindEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fieldworker_menu, menu);
        menu.findItem(R.id.field_worker_home_menu_button).setVisible(false);

        MenuItem searchMenuItem = menu.findItem(R.id.field_worker_search);
        boolean searchEnabled = getPreferenceBool(this, getString(R.string.use_search_key), true);
        if (searchEnabled) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.field_worker_search).getActionView();
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
