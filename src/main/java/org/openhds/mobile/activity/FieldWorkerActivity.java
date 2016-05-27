package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.navigate.FormListFragment;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;

import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;
import static org.openhds.mobile.utilities.OdkCollectHelper.getAllUnsentFormInstances;

public class FieldWorkerActivity extends Activity implements OnClickListener {

    public static final String ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA";

    public static final int MODULE_SPACING = 5;

    private FieldWorker currentFieldWorker;
    private FormListFragment formListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.portal_activity);
        setTitle(this.getResources().getString(R.string.field_worker_home_menu_text));

        // who logged in?
        currentFieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

        // fill the middle column with a button for each available activity
        LinearLayout activitiesLayout = (LinearLayout) findViewById(R.id.portal_middle_column);
        for (NavigatorModule module : NavigatorConfig.getInstance().getModules()) {
            RelativeLayout layout = makeTextWithPayload(this, module.getLaunchLabel(), module.getLaunchDescription(),
                    module.getActivityTitle(), this, activitiesLayout, R.drawable.data_selector, null, null, true);
            layout.setPadding(10, 10, 10, 10);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, MODULE_SPACING);
        }

        formListFragment = (FormListFragment) getFragmentManager().findFragmentById(R.id.portal_form_list);
        formListFragment.setHeaderText(R.string.unsent_forms);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.logout_menu_button:
                intent.setClass(this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, HierarchyNavigatorActivity.class);
        intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, currentFieldWorker);
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
