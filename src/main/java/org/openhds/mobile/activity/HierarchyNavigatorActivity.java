package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
import org.openhds.mobile.model.form.FormBehavior;
import org.openhds.mobile.model.form.FormHelper;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.NavigatorModule;
import org.openhds.mobile.navconfig.db.DefaultQueryHelper;
import org.openhds.mobile.navconfig.db.QueryHelper;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.consumers.ConsumerResult;
import org.openhds.mobile.navconfig.forms.consumers.FormPayloadConsumer;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.search.EntityFieldSearch;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.FormUtils.editIntent;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

public class HierarchyNavigatorActivity extends Activity implements HierarchyNavigator, LaunchContext {

    private static final String TAG = HierarchyNavigatorActivity.class.getSimpleName();

    private static final int ODK_ACTIVITY_REQUEST_CODE = 0;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 1;

    private HierarchyButtonFragment hierarchyButtonFragment;
    private DataSelectionFragment valueFragment;
    private FormSelectionFragment formFragment;
    private DetailToggleFragment detailToggleFragment;
    private DetailFragment defaultDetailFragment;
    private DetailFragment detailFragment;
    private VisitFragment visitFragment;
    private FormListFragment formListFragment;

    private static final String HIERARCHY_BUTTON_FRAGMENT_TAG = "hierarchyButtonFragment";
    private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
    private static final String FORM_FRAGMENT_TAG = "hierarchyFormFragment";
    private static final String TOGGLE_FRAGMENT_TAG = "hierarchyToggleFragment";
    private static final String DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment";
    private static final String VISIT_FRAGMENT_TAG = "hierarchyVisitFragment";
    private static final String VIEW_PATH_FORM_FRAGMENT_TAG ="formListFragment";
    private static final String VISIT_KEY = "visitKey";
    private static final String HIERARCHY_PATH_KEYS = "hierarchyPathKeys";
    private static final String HIERARCHY_PATH_VALUES = "hierarchyPathValues";
    private static final String CURRENT_RESULTS_KEY = "currentResults";

    private NavigatorModule currentModule;

    private FormHelper formHelper;
    private NavigationManager levelManager;
    private Map<String, DataWrapper> hierarchyPath;
    private List<DataWrapper> currentResults;
    private DataWrapper currentSelection;
    private FieldWorker currentFieldWorker;
    private Visit currentVisit;
    private HashMap<MenuItem, String> menuItemTags;
    private String currentModuleName;
    private ConsumerResult consumerResult;

    private QueryHelper queryHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigate_activity);

        queryHelper = DefaultQueryHelper.getInstance();

        FieldWorker fieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
        setCurrentFieldWorker(fieldWorker);
        currentModuleName = (String) getIntent().getExtras().get(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA);

        try {

            currentModule = NavigatorConfig.getInstance().getModule(currentModuleName);
            hierarchyPath = new HashMap<>();
            formHelper = new FormHelper(this);
            levelManager = new NavigationManager(getStateSequence());
            levelManager.addListener(new HierarchyLevelListener());

            if (savedInstanceState == null) {

                if (null != getIntent().getStringArrayListExtra(HIERARCHY_PATH_KEYS)) {
                    ArrayList<String> hierarchyPathKeys = getIntent().getStringArrayListExtra(HIERARCHY_PATH_KEYS);
                    for (String key : hierarchyPathKeys) {
                        hierarchyPath.put(key, (DataWrapper) getIntent().getParcelableExtra(key + HIERARCHY_PATH_VALUES));
                    }
                    currentResults = getIntent().getParcelableArrayListExtra(CURRENT_RESULTS_KEY);
                }

                //fresh activity
                hierarchyButtonFragment = new HierarchyButtonFragment();
                hierarchyButtonFragment.setNavigator(this);
                valueFragment = new DataSelectionFragment();
                valueFragment.setSelectionHandler(new ValueSelectionHandler());
                formFragment = new FormSelectionFragment();
                formFragment.setSelectionHandler(new FormSelectionHandler());
                detailToggleFragment = new DetailToggleFragment();
                detailToggleFragment.setNavigateActivity(this);
                defaultDetailFragment = new DefaultDetailFragment();
                visitFragment = new VisitFragment();
                visitFragment.setNavigateActivity(this);
                formListFragment = new FormListFragment();

                getFragmentManager().beginTransaction()
                        .add(R.id.left_column_top, hierarchyButtonFragment, HIERARCHY_BUTTON_FRAGMENT_TAG)
                        .add(R.id.left_column_bottom, detailToggleFragment, TOGGLE_FRAGMENT_TAG)
                        .add(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG)
                        .add(R.id.right_column_top, formFragment, FORM_FRAGMENT_TAG)
                        .add(R.id.right_column_middle, visitFragment, VISIT_FRAGMENT_TAG)
                        .add(R.id.right_column_bottom, formListFragment, VIEW_PATH_FORM_FRAGMENT_TAG)
                        .commit();
            } else {

                FragmentManager fragmentManager = getFragmentManager();
                // restore saved activity state
                hierarchyButtonFragment = (HierarchyButtonFragment) fragmentManager.findFragmentByTag(HIERARCHY_BUTTON_FRAGMENT_TAG);
                hierarchyButtonFragment.setNavigator(this);
                formFragment = (FormSelectionFragment) fragmentManager.findFragmentByTag(FORM_FRAGMENT_TAG);
                formFragment.setSelectionHandler(new FormSelectionHandler());
                detailToggleFragment = (DetailToggleFragment) fragmentManager.findFragmentByTag(TOGGLE_FRAGMENT_TAG);
                detailToggleFragment.setNavigateActivity(this);
                visitFragment = (VisitFragment) fragmentManager.findFragmentByTag(VISIT_FRAGMENT_TAG);
                visitFragment.setNavigateActivity(this);

                defaultDetailFragment = new DefaultDetailFragment();

                valueFragment = (DataSelectionFragment) fragmentManager.findFragmentByTag(VALUE_FRAGMENT_TAG);
                if (valueFragment == null) {
                    valueFragment = new DataSelectionFragment();
                    detailFragment = (DetailFragment) fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
                    detailFragment.setNavigateActivity(this);
                }
                valueFragment.setSelectionHandler(new ValueSelectionHandler());

                formListFragment = (FormListFragment) fragmentManager.findFragmentByTag(VIEW_PATH_FORM_FRAGMENT_TAG);

                ArrayList<String> hierarchyPathKeys = savedInstanceState.getStringArrayList(HIERARCHY_PATH_KEYS);
                for (String key : hierarchyPathKeys) {
                    hierarchyPath.put(key, (DataWrapper) savedInstanceState.getParcelable(key + HIERARCHY_PATH_VALUES));
                }
                currentResults = savedInstanceState.getParcelableArrayList(CURRENT_RESULTS_KEY);
                setCurrentVisit((Visit) savedInstanceState.get(VISIT_KEY));
            }

            configure(currentModule);

        } catch (Exception e) {
            Log.e(TAG, "failed to create navigation module by name " + currentModuleName, e);
        }
    }

    /**
     * Setup the activity based on the specified navigation module.
     */
    private void configure(NavigatorModule module){
        setTitle(module.getActivityTitle());
        hierarchyButtonFragment.setButtonDrawable(R.drawable.data_selector);
        valueFragment.setDataSelectionDrawableId(R.drawable.data_selector);
        formFragment.setFormSelectionDrawableId(R.drawable.form_selector);
        View middleColumn = findViewById(R.id.middle_column);
        middleColumn.setBackgroundResource(R.drawable.gray_middle_column_drawable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hierarchySetup();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        ArrayList<String> hierarchyPathKeys = new ArrayList<>(hierarchyPath.keySet());
        for (String key : hierarchyPathKeys) {
            savedInstanceState.putParcelable(key + HIERARCHY_PATH_VALUES, hierarchyPath.get(key));
        }
        savedInstanceState.putStringArrayList(HIERARCHY_PATH_KEYS, hierarchyPathKeys);

        savedInstanceState.putParcelableArrayList(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);
        savedInstanceState.putSerializable(VISIT_KEY, getCurrentVisit());
        super.onSaveInstanceState(savedInstanceState);
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
                intent.setClass(this, LoginActivity.class);
                break;
            case R.id.field_worker_home_menu_button:
                intent.setClass(this, FieldWorkerActivity.class);
                intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, getCurrentFieldWorker());
                break;
            default:
                String menuModule = menuItemTags.get(item);
                if (menuModule != null) {
                    intent.setClass(this, HierarchyNavigatorActivity.class);
                    intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, getCurrentFieldWorker());
                    intent.putExtra(FieldWorkerActivity.ACTIVITY_MODULE_EXTRA, menuModule);
                    intent.putParcelableArrayListExtra(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);
                    ArrayList<String> hierarchyPathKeys = new ArrayList<>(hierarchyPath.keySet());
                    for (String key : hierarchyPathKeys) {
                        intent.putExtra(key + HIERARCHY_PATH_VALUES, hierarchyPath.get(key));
                    }
                    intent.putStringArrayListExtra(HIERARCHY_PATH_KEYS, hierarchyPathKeys);
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
        startActivity(intent);
        return true;
    }

    private void hierarchySetup() {
        int stateIndex = 0;
        for (String state : getStateSequence()) {
            if (hierarchyPath.containsKey(state)) {
                updateButtonLabel(state);
                hierarchyButtonFragment.setVisible(state, true);
                stateIndex++;
            } else {
                break;
            }
        }

        String state = getStateSequence().get(stateIndex);
        if (stateIndex == 0) {
            hierarchyButtonFragment.setVisible(state, true);
            currentResults = queryHelper.getAll(getContentResolver(), getStateSequence().get(0));
            updateToggleButton();

        } else {
            String previousState = getStateSequence().get(stateIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousState);
            currentSelection = previousSelection;
            if(currentResults == null) {
                currentResults = queryHelper.getChildren(getContentResolver(), previousSelection, state);
            }
        }

        boolean isAdded = valueFragment.isAdded();

        // make sure that listeners will fire for the current state
        refreshHierarchy(state);

        if (isAdded || !currentResults.isEmpty()) {
            showValueFragment();
            valueFragment.populateData(currentResults);
        } else {
            showDetailFragment();
            detailToggleFragment.setButtonHighlighted(true);
        }
        updateAttachedForms();

        visitFragment.setButtonEnabled(getCurrentVisit() != null);
    }

    void detachFormsFromHierarchy(List<String> formPaths) {
        DatabaseAdapter.getInstance(this).detachFromHierarchy(formPaths);
    }

    private void populateFormView(List<FormInstance> forms) {
        formListFragment.populate(forms);
    }

    private List<FormInstance> getAttachedForms(String hierarchyPath) {
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
        Collection<String> attachedPaths = dbAdapter.findFormsForHierarchy(hierarchyPath);
        return OdkCollectHelper.getByPaths(getContentResolver(), attachedPaths);
    }

    public Map<String, DataWrapper> getHierarchyPath() {
        return hierarchyPath;
    }

    public String getState() {
        return levelManager.getLevel();
    }

    private void updateButtonLabel(String state) {
        DataWrapper selected = hierarchyPath.get(state);
        if (selected == null) {
            String stateLabel = getString(getStateLabels().get(state));
            hierarchyButtonFragment.setButtonLabel(state, stateLabel, null, true);
            hierarchyButtonFragment.setHighlighted(state, true);
        } else {
            hierarchyButtonFragment.setButtonLabel(state, selected.getName(), selected.getExtId(), false);
            hierarchyButtonFragment.setHighlighted(state, false);
        }
    }

    @Override
    public Map<String, Integer> getStateLabels() {
        return currentModule.getHierarchyInfo().getStateLabels();
    }

    @Override
    public List<String> getStateSequence() {
        return currentModule.getHierarchyInfo().getStateSequence();
    }

    @Override
    public void jumpUp(String targetState) {
        int targetIndex = getStateSequence().indexOf(targetState);
        if (targetIndex < 0) {
            throw new IllegalStateException("Target state <" + targetState + "> is not a valid state");
        }

        String currentState = getState();
        int currentIndex = getStateSequence().indexOf(currentState);
        if (targetIndex >= currentIndex) {
            // use stepDown() to go down the hierarchy
            return;
        }

        // un-traverse the hierarchy up to the target state
        for (int i = currentIndex; i >= targetIndex; i--) {
            String state = getStateSequence().get(i);
            hierarchyButtonFragment.setVisible(state, false);
            hierarchyPath.remove(state);

        }

        // prepare to stepDown() from this target state
        if (0 == targetIndex) {
            // root of the hierarchy
            currentResults = queryHelper.getAll(getContentResolver(), getStateSequence().get(0));
        } else {
            // middle of the hierarchy
            String previousState = getStateSequence().get(targetIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousState);
            currentSelection = previousSelection;
            currentResults = queryHelper.getChildren(getContentResolver(), previousSelection, targetState);
        }
        levelManager.moveTo(targetState);

    }

    @Override
    public void stepDown(DataWrapper selected) {
        String currentState = getState();

        if (!currentState.equals(selected.getCategory())) {
            throw new IllegalStateException("Selected state <"
                    + selected.getCategory() + "> mismatch with current state <"
                    + currentState + ">");
        }

        int currentIndex = getStateSequence().indexOf(currentState);
        if (currentIndex >= 0 && currentIndex < getStateSequence().size() - 1) {
            String nextState = getStateSequence().get(currentIndex + 1);

            currentSelection = selected;
            currentResults = queryHelper.getChildren(getContentResolver(), selected, nextState);

            hierarchyPath.put(currentState, selected);
            levelManager.moveTo(nextState);
        }
    }

    public void launchForm(FormBehavior formBehavior, Map<String, String> followUpFormHints) {
        formHelper.setBehavior(formBehavior); // update activity's current form
        if (formBehavior != null) {
            formHelper.setData(buildDataWithHints(formBehavior, followUpFormHints));
            if (formBehavior.requiresSearch()) {
                launchSearch();
            } else {
                launchNewForm();
            }
        }
    }

    private Map<String, String> buildDataWithHints(FormBehavior behavior, Map<String, String> followUpHints) {
        Map<String, String> formData = behavior.getBuilder().buildPayload(this);
        if(followUpHints != null){
            formData.putAll(followUpHints);
        }
        return formData;
    }

    private void launchNewFormAfterSearch(Intent data) {
        List<EntityFieldSearch> searchModules = data.getParcelableArrayListExtra(EntitySearchActivity.SEARCH_MODULES_KEY);
        for (EntityFieldSearch plugin : searchModules) {
            formHelper.getData().put(plugin.getName(), plugin.getValue());
        }
        launchNewForm();
    }

    private void launchNewForm() {
        try {
            showShortToast(this, R.string.launching_odk_collect);
            startActivityForResult(editIntent(formHelper.newInstance()), ODK_ACTIVITY_REQUEST_CODE);
        } catch (Exception e) {
            showShortToast(this, "failed to launch form: " + e.getMessage());
        }
    }

    private void launchSearch() {
        Intent intent = new Intent(this, EntitySearchActivity.class);
        ArrayList<EntityFieldSearch> searchModules = formHelper.getBehavior().getRequiredSearches();
        intent.putParcelableArrayListExtra(EntitySearchActivity.SEARCH_MODULES_KEY, searchModules);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    private void showValueFragment() {
        // there is only 1 value fragment that can be added
        if (!valueFragment.isAdded()) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG).commit();
            getFragmentManager().executePendingTransactions();

            valueFragment.populateData(currentResults);
        }
    }

    private void showDetailFragment() {
        // we don't check if it is added here because there are detail fragments for each state
        detailFragment = getDetailFragmentForCurrentState();
        detailFragment.setNavigateActivity(this);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.middle_column, detailFragment, DETAIL_FRAGMENT_TAG)
                .commit();
        getFragmentManager().executePendingTransactions();

        detailFragment.setUpDetails();
    }

    private DetailFragment getDetailFragmentForCurrentState() {

        if (null != (detailFragment = currentModule.getDetailFragsForStates().get(getState()))) {
            return detailFragment;
        }
        return defaultDetailFragment;
    }

    private boolean shouldShowDetailFragment() {
        return currentResults == null || currentResults.isEmpty();
    }

    private void updateToggleButton() {
        if (null != currentModule.getDetailFragsForStates().get(getState()) && !shouldShowDetailFragment()) {

            detailToggleFragment.setButtonEnabled(true);
            if (!valueFragment.isAdded()) {
                detailToggleFragment.setButtonHighlighted(true);
            }
        } else {
            detailToggleFragment.setButtonEnabled(false);
        }
    }

    // for ONCLICK of the toggleFrag
    public void toggleMiddleFragment() {
        if (valueFragment.isAdded()) {
            showDetailFragment();
            detailToggleFragment.setButtonHighlighted(true);
        } else if (detailFragment.isAdded()) {
            showValueFragment();
            detailToggleFragment.setButtonHighlighted(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ODK_ACTIVITY_REQUEST_CODE:
                    handleFormResult(data);
                    break;
                case SEARCH_ACTIVITY_REQUEST_CODE:
                    launchNewFormAfterSearch(data);
                    break;
            }
        }
    }

    /**
     * Handles forms created with launchNewForm on return from ODK.
     */
    private void handleFormResult(Intent data) {
        FormInstance instance = formHelper.getInstance(data.getData());
        associateFormToPath(instance.getFilePath());
        if (instance.isComplete()) {
            FormPayloadConsumer consumer = formHelper.getBehavior().getConsumer();
            try {
                clearResults();
                consumerResult = consumer.consumeFormPayload(formHelper.fetch(), this);
                if (consumerResult.hasInstanceUpdates()) {
                    consumer.augmentInstancePayload(formHelper.getData());
                    try {
                        formHelper.update();
                    } catch (IOException ue) {
                        showShortToast(this, "Update failed: " + ue.getMessage());
                    }
                }
                if (consumerResult.hasFollowUp()) {
                    launchForm(consumerResult.getFollowUp(), consumerResult.getFollowUpHints());
                }
            } catch (IOException e) {
                showShortToast(this, "Read failed: " + e.getMessage());
            }
        }
    }

    /**
     * Resets the cached results for the current hierarchy position. Calling this prior to hierarchySetup ensures
     * that any changes to the database will be reflected in the user interface once hierarchySetup terminates.
     */
    private void clearResults() {
        currentResults = null;
    }

    /**
     * Constructs a string representing the current location hierarchy path for attaching forms to the hierarchy.
     */
    private String currentHierarchyPath() {
        String SEP = "/";
        StringBuilder b = new StringBuilder(SEP);
        List<String> stateSequence = getStateSequence();
        Map<String, DataWrapper> hierarchyPath = getHierarchyPath();
        for (String state : stateSequence) {
            DataWrapper pathData = hierarchyPath.get(state);
            if (pathData != null) {
                b.append(pathData.getExtId());
                b.append(SEP);
            }
        }
        return b.toString();
    }

    /**
     * Associates the form instance path to the current hierarchy path of the activity.
     */
    private void associateFormToPath(String formPath) {
        String path = currentHierarchyPath();
        DatabaseAdapter dbAdapter = DatabaseAdapter.getInstance(this);
        if (formPath != null) {
            dbAdapter.attachFormToHierarchy(path, formPath);
        }
    }

    /**
     * Refreshes the attached forms at the current hierarchy path and prunes sent form associations.
     */
    private void updateAttachedForms() {
        List<FormInstance> unsentForms = new ArrayList<>();
        List<String> sentFormPaths = new ArrayList<>();
        for (FormInstance attachedForm : getAttachedForms(currentHierarchyPath())) {
            if (attachedForm.isSubmitted()) {
                sentFormPaths.add(attachedForm.getFilePath());
            } else {
                unsentForms.add(attachedForm);
            }
        }
        detachFormsFromHierarchy(sentFormPaths);
        populateFormView(unsentForms);
    }

    public DataWrapper getCurrentSelection() {
        return currentSelection;
    }

    @Override
    public void onBackPressed() {
        int currentStateIndex;
        if (0 < (currentStateIndex = getStateSequence().indexOf(getState()))) {
            jumpUp(getStateSequence().get(currentStateIndex - 1));
        } else {
            super.onBackPressed();
        }
    }

    public FieldWorker getCurrentFieldWorker() {
        return currentFieldWorker;
    }

    public void setCurrentFieldWorker(FieldWorker currentFieldWorker) {
        this.currentFieldWorker = currentFieldWorker;
    }

    public Visit getCurrentVisit() {
        return currentVisit;
    }

    public ConsumerResult getConsumerResult() {
        return consumerResult;
    }

    public void setCurrentVisit(Visit currentVisit) {
        this.currentVisit = currentVisit;
    }

    public void startVisit(Visit visit) {
        setCurrentVisit(visit);
        visitFragment.setButtonEnabled(true);
    }

    public void finishVisit() {
        setCurrentVisit(null);
        visitFragment.setButtonEnabled(false);
        refreshHierarchy(getState());
    }

    private void refreshHierarchy(String state){
        levelManager.moveTo(levelManager.getTop());
        levelManager.moveTo(state);
    }

    // Respond when the navigation state machine changes state.
    private class HierarchyLevelListener implements LevelListener {

        @Override
        public void onEntering(String level) {

            updateButtonLabel(level);

            if (!level.equals(levelManager.getBottom())) {
                hierarchyButtonFragment.setVisible(level, true);
            }

            if (shouldShowDetailFragment()) {
                showDetailFragment();
            } else {
                showValueFragment();
                valueFragment.populateData(currentResults);
            }
            updateToggleButton();

            List<FormBehavior> formsToDisplay = new ArrayList<>();
            for (FormBehavior form : currentModule.getFormsForState(level)) {
                if (form.getFilter().shouldDisplay(HierarchyNavigatorActivity.this)) {
                    formsToDisplay.add(form);
                }
            }
            updateAttachedForms();
            formFragment.createFormButtons(formsToDisplay);
        }

        @Override
        public void onLeaving(String level) {
            updateButtonLabel(level);
        }
    }

    // Receive a value that the user clicked in ValueSelectionFragment.
    private class ValueSelectionHandler implements DataSelectionFragment.SelectionHandler {
        @Override
        public void handleSelectedData(DataWrapper dataWrapper) {
            stepDown(dataWrapper);
        }
    }

    // Receive a form that the user clicked in FormSelectionFragment.
    private class FormSelectionHandler implements FormSelectionFragment.SelectionHandler {
        @Override
        public void handleSelectedForm(FormBehavior formBehavior) {
            launchForm(formBehavior, null);
        }
    }

    private interface LevelListener {
        void onEntering(String level);
        void onLeaving(String level);
    }

    private static class NavigationManager {

        private String top, bottom;
        private Set<String> levels;
        private String currentLevel;
        private List<LevelListener> listeners;

        public NavigationManager(List<String> levels) {
            this.levels = new LinkedHashSet<>(levels);
            top = levels.get(0);
            bottom = levels.get(levels.size() - 1);
            listeners = new ArrayList<>();
            moveTo(top);
        }

        public String getTop() {
            return top;
        }

        public String getBottom() {
            return bottom;
        }

        public String getLevel() {
            return currentLevel;
        }

        public void moveTo(String level) {
            if (!levels.contains(level)) {
                throw new IllegalStateException("no such level: " + level);
            }
            if (!level.equals(currentLevel)) {
                if (currentLevel != null) {
                    fireLeaving(currentLevel);
                }
                currentLevel = level;
                fireEntering(level);
            }
        }

        public void addListener(LevelListener listener) {
            listeners.add(listener);
        }

        private void fireLeaving(String level) {
            for (LevelListener listener : listeners) {
                listener.onLeaving(level);
            }
        }

        private void fireEntering(String level) {
            for (LevelListener listener : listeners) {
                listener.onEntering(level);
            }
        }
    }
}
