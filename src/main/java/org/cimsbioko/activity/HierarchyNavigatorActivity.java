package org.cimsbioko.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.cimsbioko.R;
import org.cimsbioko.fragment.DataSelectionFragment;
import org.cimsbioko.fragment.FormSelectionFragment;
import org.cimsbioko.fragment.navigate.DetailToggleFragment;
import org.cimsbioko.fragment.navigate.FormListFragment;
import org.cimsbioko.fragment.navigate.HierarchyButtonFragment;
import org.cimsbioko.fragment.navigate.detail.DefaultDetailFragment;
import org.cimsbioko.fragment.navigate.detail.DetailFragment;
import org.cimsbioko.model.core.FieldWorker;
import org.cimsbioko.model.form.Form;
import org.cimsbioko.model.form.FormInstance;
import org.cimsbioko.navconfig.HierarchyPath;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.navconfig.NavigatorModule;
import org.cimsbioko.navconfig.db.DefaultQueryHelper;
import org.cimsbioko.navconfig.db.QueryHelper;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.Launcher;
import org.cimsbioko.provider.DatabaseAdapter;
import org.cimsbioko.data.DataWrapper;
import org.cimsbioko.utilities.ConfigUtils;
import org.cimsbioko.utilities.FormsHelper;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.cimsbioko.model.form.FormInstance.*;
import static org.cimsbioko.search.Utils.isSearchEnabled;
import static org.cimsbioko.utilities.FormUtils.editIntent;
import static org.cimsbioko.utilities.LoginUtils.getLogin;
import static org.cimsbioko.utilities.MessageUtils.showShortToast;

public class HierarchyNavigatorActivity extends AppCompatActivity implements LaunchContext,
        HierarchyButtonFragment.HierarchyButtonListener, DetailToggleFragment.DetailToggleListener,
        DataSelectionFragment.DataSelectionListener, FormSelectionFragment.FormSelectionListener {

    private static final String TAG = HierarchyNavigatorActivity.class.getSimpleName();

    private static final int FORM_ACTIVITY_REQUEST_CODE = 0;

    private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
    private static final String DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment";

    public static final String HIERARCHY_PATH_KEY = "hierarchyPathKeys";
    private static final String CURRENT_RESULTS_KEY = "currentResults";
    private static final String HISTORY_KEY = "navHistory";

    private static final String ROOT_LEVEL = "root";

    private HierarchyButtonFragment hierarchyButtonFragment;
    private DataSelectionFragment valueFragment;
    private FormSelectionFragment formFragment;
    private DetailToggleFragment detailToggleFragment;
    private DetailFragment defaultDetailFragment;
    private DetailFragment detailFragment;
    private FormListFragment formListFragment;

    private NavigatorConfig config;
    private String currentModuleName;
    private NavigatorModule currentModule;

    private HierarchyPath hierarchyPath;
    private Stack<HierarchyPath> pathHistory;
    private List<DataWrapper> currentResults;

    private HashMap<MenuItem, String> menuItemTags;
    private QueryHelper queryHelper;

    private boolean updateAfterResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigate_activity);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        config = NavigatorConfig.getInstance();
        currentModuleName = (String) extras.get(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA);
        currentModule = config.getModule(currentModuleName);

        setTitle(currentModule.getActivityTitle());

        Toolbar toolbar = findViewById(R.id.navigate_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        queryHelper = DefaultQueryHelper.getInstance();

        hierarchyPath = new HierarchyPath();

        FragmentManager fragmentManager = getSupportFragmentManager();

        hierarchyButtonFragment = (HierarchyButtonFragment) fragmentManager.findFragmentById(R.id.hierarchy_button_fragment);
        detailToggleFragment = (DetailToggleFragment) fragmentManager.findFragmentById(R.id.detail_toggle_fragment);
        formFragment = (FormSelectionFragment) fragmentManager.findFragmentById(R.id.form_selection_fragment);
        formListFragment = (FormListFragment) fragmentManager.findFragmentById(R.id.form_list_fragment);
        defaultDetailFragment = new DefaultDetailFragment();
        valueFragment = new DataSelectionFragment();

        if (savedInstanceState == null) {
            pathHistory = new Stack<>();
            HierarchyPath suppliedPath = intent.getParcelableExtra(HIERARCHY_PATH_KEY);
            if (suppliedPath != null) {
                hierarchyPath = suppliedPath;
                currentResults = getIntent().getParcelableArrayListExtra(CURRENT_RESULTS_KEY);
            }
            fragmentManager.beginTransaction()
                    .add(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG)
                    .commit();
        } else {
            hierarchyPath = savedInstanceState.getParcelable(HIERARCHY_PATH_KEY);
            currentResults = savedInstanceState.getParcelableArrayList(CURRENT_RESULTS_KEY);
            pathHistory = (Stack<HierarchyPath>) savedInstanceState.getSerializable(HISTORY_KEY);

            DataSelectionFragment existingValueFragment = (DataSelectionFragment) fragmentManager.findFragmentByTag(VALUE_FRAGMENT_TAG);
            if (existingValueFragment != null) {
                valueFragment = existingValueFragment;
            }
            DetailFragment existingDetailFragment = (DetailFragment) fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (existingDetailFragment != null) {
                detailFragment = existingDetailFragment;
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (updateAfterResult) {
            update();
            updateAfterResult = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(HIERARCHY_PATH_KEY, hierarchyPath);
        savedInstanceState.putParcelableArrayList(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);
        savedInstanceState.putSerializable(HISTORY_KEY, pathHistory);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        update(); // called here since it expects fragments to be created
    }

    /*
     * A hack to inject extra context when starting the search activity, onSearchRequested was not being called.
     */
    @Override
    public void startActivity(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            intent.putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, currentModuleName);
        }
        super.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fieldworker_menu, menu);

        // MenuItems do not have their own tags, so I am using a map as a substitute. This map uses the MenuItem itself
        // as a key and the moduleName.
        menuItemTags = new HashMap<>();

        // Configures the menu for switching between inactive modules (ones other than the 'current' one)
        for (NavigatorModule module : ConfigUtils.getActiveModules(this)) {
            String moduleName = module.getName();
            if (!moduleName.equals(currentModuleName)) {
                MenuItem menuItem = menu.add(module.getActivityTitle());
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menuItemTags.put(menuItem, module.getName());
            }
        }

        MenuItem searchMenuItem = menu.findItem(R.id.field_worker_search);
        boolean searchEnabled = isSearchEnabled(this);
        if (searchEnabled) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchableInfo searchInfo = searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class));
            SearchView searchView = (SearchView) searchMenuItem.getActionView();
            searchView.setSearchableInfo(searchInfo);
        }
        searchMenuItem.setVisible(searchEnabled);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        if (item.getItemId() == R.id.logout_menu_button) {
            getLogin(FieldWorker.class).logout(this, true);
        } else {
            String menuModule = menuItemTags.get(item);
            if (menuModule != null) {
                intent.setClass(this, HierarchyNavigatorActivity.class);
                intent.putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, menuModule);
                intent.putExtra(HIERARCHY_PATH_KEY, hierarchyPath);
                intent.putParcelableArrayListExtra(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);
                startActivity(intent);
            } else {
                return super.onOptionsItemSelected(item);
            }
        }

        return true;
    }

    private void launchNewForm(Binding binding) {
        if (binding != null) {
            try {
                showShortToast(this, R.string.launching_form);
                Form form = Form.lookup(binding);
                Uri instanceUri = generate(form, binding, this);
                Intent intent = editIntent(form.getUri());
                ClipData clipData = new ClipData("generated form instance", new String[] {"application/xml"}, new ClipData.Item(instanceUri));
                intent.setClipData(clipData);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, FORM_ACTIVITY_REQUEST_CODE);
            } catch (Exception e) {
                showShortToast(this, "failed to launch form: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == FORM_ACTIVITY_REQUEST_CODE) {
                handleFormResult(data);
                updateAfterResult = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Handles forms created with launchNewForm on return from the forms app.
     */
    private void handleFormResult(Intent data) {
        FormInstance instance = lookup(data.getData());
        Long formId = instance.getId();
        if (formId != null) {
            DatabaseAdapter db = DatabaseAdapter.getInstance();
            db.attachFormToHierarchy(hierarchyPath.toString(), formId);
        }
        try {
            Document dataDoc = instance.load();
            Binding binding = getBinding(dataDoc);
            if (instance.isComplete() && binding != null) {
                boolean formModified = binding.getConsumer().consume(dataDoc, this);
                if (formModified) {
                    try {
                        instance.store(dataDoc);
                    } catch (IOException ue) {
                        showShortToast(this, "Update failed: " + ue.getMessage());
                    }
                }
            }
        } catch (IOException | JDOMException e) {
            showShortToast(this, "Read failed: " + e.getMessage());
        }
    }

    public HierarchyPath getHierarchyPath() {
        return hierarchyPath;
    }

    public DataWrapper getCurrentSelection() {
        return hierarchyPath.get(getLevel());
    }

    public FieldWorker getCurrentFieldWorker() {
        return getLogin(FieldWorker.class).getAuthenticatedUser();
    }

    @Override
    public void onFormSelected(Binding binding) {
        launchNewForm(binding);
    }

    @Override
    public void onDataSelected(DataWrapper data) {
        stepDown(data);
    }

    @Override
    public void onDetailToggled() {
        if (valueFragment.isAdded()) {
            showDetailFragment();
            detailToggleFragment.setHighlighted(true);
        } else if (detailFragment.isAdded()) {
            showValueFragment();
            detailToggleFragment.setHighlighted(false);
        }
    }

    private void showValueFragment() {
        // there is only 1 value fragment that can be added
        if (!valueFragment.isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG).commit();
            getSupportFragmentManager().executePendingTransactions();
        }
        valueFragment.populateData(currentResults);
    }

    private void showDetailFragment() {
        DetailFragment fragment = getDetailForCurrentLevel();
        detailFragment = fragment == null ? defaultDetailFragment : fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.middle_column, detailFragment, DETAIL_FRAGMENT_TAG)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
        detailFragment.setUpDetails(getCurrentSelection());
    }

    private DetailFragment getDetailForCurrentLevel() {
        return currentModule.getDetailFragment(getLevel());
    }

    @Override
    public void onHierarchyButtonClicked(String level) {
        jumpUp(level);
    }

    private void jumpUp(String level) {
        boolean isRootLevel = ROOT_LEVEL.equals(level);
        if (!(isRootLevel || hierarchyPath.getLevels().contains(level))) {
            throw new IllegalStateException("invalid level: " + level);
        }
        pushHistory();
        if (isRootLevel) {
            hierarchyPath.clear();
        } else {
            hierarchyPath.truncate(level);
        }
        update();
    }

    private void stepDown(DataWrapper selected) {
        pushHistory();
        hierarchyPath.down(selected.getCategory(), selected);
        update();
    }

    private void pushHistory() {
        try {
            pathHistory.push((HierarchyPath) hierarchyPath.clone());
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "failed to push path history", e);
        }
    }

    @Override
    public void onBackPressed() {
        if (!pathHistory.empty()) {
            hierarchyPath = pathHistory.pop();
            update();
        } else {
            super.onBackPressed();
        }
    }

    private String getLevel() {
        if (hierarchyPath.depth() <= 0) {
            return ROOT_LEVEL;
        } else {
            return config.getLevels().get(hierarchyPath.depth() - 1);
        }
    }

    private void update() {
        String level = getLevel();
        if (!(ROOT_LEVEL.equals(level) || config.getLevels().contains(level))) {
            throw new IllegalStateException("no such level: " + level);
        }
        updatePathButtons();
        updateData();
        updateMiddle();
        updateDetailToggle();
        updateFormLaunchers();
        updateForms();
    }

    private void updatePathButtons() {
        hierarchyButtonFragment.update(hierarchyPath);
    }

    private void updateData() {
        int depth = hierarchyPath.depth();
        String level = getLevel();
        if (ROOT_LEVEL.equals(level)) {
            currentResults = queryHelper.getAll(config.getTopLevel());
        } else {
            String nextLevel = depth >= config.getLevels().size() ? null : config.getLevels().get(depth);
            currentResults = queryHelper.getChildren(getCurrentSelection(), nextLevel);
        }
    }

    private void updateMiddle() {
        if (shouldShowDetail()) {
            showDetailFragment();
        } else {
            showValueFragment();
        }
    }

    private void updateDetailToggle() {
        if (getDetailForCurrentLevel() != null && !shouldShowDetail()) {
            detailToggleFragment.setEnabled(true);
            if (!valueFragment.isAdded()) {
                detailToggleFragment.setHighlighted(true);
            }
        } else {
            detailToggleFragment.setEnabled(false);
        }
    }

    private boolean shouldShowDetail() {
        return currentResults == null || currentResults.isEmpty();
    }

    private void updateFormLaunchers() {
        List<Launcher> relevantLaunchers = new ArrayList<>();
        for (Launcher launcher : currentModule.getLaunchers(getLevel())) {
            if (launcher.relevantFor(HierarchyNavigatorActivity.this)) {
                relevantLaunchers.add(launcher);
            }
        }
        formFragment.createFormButtons(relevantLaunchers);
    }

    /**
     * Refreshes the attached forms at the current hierarchy path and prunes sent form associations.
     */
    private void updateForms() {
        List<FormInstance> unsentForms = new ArrayList<>();
        List<Long> sentIds = new ArrayList<>();
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance();
        Collection<Long> attachedFormIds = dbAdapter.findFormsForHierarchy(hierarchyPath.toString());
        for (FormInstance attachedForm : FormsHelper.getByIds(attachedFormIds)) {
            if (attachedForm.isSubmitted()) {
                sentIds.add(attachedForm.getId());
            } else {
                unsentForms.add(attachedForm);
            }
        }
        dbAdapter.detachFromHierarchy(sentIds);
        formListFragment.populate(unsentForms);
    }
}
