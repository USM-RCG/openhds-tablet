package org.openhds.mobile.navconfig;

import android.util.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.fragment.navigate.detail.IndividualDetailFragment;
import org.openhds.mobile.model.form.FormBehavior;
import org.openhds.mobile.navconfig.forms.builders.UpdateFormPayloadBuilders;
import org.openhds.mobile.navconfig.forms.consumers.UpdateFormPayloadConsumers;
import org.openhds.mobile.navconfig.forms.filters.UpdateFormFilters;
import org.openhds.mobile.repository.search.EntityFieldSearch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.ResourceBundle.getBundle;
import static org.openhds.mobile.navconfig.BiokoHierarchy.BOTTOM;
import static org.openhds.mobile.navconfig.BiokoHierarchy.INDIVIDUAL;
import static org.openhds.mobile.repository.search.SearchUtils.getIndividualModule;

/**
 * The configuration source for hierarchy navigation, form display and form data binding for the field worker section of
 * the application.
 *
 * @see org.openhds.mobile.activity.FieldWorkerActivity
 * @see org.openhds.mobile.activity.HierarchyNavigatorActivity
 */
public class NavigatorConfig {

    private static final String TAG = NavigatorConfig.class.getSimpleName();

    private static NavigatorConfig instance;

    private Map<String, NavigatorModule> modules = emptyMap();
    private Map<String, String> formLabels = emptyMap();


    protected NavigatorConfig() {
        init();
    }

    private void init() {
        initModules();
        initFormLabels();
    }

    /*
     * Define the navigation modules. They will show up in the interface in the order specified.
     */
    private void initModules() {
        modules = new LinkedHashMap<>();
        try {
            initCoreModules();
            initExtendedModules();
        } catch (IOException e) {
            Log.e(TAG, "failure initializing modules", e);
        }
    }

    private void initExtendedModules() throws IOException {
        executeConfigScript("/extensions.js");
    }

    private void executeConfigScript(String resourcePath) throws IOException {
        InputStream scriptStream = NavigatorConfig.class.getResourceAsStream(resourcePath);
        if (scriptStream != null) {
            Reader scriptReader = new InputStreamReader(scriptStream);
            Context ctx = Context.enter();
            ctx.setOptimizationLevel(-1);
            try {
                Scriptable scope = ctx.initStandardObjects();
                scope.put("config", scope, this);
                ctx.evaluateReader(scope, scriptReader, resourcePath, 1, null);
            } finally {
                Context.exit();
                scriptStream.close();
            }
        }
    }

    private void initCoreModules() throws IOException {
        executeConfigScript("/core.js");
        for (NavigatorModule module : asList(new UpdateModule(this))) {
            addModule(module);
        }
    }

    public void addModule(NavigatorModule module) {
        modules.put(module.getActivityTitle(), module);
    }

    /*
     * Define the labels to use for rendering stored forms in the UI. It defines the mapping from jr form id to resource
     * bundle key for the label.
     */
    private void initFormLabels() {
        formLabels = new HashMap<>();
        for (NavigatorModule module : modules.values()) {
            formLabels.putAll(module.getFormLabels());
        }
    }

    public static synchronized NavigatorConfig getInstance() {
        if (instance == null) {
            instance = new NavigatorConfig();
        }
        return instance;
    }

    /**
     * Gets all configured navigator modules.
     *
     * @return a list of configured {@link NavigatorModule}s in definition order
     */
    public Collection<NavigatorModule> getModules() {
        return unmodifiableCollection(modules.values());
    }

    /**
     * Returns the label for the specified form id.
     *
     * @param formId the instance id (jrId) of the form
     * @return the label describing the form type
     */
    public String getFormLabel(String formId) {
        if (formLabels.containsKey(formId)) {
            return getString(formLabels.get(formId));
        } else {
            return formId;
        }
    }

    /**
     * Get a localized string from the modular {@link java.util.ResourceBundle}.
     *
     * @param key the resource key for a localized string
     * @return the string, localized for the current {@link java.util.Locale}
     */
    public String getString(String key) {
        return getBundle("modulestrings").getString(key);
    }

    /**
     * Get logical names for the configured hierarchy levels.
     *
     * @return a list of configured hier levels, from highest to lowest.
     */
    public List<String> getLevels() {
        return BiokoHierarchy.INSTANCE.getLevels();
    }

    /**
     * Get a localized label for the logical hierarchy level.
     *
     * @param level the hierarchy level
     * @return the level's label for the current locale
     */
    public int getLevelLabel(String level) {
        return BiokoHierarchy.INSTANCE.getLevelLabels().get(level);
    }

    public NavigatorModule getModule(String name) {
        return modules.get(name);
    }
}


/**
 * Basic functionality for {@link NavigatorModule}.
 */
abstract class AbstractNavigatorModule implements NavigatorModule {

    protected final NavigatorConfig config;
    protected final Map<String, List<FormBehavior>> formsForLevels = new HashMap<>();
    protected final Map<String, DetailFragment> detailForLevels = new HashMap<>();
    protected final Map<String, String> formLabels = new HashMap<>();

    AbstractNavigatorModule(NavigatorConfig config) {
        this.config = config;
    }

    /*
     * Forwards localized string lookups to the config from which the module was defined.
     */
    protected String getString(String key) {
        return config.getString(key);
    }

    @Override
    public List<FormBehavior> getForms(String level) {
        if (formsForLevels.get(level) == null) {
            formsForLevels.put(level, new ArrayList<FormBehavior>());
        }
        return formsForLevels.get(level);
    }

    public void labelForm(String formId, String labelKey) {
        formLabels.put(formId, labelKey);
    }

    public void bindForm(String level, FormBehavior form) {
        getForms(level).add(form);
    }

    public void bindDetail(String level, DetailFragment fragment) {
        detailForLevels.put(level, fragment);
    }

    /*
     * These details are off by 1: details for an individual should be shown after clicking a specific individual
     * which is actually the bottom level, not the individual level.
     */
    @Override
    public DetailFragment getDetailFragment(String level) {
        return detailForLevels.get(level);
    }

    @Override
    public Map<String, String> getFormLabels() {
        return formLabels;
    }
}


class UpdateModule extends AbstractNavigatorModule {

    UpdateModule(NavigatorConfig config) {

        super(config);

        labelForm("visit", "visitFormLabel");
        labelForm("in_migration", "inMigrationFormLabel");
        labelForm("individual", "individualFormLabel");
        labelForm("out_migration", "outMigrationFormLabel");
        labelForm("death", "deathFormLabel");
        labelForm("pregnancy_observation", "pregnancyObservationFormLabel");
        labelForm("pregnancy_outcome", "pregnancyOutcomeFormLabel");

        bindForm(INDIVIDUAL, new FormBehavior("visit", "shared.visitLabel",
                new UpdateFormFilters.StartAVisit(),
                new UpdateFormPayloadBuilders.StartAVisit(),
                new UpdateFormPayloadConsumers.StartAVisit()));

        EntityFieldSearch migrantSearch = getIndividualModule(
                ProjectFormFields.Individuals.INDIVIDUAL_UUID, R.string.search_individual_label);
        bindForm(INDIVIDUAL, new FormBehavior("in_migration", "update.internalInMigrationLabel",
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.RegisterInternalInMigration(),
                new UpdateFormPayloadConsumers.RegisterInMigration(),
                migrantSearch));

        FormBehavior externalInMigrationFormBehavior = new FormBehavior("in_migration", "update.externalInMigrationLabel",
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.RegisterExternalInMigration(),
                new UpdateFormPayloadConsumers.RegisterInMigration());

        bindForm(INDIVIDUAL, new FormBehavior("individual", "update.externalInMigrationLabel",
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.AddIndividualFromInMigration(),
                new UpdateFormPayloadConsumers.AddIndividualFromInMigration(externalInMigrationFormBehavior)));

        bindForm(BOTTOM, new FormBehavior("out_migration", "update.outMigrationLabel",
                new UpdateFormFilters.DeathOrOutMigrationFilter(),
                new UpdateFormPayloadBuilders.RegisterOutMigration(),
                new UpdateFormPayloadConsumers.RegisterOutMigration()));

        bindForm(BOTTOM, new FormBehavior("death", "update.deathLabel",
                new UpdateFormFilters.DeathOrOutMigrationFilter(),
                new UpdateFormPayloadBuilders.RegisterDeath(),
                new UpdateFormPayloadConsumers.RegisterDeath()));

        bindForm(BOTTOM, new FormBehavior("pregnancy_observation", "shared.pregnancyObservationLabel",
                new UpdateFormFilters.PregnancyFilter(),
                new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                null));

        EntityFieldSearch paternitySearch = getIndividualModule(
                ProjectFormFields.PregnancyOutcome.FATHER_UUID, R.string.search_father_label);
        bindForm(BOTTOM, new FormBehavior("pregnancy_outcome", "update.pregnancyOutcomeLabel",
                new UpdateFormFilters.PregnancyFilter(),
                new UpdateFormPayloadBuilders.RecordPregnancyOutcome(),
                null, paternitySearch));

        bindDetail(BOTTOM, new IndividualDetailFragment());
    }

    @Override
    public String getLaunchLabel() {
        return getString("update.launchTitle");
    }

    @Override
    public String getLaunchDescription() {
        return getString("update.launchDescription");
    }

    @Override
    public String getActivityTitle() {
        return getString("update.activityTitle");
    }
}