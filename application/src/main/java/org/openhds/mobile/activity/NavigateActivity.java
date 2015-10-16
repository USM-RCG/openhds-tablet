package org.openhds.mobile.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.FormSelectionFragment;
import org.openhds.mobile.fragment.DataSelectionFragment;
import org.openhds.mobile.fragment.navigate.DetailToggleFragment;
import org.openhds.mobile.fragment.navigate.HierarchyButtonFragment;
import org.openhds.mobile.fragment.navigate.ViewPathFormsFragment;
import org.openhds.mobile.fragment.navigate.VisitFragment;
import org.openhds.mobile.fragment.navigate.detail.DefaultDetailFragment;
import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.model.form.FormBehaviour;
import org.openhds.mobile.model.form.FormHelper;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.utilities.StateMachine;
import org.openhds.mobile.utilities.StateMachine.StateListener;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.projectdata.FormPayloadConsumers.ConsumerResults;
import org.openhds.mobile.projectdata.FormPayloadConsumers.FormPayloadConsumer;
import org.openhds.mobile.projectdata.NavigatePluginModule;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ModuleUiHelper;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openhds.mobile.utilities.ConfigUtils.getAppVersionNumber;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showShortToast;

public class NavigateActivity extends Activity implements HierarchyNavigator {

    private static final int ODK_ACTIVITY_REQUEST_CODE = 0;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 1;

    private HierarchyButtonFragment hierarchyButtonFragment;
    private DataSelectionFragment valueFragment;
    private FormSelectionFragment formFragment;
    private DetailToggleFragment detailToggleFragment;
    private DetailFragment defaultDetailFragment;
    private DetailFragment detailFragment;
    private VisitFragment visitFragment;
    private ViewPathFormsFragment viewPathFormsFragment;

    private static final String HIERARCHY_BUTTON_FRAGMENT_TAG = "hierarchyButtonFragment";
    private static final String VALUE_FRAGMENT_TAG = "hierarchyValueFragment";
    private static final String FORM_FRAGMENT_TAG = "hierarchyFormFragment";
    private static final String TOGGLE_FRAGMENT_TAG = "hierarchyToggleFragment";
    private static final String DETAIL_FRAGMENT_TAG = "hierarchyDetailFragment";
    private static final String VISIT_FRAGMENT_TAG = "hierarchyVisitFragment";
    private static final String VIEW_RECENT_FORM_TAG="viewPathFormsFragment";
    private static final String VISIT_KEY = "visitKey";
    private static final String HIERARCHY_PATH_KEYS = "hierarchyPathKeys";
    private static final String HIERARCHY_PATH_VALUES = "hierarchyPathValues";
    private static final String CURRENT_RESULTS_KEY = "currentResults";



    NavigatePluginModule builder;

    private FormHelper formHelper;
    private StateMachine stateMachine;
    private Map<String, DataWrapper> hierarchyPath;
    private List<DataWrapper> currentResults;
    private DataWrapper currentSelection;
    private FieldWorker currentFieldWorker;
    private Visit currentVisit;
    private HashMap<MenuItem, String> menuItemTags;
    private String currentModuleName;
    private ConsumerResults previousConsumerResults;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_activity);

        FieldWorker fieldWorker = (FieldWorker) getIntent().getExtras().get(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA);
        setCurrentFieldWorker(fieldWorker);
        currentModuleName = (String) getIntent().getExtras().get(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA);
        builder = ProjectActivityBuilder.getModuleByName(currentModuleName);

        hierarchyPath = new HashMap<String, DataWrapper>();
        formHelper = new FormHelper(getContentResolver());
        stateMachine = new StateMachine(new HashSet<String>(getStateSequence()), getStateSequence().get(0));


        for (String state : getStateSequence()) {
            stateMachine.registerListener(state, new HierarchyStateListener());
        }

        if (null == savedInstanceState) {

            if(null != getIntent().getStringArrayListExtra(HIERARCHY_PATH_KEYS)) {
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
            viewPathFormsFragment = new ViewPathFormsFragment();



            getFragmentManager().beginTransaction()
                     .add(R.id.left_column_top, hierarchyButtonFragment, HIERARCHY_BUTTON_FRAGMENT_TAG)
                     .add(R.id.left_column_bottom, detailToggleFragment, TOGGLE_FRAGMENT_TAG)
                     .add(R.id.middle_column, valueFragment, VALUE_FRAGMENT_TAG)
                     .add(R.id.right_column_top, formFragment, FORM_FRAGMENT_TAG)
                     .add(R.id.right_column_bottom, visitFragment, VISIT_FRAGMENT_TAG)
                     .add(R.id.view_column_bottom, viewPathFormsFragment, VIEW_RECENT_FORM_TAG)
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
            viewPathFormsFragment = (ViewPathFormsFragment) fragmentManager.findFragmentByTag(VIEW_RECENT_FORM_TAG);

            // draw details if valuefrag is null, the drawing of valuefrag is
            // handled in onResume().
            if (null == valueFragment) {
                valueFragment = new DataSelectionFragment();
                viewPathFormsFragment = (ViewPathFormsFragment) fragmentManager.findFragmentByTag(VIEW_RECENT_FORM_TAG);
                detailFragment = (DetailFragment) fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
                detailFragment.setNavigateActivity(this);
            }
            valueFragment.setSelectionHandler(new ValueSelectionHandler());

            ArrayList<String> hierarchyPathKeys = savedInstanceState.getStringArrayList(HIERARCHY_PATH_KEYS);
            for(String key : hierarchyPathKeys){
                hierarchyPath.put(key, (DataWrapper) savedInstanceState.getParcelable(key + HIERARCHY_PATH_VALUES));
            }
            currentResults = savedInstanceState.getParcelableArrayList(CURRENT_RESULTS_KEY);
            setCurrentVisit((Visit)savedInstanceState.get(VISIT_KEY));
        }
        setActivityVisualTheme(builder.getModuleUiHelper());
    }

    // Takes in a NavigatePluginModule's (builder) ModuleUiHelper and sets all the fragment's drawables
    private void setActivityVisualTheme(ModuleUiHelper uiHelper){

        setTitle(getString(uiHelper.getModuleTitleStringId()));
        hierarchyButtonFragment.setHiearchySelectionDrawableId(uiHelper.getHierarchySelectionDrawableId());
        valueFragment.setDataSelectionDrawableId(uiHelper.getDataSelectionDrawableId());
        formFragment.setFormSelectionDrawableId(uiHelper.getFormSelectionDrawableId());
        View middleColumn = findViewById(R.id.middle_column);
        middleColumn.setBackgroundResource(uiHelper.getMiddleColumnDrawableId());

    }

    @Override
    protected void onResume() {
        super.onResume();
        hierarchySetup();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        ArrayList<String> hierarchyPathKeys = new ArrayList<String>(hierarchyPath.keySet());
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

        // get the list of modules to iterate through
        List<String> modulesList = ProjectActivityBuilder.getActivityModuleNames();

        // reference to the current hierarchy's name
        String currentHierarchy = builder.getHierarchyInfo().getHierarchyName();

        // for each module
        for(String moduleName : modulesList){

            // keep a reference to make the code readable
            NavigatePluginModule module = ProjectActivityBuilder.getModuleByName(moduleName);

            // if that module has the same hierarchy as the current module
            if(module.getHierarchyInfo().getHierarchyName().equals(currentHierarchy) && !moduleName.equals(currentModuleName)){

                // keep a reference to make the code readable
                ModuleUiHelper uiHelper = module.getModuleUiHelper();

                // add a menuItem for the module, set its UI, give it a tag for OnClick
                MenuItem menuItem = menu.add(uiHelper.getModuleTitleStringId());
                menuItem.setIcon(uiHelper.getModulePortalDrawableId());
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                menuItemTags.put(menuItem, moduleName);

            }

        }



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent();

        if(item.getItemId() == R.id.logout_menu_button) {
            intent.setClass(this, OpeningActivity.class);
            startActivity(intent);
            return true;
        }
        if(item.getItemId() == R.id.field_worker_home_menu_button) {
            intent.setClass(this, PortalActivity.class);
            intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, getCurrentFieldWorker());
            startActivity(intent);
            return true;
        }

        if(null != menuItemTags.get(item)) {
            intent.setClass(this, NavigateActivity.class);
            intent.putExtra(FieldWorkerLoginFragment.FIELD_WORKER_EXTRA, getCurrentFieldWorker());
            intent.putExtra(ProjectActivityBuilder.ACTIVITY_MODULE_EXTRA, menuItemTags.get(item));
            intent.putParcelableArrayListExtra(CURRENT_RESULTS_KEY, (ArrayList<DataWrapper>) currentResults);

            ArrayList<String> hierarchyPathKeys = new ArrayList<String>(hierarchyPath.keySet());
            for (String key : hierarchyPathKeys) {
                intent.putExtra(key + HIERARCHY_PATH_VALUES, hierarchyPath.get(key));
            }
            intent.putStringArrayListExtra(HIERARCHY_PATH_KEYS, hierarchyPathKeys);

            startActivity(intent);
            return true;
        }
            return super.onOptionsItemSelected(item);

    }

    private void hierarchySetup() {
        int stateIndex = 0;
        for (String state : getStateSequence()) {
            if (hierarchyPath.containsKey(state)) {
                updateButtonLabel(state);
                hierarchyButtonFragment.setButtonAllowed(state, true);
                stateIndex++;
            } else {
                break;
            }
        }

        String state = getStateSequence().get(stateIndex);
        if (0 == stateIndex) {
            hierarchyButtonFragment.setButtonAllowed(state, true);
            currentResults = builder.getQueryHelper().getAll(getContentResolver(), getStateSequence().get(0));
            updateToggleButton();

        } else {
            String previousState = getStateSequence().get(stateIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousState);
            currentSelection = previousSelection;
            if(null == currentResults) {
                currentResults = builder.getQueryHelper().getChildren(getContentResolver(), previousSelection, state);
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
        //populate recent forms
        populateFormsForPath();

        if(null != getCurrentVisit()){
            visitFragment.setButtonEnabled(true);
        }
    }

    private void populateFormsForPath() {

        List<FormInstance> recforms;
        ContentResolver reslovr = this.getContentResolver();
        DatabaseAdapter dbAdpt = new DatabaseAdapter(this);

        //get all the existing recent form paths
        Collection<String> formsdel=  dbAdpt.findAllFormPaths();

        //find if any of the form is submitted
        List <String> submitForms  = OdkCollectHelper.getSentFormPaths(getContentResolver(), formsdel);

        //if submitted, delete those paths from Recent_form_DB
        if(!submitForms.isEmpty()) {
           for (String sf : submitForms) {
                dbAdpt.deleteSubmitForms(sf);
            }
        }

        //display all recent forms for the current hierarchy path
        Collection forms = dbAdpt.findFormByPath(currentHierarchyPath());

        if (!forms.isEmpty()) {
            //get forms instances from ODK
            recforms = OdkCollectHelper.getFormInstancesByPath(reslovr, forms);
            viewPathFormsFragment.populateRecentFormInstanceListView(recforms);
        }
        else {
            recforms = Collections.EMPTY_LIST;
            viewPathFormsFragment.populateRecentFormInstanceListView(recforms);
        }
    }


    public Map<String, DataWrapper> getHierarchyPath() {
        return hierarchyPath;
    }

    public List<DataWrapper> getCurrentResults() {
        return currentResults;
    }

    public String getState() {
        return stateMachine.getState();
    }

    private void updateButtonLabel(String state) {

        DataWrapper selected = hierarchyPath.get(state);
        if (null == selected) {
            String stateLabel = getResourceString(NavigateActivity.this, getStateLabels().get(state));
            hierarchyButtonFragment.setButtonLabel(state, stateLabel, null, true);
            hierarchyButtonFragment.setButtonHighlighted(state, true);
        } else {
            hierarchyButtonFragment.setButtonLabel(state, selected.getName(), selected.getExtId(), false);
            hierarchyButtonFragment.setButtonHighlighted(state, false);
        }
    }

    @Override
    public Map<String, Integer> getStateLabels() {
        return builder.getHierarchyInfo().getStateLabels();
    }

    @Override
    public List<String> getStateSequence() {
        return builder.getHierarchyInfo().getStateSequence();
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
            hierarchyButtonFragment.setButtonAllowed(state, false);
            hierarchyPath.remove(state);

        }

        // prepare to stepDown() from this target state
        if (0 == targetIndex) {
            // root of the hierarchy
            currentResults = builder.getQueryHelper().getAll(getContentResolver(), getStateSequence().get(0));
        } else {
            // middle of the hierarchy
            String previousState = getStateSequence().get(targetIndex - 1);
            DataWrapper previousSelection = hierarchyPath.get(previousState);
            currentSelection = previousSelection;
            currentResults = builder.getQueryHelper().getChildren(getContentResolver(), previousSelection, targetState);
        }
        stateMachine.transitionTo(targetState);

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
            currentResults = builder.getQueryHelper().getChildren(getContentResolver(), selected, nextState);

            hierarchyPath.put(currentState, selected);
            stateMachine.transitionTo(nextState);
        }
    }

    @Override
    public void launchForm(FormBehaviour formBehaviour, Map<String,String> followUpFormHints) {


        // use the given form as the current form
        formHelper.setFormBehaviour(formBehaviour);


        Map<String, String> formFieldData = new HashMap<String, String>();
        if(null != followUpFormHints){
            formFieldData.putAll(followUpFormHints);
        }

        formBehaviour.getFormPayloadBuilder().buildFormPayload(formFieldData, this);
        formHelper.setFormFieldData(formFieldData);

        // if needed, ask the user to search for required form field data
        if (formBehaviour.getNeedsFormFieldSearch()) {
            launchCurrentFormInSearchActivity();
            return;
        }

        // otherwise, launch the form in ODK immediately
        launchCurrentFormInODK();
    }

    private void launchCurrentFormInODK() {
        FormBehaviour formBehaviour = formHelper.getFormBehaviour();
        if (null != formBehaviour && null != formBehaviour.getFormName()) {

            FormInstance formInstance;
            try {
                formInstance = formHelper.newFormInstance();
            } catch (Exception e) {
                showShortToast(this, "Error creating Form instance: " + e.getMessage());
                return;
            }

            if(null == formInstance){
                showShortToast(this, "Warning: Could not find '"+formBehaviour.getFormName()+"' form.");
                return;
            }

            if(null == formInstance.getFormVersion()){
                showShortToast(this, "Warning: Form has no defined Version Number.");
                return;
            }

            String appVersionNumber = Integer.toString(getAppVersionNumber(this));

            if(!formInstance.getFormVersion().equals(appVersionNumber)){
                showShortToast(this, "Warning: Version difference between CIMS ("+appVersionNumber+") and ODK ("+formInstance.getFormVersion()+").");
            }

            Intent intent = formHelper.buildEditFormInstanceIntent();
            showShortToast(this, R.string.launching_odk_collect);

            // clear currentResults to get the most up-to-date currentResults after the form is consumed
            currentResults = null;

            startActivityForResult(intent, ODK_ACTIVITY_REQUEST_CODE);
            return;

        }
    }

    private void launchCurrentFormInSearchActivity() {
        FormBehaviour formBehaviour = formHelper.getFormBehaviour();
        if (null != formBehaviour) {
            // put form search plugins in the intent so we know what the user needs to search for
            Intent intent = new Intent(this, FormSearchActivity.class);
            intent.putParcelableArrayListExtra(
                    FormSearchActivity.FORM_SEARCH_PLUGINS_KEY, formBehaviour.getFormSearchPluginModules());
            startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
        }
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

        if (null != (detailFragment = builder.getDetailFragsForStates().get(getState()))) {
            return detailFragment;
        }
        return defaultDetailFragment;
    }

    private boolean shouldShowDetailFragment() {
        if (null == currentResults || currentResults.isEmpty()) {
            return true;
        }
        return false;
    }

    private void updateToggleButton() {
        if (null != builder.getDetailFragsForStates().get(getState()) && !shouldShowDetailFragment()) {

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

    private Map<String, Set<String>> hierPathInstances = new HashMap<>();

    private String currentHierarchyPath() {
        String SEP = "/";
        StringBuilder b = new StringBuilder(SEP);
        List<String> stateSequence = getStateSequence();
        Map<String, DataWrapper> hierarchyPath = getHierarchyPath();
        for (String state : stateSequence) {
            DataWrapper pathData = hierarchyPath.get(state);
            if (pathData != null) {
                b.append(pathData.getExtId());
                b.append("/");
            }
        }
        return b.toString();
    }

    private void associateFormToPath(String formId) {
        String path = currentHierarchyPath();
        DatabaseAdapter dbadpter = new DatabaseAdapter(this);
        if (formId != null) {
                dbadpter.addHierarchyPath(path, formId);
        }
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case ODK_ACTIVITY_REQUEST_CODE:
                    // consume form data that the user entered with ODK
                    FormBehaviour formBehaviour = formHelper.getFormBehaviour();
                    formHelper.checkFormInstanceStatus();
                    if (formHelper.getFinalizedFormFilePath() != null) {

                        // Tie the form instance to the hierarchy path
                        associateFormToPath(formHelper.getFinalizedFormFilePath());

                        FormPayloadConsumer consumer = formBehaviour.getFormPayloadConsumer();
                        if (consumer != null) {
                            previousConsumerResults = consumer.consumeFormPayload(formHelper.fetchFormInstanceData(), this);
                            if (previousConsumerResults.needsPostfill()) {
                                consumer.postFillFormPayload(formHelper.getFormFieldData());
                                formHelper.updateExistingFormInstance();
                            }
                            if (null != previousConsumerResults.getFollowUpFormBehaviour()){
                                launchForm(previousConsumerResults.getFollowUpFormBehaviour(), previousConsumerResults.getFollowUpFormHints());
                                return;
                            }
                        }
                    }

                    // encrypt files regardless
                    List<FormInstance> allFormInstances = OdkCollectHelper.getAllFormInstances(getContentResolver());
                    if (null != allFormInstances) {
                        EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
                    }

                    // TODO: launchForm() for follow-up, if any.

                    return;

                case SEARCH_ACTIVITY_REQUEST_CODE:
                    // data intent contains the form fields and values that the user just search for
                    List<FormSearchPluginModule> formSearchPluginModules =
                            data.getParcelableArrayListExtra(FormSearchActivity.FORM_SEARCH_PLUGINS_KEY);

                    // merge searched fields with the existing form payload
                    for (FormSearchPluginModule plugin : formSearchPluginModules) {
                        formHelper.getFormFieldData().put(plugin.getFieldName(), plugin.getFieldValue());
                    }

                    // now let the user finish filling in the form in ODK
                    launchCurrentFormInODK();
                    return;
            }
        }
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

    public ConsumerResults getPreviousConsumerResults() {
        return previousConsumerResults;
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
        stateMachine.transitionTo(getStateSequence().get(0));
        stateMachine.transitionTo(state);
    }

    // Respond when the navigation state machine changes state.
    private class HierarchyStateListener implements StateListener {

        @Override
        public void onEnterState() {
            String state = getState();
            updateButtonLabel(state);

            if (!state.equals(getStateSequence().get(getStateSequence().size() - 1))) {
                hierarchyButtonFragment.setButtonAllowed(state, true);
            }

            List<FormBehaviour> filteredForms = builder.getFormsForStates().get(state);
            List<FormBehaviour> validForms = new ArrayList<FormBehaviour>();

            for (FormBehaviour form : filteredForms) {
                if (form.getFormFilter().amIValid(NavigateActivity.this)) {
                    validForms.add(form);
                }
            }
            //populate recent forms
            populateFormsForPath();

            if (shouldShowDetailFragment()) {
                showDetailFragment();
            } else {
                showValueFragment();
                valueFragment.populateData(currentResults);
            }
            updateToggleButton();

            formFragment.createFormButtons(validForms);
        }

        @Override
        public void onExitState() {
            String state = getState();
            updateButtonLabel(state);
        }
    }

    // Receive a value that the user clicked in ValueSelectionFragment.
    private class ValueSelectionHandler implements DataSelectionFragment.SelectionHandler {
        @Override
        public void handleSelectedData(DataWrapper dataWrapper) {
            NavigateActivity.this.stepDown(dataWrapper);
        }
    }

    // Receive a form that the user clicked in FormSelectionFragment.
    private class FormSelectionHandler implements FormSelectionFragment.SelectionHandler {
        @Override
        public void handleSelectedForm(FormBehaviour formBehaviour) {
            NavigateActivity.this.launchForm(formBehaviour, null);
        }
    }
}
