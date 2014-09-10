package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.adapter.FormInstanceAdapter;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.SearchFragment;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.FormInstance;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.search.SearchPluginModule;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class PortalActivity extends Activity implements OnClickListener {

    private static final String SEARCH_FRAGMENT_TAG = "searchFragment";

    private FieldWorker currentFieldWorker;

    private SearchFragment searchFragment;
    private ListView formInstanceView;
    private List<FormInstance> formInstances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.portal_activity);
        setTitle(this.getResources().getString(R.string.field_worker_home_menu_text));

        // who logged in?
        currentFieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);

        // put a sample search fragment in the left column for testing
        // TODO: design this UI
        if (null == savedInstanceState) {
            searchFragment = new SearchFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.portal_left_column, searchFragment, SEARCH_FRAGMENT_TAG)
                    .commit();
        } else {
            searchFragment = (SearchFragment) getFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
        }

        // fill the middle column with a button for each available activity
        LinearLayout activitiesLayout = (LinearLayout) findViewById(R.id.portal_middle_column);
        List<String> activityModuleNames = ProjectActivityBuilder.getActivityModuleNames();
        for (String name : activityModuleNames) {
            RelativeLayout layout = makeTextWithPayload(this,
                    getString(ProjectActivityBuilder.getModuleInfoByName(name).getModuleLabelStringId()),
                    getString(ProjectActivityBuilder.getModuleInfoByName(name).getModuleDescriptionStringId()),
                    name, this, activitiesLayout,
                    ProjectActivityBuilder.getModuleInfoByName(name).getModuleColorId(), null, null);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, 20);
        }

        // fill the right column with a list of recent form instances
        formInstanceView = (ListView) findViewById(R.id.portal_right_column);
        TextView header = (TextView) this.getLayoutInflater().inflate(R.layout.generic_header, null);
        header.setText(R.string.form_instance_list_header);
        header.setVisibility(View.VISIBLE);
        formInstanceView.addHeaderView(header);
        populateFormInstanceListView();
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
        populateSearchFragment();
        populateFormInstanceListView();
    }

    // Allow a few searches for testing
    // TODO: design this UI
    private void populateSearchFragment() {
        List<SearchPluginModule> searchPluginModules = new ArrayList<>();

        SearchPluginModule individualPluginModule = new SearchPluginModule(GatewayRegistry.getIndividualGateway(), R.string.individual_label);
        individualPluginModule.getColumnsAndLabels().put("Age Column", R.string.individual_age_label);
        individualPluginModule.getColumnsAndLabels().put("DIP Column", R.string.individual_dip_label);
        individualPluginModule.getColumnsAndLabels().put("DoB Column", R.string.individual_date_of_birth_label);
        searchPluginModules.add(individualPluginModule);

        SearchPluginModule locationPluginModule = new SearchPluginModule(GatewayRegistry.getLocationGateway(), R.string.location_lbl);
        locationPluginModule.getColumnsAndLabels().put("Locality Column", R.string.locality_label);
        searchPluginModules.add(locationPluginModule);

        SearchPluginModule householdPlugin = new SearchPluginModule(GatewayRegistry.getSocialGroupGateway(), R.string.household_label);
        searchPluginModules.add(householdPlugin);

        searchFragment.setSearchPluginModules(searchPluginModules);
        searchFragment.setTitle(R.string.search_individual_lbl);
    }

    // Display a list of recent form instances not yet sent to the ODK server
    private void populateFormInstanceListView() {
        formInstances = OdkCollectHelper.getAllUnsentFormInstances(getContentResolver());
        if (!formInstances.isEmpty()) {
            FormInstanceAdapter adapter = new FormInstanceAdapter(
                    this, R.id.form_instance_list_item, formInstances.toArray());
            formInstanceView.setAdapter(adapter);
            formInstanceView.setOnItemClickListener(new FormInstanceClickListener());
        }
    }

    // Launch an intent for ODK Collect when user clicks on a form instance.
    private class FormInstanceClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            FormInstance selected = formInstances.get(position - 1);
            Uri uri = Uri.parse(selected.getUriString());

            File selectedFile = new File(selected.getFilePath());
            EncryptionHelper.decryptFile(selectedFile, getApplicationContext());

            Intent intent = new Intent(Intent.ACTION_EDIT, uri);
            startActivityForResult(intent, 0);
        }
    }
}
