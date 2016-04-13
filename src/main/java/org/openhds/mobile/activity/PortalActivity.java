package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.navigate.FormListFragment;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.projectdata.ModuleUiHelper;
import org.openhds.mobile.projectdata.NavigatePluginModule;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;

import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;
import static org.openhds.mobile.utilities.OdkCollectHelper.getAllUnsentFormInstances;

public class PortalActivity extends Activity implements OnClickListener {

    private static final String TAG = PortalActivity.class.getSimpleName();

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
        for (ProjectActivityBuilder.Module module : ProjectActivityBuilder.Module.values()) {
            try {
                NavigatePluginModule instance = module.newInstance();
                ModuleUiHelper moduleInfo = instance.getModuleUiHelper();
                RelativeLayout layout = makeTextWithPayload(this,
                        getString(moduleInfo.getModuleLabelStringId()),
                        getString(moduleInfo.getModuleDescriptionStringId()),
                        module.name(), this, activitiesLayout,
                        moduleInfo.getModulePortalDrawableId(), null, null,true);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                params.setMargins(0, 0, 0, 20);
            } catch (Exception e) {
                Log.e(TAG, "failed to create launcher for module " + module.name(), e);
            }
        }

        formListFragment = (FormListFragment) getFragmentManager().findFragmentById(R.id.portal_form_list);

        TextView header = (TextView) this.getLayoutInflater().inflate(R.layout.generic_header, null);
        header.setText(R.string.form_instance_list_header);
        formListFragment.addHeaderView(header);
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
                intent.setClass(this, OpeningActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NavigateActivity.class);
        intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, currentFieldWorker);
        String activityName = (String) v.getTag();
        intent.putExtra(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA, activityName);
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
