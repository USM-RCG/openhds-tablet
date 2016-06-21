package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.DataSelectionFragment;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.FormSelectionFragment;
import org.openhds.mobile.fragment.navigate.DetailToggleFragment;
import org.openhds.mobile.fragment.navigate.FormListFragment;
import org.openhds.mobile.fragment.navigate.HierarchyButtonFragment;
import org.openhds.mobile.fragment.navigate.VisitFragment;
import org.openhds.mobile.fragment.navigate.detail.DefaultDetailFragment;
import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.navconfig.HierarchyPath;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.navconfig.db.DefaultQueryHelper;
import org.openhds.mobile.navconfig.db.QueryHelper;
import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.Launcher;
import org.openhds.mobile.navconfig.forms.consumers.ConsumerResult;
import org.openhds.mobile.navconfig.forms.consumers.FormPayloadConsumer;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.search.EntityFieldSearch;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static org.openhds.mobile.model.form.FormInstance.generate;
import static org.openhds.mobile.model.form.FormInstance.getBinding;
import static org.openhds.mobile.model.form.FormInstance.lookup;
import static org.openhds.mobile.utilities.FormUtils.editIntent;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

public class HierarchyNavigatorActivity extends Activity implements LaunchContext,
        HierarchyButtonFragment.HierarchyButtonListener, DetailToggleFragment.DetailToggleListener,
        DataSelectionFragment.DataSelectionListener, FormSelectionFragment.FormSelectionListener,
        VisitFragment.VisitFinishedListener {

    private static final String TAG = HierarchyNavigatorActivity.class.getSimpleName();

    private static final int ODK_ACTIVITY_REQUEST_CODE = 0;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 1;

    private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
    private static final String DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment";

    public static final String HIERARCHY_PATH_KEY = "hierarchyPathKeys";
    private static final String CURRENT_RESULTS_KEY = "currentResults";
    private static final String VISIT_KEY = "visitKey";
    private static final String HISTORY_KEY = "navHistory";

    private static final String ROOT_LEVEL = "root";

    private HierarchyButtonFragment hierarchyButtonFragment;
    private DataSelectionFragment valueFragment;
    private FormSelectionFragment formFragment;
    private DetailToggleFragment detailToggleFragment;
    private DetailFragment defaultDetailFragment;
    private DetailFragment detailFragment;
    private VisitFragment visitFragment;
    private FormListFragment formListFragment;

    private NavigatorConfig config;
    private String currentModuleName;
    private NavigatorModule currentModule;

    private HierarchyPath hierarchyPath;
    private Stack<HierarchyPath> pathHistory;
    private List<DataWrapper> currentResults;

    private Visit currentVisit;
    private ConsumerResult consumerResult;

    private HashMap<MenuItem, String> menuItemTags;
    private QueryHelper queryHelper;


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

        queryHelper = DefaultQueryHelper.getInstance();

        hierarchyPath = new HierarchyPath();

        FragmentManager fragmentManager = getFragmentManager();

        hierarchyButtonFragment = (HierarchyButtonFragment) fragmentManager.findFragmentById(R.id.hierarchy_button_fragment);
        detailToggleFragment = (DetailToggleFragment) fragmentManager.findFragmentById(R.id.detail_toggle_fragment);
        formFragment = (FormSelectionFragment) fragmentManager.findFragmentById(R.id.form_selection_fragment);
        visitFragment = (VisitFragment) fragmentManager.findFragmentById(R.id.visit_fragment);
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
            currentVisit = (Visit) savedInstanceState.getSerializable(VISIT_KEY);
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(HIERARCHY_PATH_KEY, hierarchyPath);
        savedInstanceState.putParcelableArrayList(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);
        savedInstanceState.putSerializable(VISIT_KEY, getCurrentVisit());
        savedInstanceState.putSerializable(HISTORY_KEY, pathHistory);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        update(); // called here since it expects fragments to be created
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);

        // MenuItems do not have their own tags, so I am using a map as a substitute. This map uses the MenuItem itself
        // as a key and the moduleName.
        menuItemTags = new HashMap<>();

        // Configures the menu for switching between inactive modules (ones other than the 'current' one)
        for (NavigatorModule module : NavigatorConfig.getInstance().getModules()) {
            if (!module.getActivityTitle().equals(currentModuleName)) {
                MenuItem menuItem = menu.add(module.getActivityTitle());
                menuItem.setIcon(R.drawable.data_selector);
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menuItemTags.put(menuItem, module.getActivityTitle());
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.logout_menu_button:
                intent.setClass(this, LoginActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case R.id.field_worker_home_menu_button:
                intent.setClass(this, FieldWorkerActivity.class);
                break;
            default:
                String menuModule = menuItemTags.get(item);
                if (menuModule != null) {
                    intent.setClass(this, HierarchyNavigatorActivity.class);
                    intent.putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, menuModule);
                    intent.putExtra(HIERARCHY_PATH_KEY, hierarchyPath);
                    intent.putParcelableArrayListExtra(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
        startActivity(intent);
        return true;
    }

    private void launchForm(Binding binding, Map<String, String> followUpFormHints) {
        if (binding != null) {
            Map<String, String> data = buildDataWithHints(binding, followUpFormHints);
            if (binding.requiresSearch()) {
                launchSearch(binding, data);
            } else {
                launchNewForm(binding, data);
            }
        }
    }

    private Map<String, String> buildDataWithHints(Binding binding, Map<String, String> followUpHints) {
        Map<String, String> formData = binding.getBuilder().buildPayload(this);
        if (followUpHints != null) {
            formData.putAll(followUpHints);
        }
        return formData;
    }

    private void launchSearch(Binding binding, Map<String, String> data) {
        Intent intent = new Intent(this, EntitySearchActivity.class);
        List<EntityFieldSearch> searchModules = binding.getSearches();
        intent.putParcelableArrayListExtra(EntitySearchActivity.SEARCH_MODULES_KEY, new ArrayList<>(searchModules));
        intent.putExtra(EntitySearchActivity.FORM_BINDING_KEY, binding.getName());
        intent.putExtra(EntitySearchActivity.ORIGINAL_DATA_KEY, (Serializable) data);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    private void launchNewForm(Binding binding, Map<String, String> data) {
        try {
            showShortToast(this, R.string.launching_odk_collect);
            startActivityForResult(editIntent(generate(getContentResolver(), binding, data)), ODK_ACTIVITY_REQUEST_CODE);
        } catch (Exception e) {
            showShortToast(this, "failed to launch form: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ODK_ACTIVITY_REQUEST_CODE:
                    handleFormResult(data);
                    update();
                    break;
                case SEARCH_ACTIVITY_REQUEST_CODE:
                    launchNewFormAfterSearch(data);
                    break;
            }
        }
    }

    private void launchNewFormAfterSearch(Intent data) {
        Binding binding = config.getBinding(data.getStringExtra(EntitySearchActivity.FORM_BINDING_KEY));
        if (binding != null) {
            Map<String, String> formData = (Map<String, String>) data.getSerializableExtra(EntitySearchActivity.ORIGINAL_DATA_KEY);
            List<EntityFieldSearch> searchModules = data.getParcelableArrayListExtra(EntitySearchActivity.SEARCH_MODULES_KEY);
            for (EntityFieldSearch plugin : searchModules) {
                formData.put(plugin.getName(), plugin.getValue());
            }
            launchNewForm(binding, formData);
        } else {
            showShortToast(this, "failed to launch form: unknown binding");
        }
    }

    /**
     * Handles forms created with launchNewForm on return from ODK.
     */
    private void handleFormResult(Intent data) {
        FormInstance instance = lookup(getContentResolver(), data.getData());
        String formPath = instance.getFilePath();
        if (formPath != null) {
            DatabaseAdapter.getInstance(this).attachFormToHierarchy(hierarchyPath.toString(), formPath);
        }
        try {
            Map<String, String> formData = instance.load();
            Binding binding = getBinding(formData);
            if (instance.isComplete() && binding != null) {
                FormPayloadConsumer consumer = binding.getConsumer();
                consumerResult = consumer.consumeFormPayload(formData, this);
                if (consumerResult.hasInstanceUpdates()) {
                    consumer.augmentInstancePayload(formData);
                    try {
                        instance.store(formData);
                    } catch (IOException ue) {
                        showShortToast(this, "Update failed: " + ue.getMessage());
                    }
                }
                if (consumerResult.hasFollowUp()) {
                    launchForm(consumerResult.getFollowUp(), consumerResult.getFollowUpHints());
                }
            }
        } catch (IOException e) {
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

    public Visit getCurrentVisit() {
        return currentVisit;
    }

    public ConsumerResult getConsumerResult() {
        return consumerResult;
    }

    public void startVisit(Visit visit) {
        setCurrentVisit(visit);
    }

    public void finishVisit() {
        setCurrentVisit(null);
    }

    private void setCurrentVisit(Visit currentVisit) {
        this.currentVisit = currentVisit;
        update();
    }

    @Override
    public void onFormSelected(Binding binding) {
        launchForm(binding, null);
    }

    @Override
    public void onDataSelected(DataWrapper data) {
        stepDown(data);
    }

    @Override
    public void onVisitFinished() {
        finishVisit();
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
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG).commit();
            getFragmentManager().executePendingTransactions();
        }
        valueFragment.populateData(currentResults);
    }

    private void showDetailFragment() {
        DetailFragment fragment = getDetailForCurrentLevel();
        detailFragment = fragment == null ? defaultDetailFragment : fragment;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.middle_column, detailFragment, DETAIL_FRAGMENT_TAG)
                .commit();
        getFragmentManager().executePendingTransactions();
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
        updateVisit();
        updateForms();
    }

    private void updatePathButtons() {
        hierarchyButtonFragment.update(hierarchyPath);
    }

    private void updateData() {
        int depth = hierarchyPath.depth();
        String level = getLevel();
        if (ROOT_LEVEL.equals(level)) {
            currentResults = queryHelper.getAll(getContentResolver(), config.getTopLevel());
        } else {
            String nextLevel = depth >= config.getLevels().size() ? null : config.getLevels().get(depth);
            currentResults = queryHelper.getChildren(getContentResolver(), getCurrentSelection(), nextLevel);
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

    private void updateVisit() {
        visitFragment.setEnabled(currentVisit != null);
    }

    /**
     * Refreshes the attached forms at the current hierarchy path and prunes sent form associations.
     */
    private void updateForms() {
        List<FormInstance> unsentForms = new ArrayList<>();
        List<String> sentFormPaths = new ArrayList<>();
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
        Collection<String> attachedPaths = dbAdapter.findFormsForHierarchy(hierarchyPath.toString());
        for (FormInstance attachedForm : OdkCollectHelper.getByPaths(getContentResolver(), attachedPaths)) {
            if (attachedForm.isSubmitted()) {
                sentFormPaths.add(attachedForm.getFilePath());
            } else {
                unsentForms.add(attachedForm);
            }
        }
        dbAdapter.detachFromHierarchy(sentFormPaths);
        formListFragment.populate(unsentForms);
    }
}
