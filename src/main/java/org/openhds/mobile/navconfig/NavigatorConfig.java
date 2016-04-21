package org.openhds.mobile.navconfig;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.fragment.navigate.detail.IndividualDetailFragment;
import org.openhds.mobile.model.form.FormBehavior;
import org.openhds.mobile.navconfig.forms.filters.BiokoFormFilters;
import org.openhds.mobile.navconfig.forms.filters.CensusFormFilters;
import org.openhds.mobile.navconfig.forms.filters.UpdateFormFilters;
import org.openhds.mobile.navconfig.forms.builders.BiokoFormPayloadBuilders;
import org.openhds.mobile.navconfig.forms.builders.CensusFormPayloadBuilders;
import org.openhds.mobile.navconfig.forms.builders.UpdateFormPayloadBuilders;
import org.openhds.mobile.navconfig.forms.consumers.BiokoFormPayloadConsumers;
import org.openhds.mobile.navconfig.forms.consumers.CensusFormPayloadConsumers;
import org.openhds.mobile.navconfig.forms.consumers.UpdateFormPayloadConsumers;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.ResourceBundle.getBundle;
import static org.openhds.mobile.navconfig.BiokoHierarchy.BOTTOM_STATE;
import static org.openhds.mobile.navconfig.BiokoHierarchy.HOUSEHOLD_STATE;
import static org.openhds.mobile.navconfig.BiokoHierarchy.INDIVIDUAL_STATE;
import static org.openhds.mobile.navconfig.forms.filters.InvertedFilter.invert;

/**
 * The configuration source for hierarchy navigation, form display and form data binding for the field worker section of
 * the application.
 *
 * @see org.openhds.mobile.activity.FieldWorkerActivity
 * @see org.openhds.mobile.activity.HierarchyNavigatorActivity
 */
public class NavigatorConfig {

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
        for (NavigatorModule module : asList(new CensusModule(this), new UpdateModule(this), new BiokoModule(this))) {
            modules.put(module.getActivityTitle(), module);
        }
    }

    /*
     * Define the labels to use for rendering stored forms in the UI. It defines the mapping from jr form id to resource
     * bundle key for the label.
     */
    private void initFormLabels() {
        formLabels = new HashMap<>();
        String[][] mappings = {
                {"individual", "individualFormLabel"},
                {"location", "locationFormLabel"},
                {"visit", "visitFormLabel"},
                {"in_migration", "inMigrationFormLabel"},
                {"out_migration", "outMigrationFormLabel"},
                {"bed_net", "bedNetFormLabel"},
                {"death", "deathFormLabel"},
                {"pregnancy_observation", "pregnancyObservationFormLabel"},
                {"pregnancy_outcome", "pregnancyOutcomeFormLabel"},
                {"location_evaluation", "locationEvaluationFormLabel"},
                {"spraying", "sprayingFormLabel"},
                {"super_ojo", "superOjoFormLabel"},
                {"duplicate_location", "duplicateLocationFormLabel"}};
        for (String[] mapping : mappings) {
            formLabels.put(mapping[0], mapping[1]);
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

    public NavigatorModule getModule(String name) {
        return modules.get(name);
    }
}


/**
 * Basic functionality for {@NavigatorModules}.
 */
abstract class AbstractNavigatorModule implements NavigatorModule {

    protected final Map<String, List<FormBehavior>> formsForStates = new HashMap<>();
    protected final Map<String, DetailFragment> detailFragsForStates = new HashMap<>();
    protected final NavigatorConfig config;

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
    public HierarchyInfo getHierarchyInfo() {
        return BiokoHierarchy.INSTANCE;
    }

    @Override
    public List<FormBehavior> getFormsForState(String state) {
        List<FormBehavior> formsForState = formsForStates.get(state);
        if (formsForState == null)
            formsForState = emptyList();
        return formsForState;
    }

    /*
     * These details are off by 1: details for an individual should be shown after clicking a specific individual
     * which is actually the bottom (not the individual) state.
     */
    @Override
    public Map<String, DetailFragment> getDetailFragsForStates() {
        return detailFragsForStates;
    }
}


class BiokoModule extends AbstractNavigatorModule {

    BiokoModule(NavigatorConfig config) {

        super(config);

        List<FormBehavior> individualForms = new ArrayList<>();

        individualForms.add(new FormBehavior("bed_net", "bioko.bednetsLabel",
                new BiokoFormFilters.DistributeBednets(),
                new BiokoFormPayloadBuilders.DistributeBednets(),
                new BiokoFormPayloadConsumers.DistributeBednets()));

        individualForms.add(new FormBehavior("spraying", "bioko.sprayingLabel",
                new BiokoFormFilters.SprayHousehold(),
                new BiokoFormPayloadBuilders.SprayHousehold(),
                new BiokoFormPayloadConsumers.SprayHousehold()));

        individualForms.add(new FormBehavior("super_ojo", "bioko.superOjoLabel",
                null,
                new BiokoFormPayloadBuilders.SuperOjo(),
                new BiokoFormPayloadConsumers.SuperOjo()));

        individualForms.add(new FormBehavior("duplicate_location", "bioko.duplicateLocationLabel",
                null,
                new BiokoFormPayloadBuilders.DuplicateLocation(),
                new BiokoFormPayloadConsumers.DuplicateLocation()));

        formsForStates.put(INDIVIDUAL_STATE, individualForms);
        detailFragsForStates.put(BOTTOM_STATE, new IndividualDetailFragment());
    }

    @Override
    public String getLaunchLabel() {
        return getString("bioko.launchTitle");
    }

    @Override
    public String getLaunchDescription() {
        return getString("bioko.launchDescription");
    }

    @Override
    public String getActivityTitle() {
        return getString("bioko.activityTitle");
    }
}


class CensusModule extends AbstractNavigatorModule {

    CensusModule(NavigatorConfig config) {

        super(config);

        List<FormBehavior> householdFormList = new ArrayList<>();
        List<FormBehavior> individualFormList = new ArrayList<>();

        FormBehavior pregObFormBehavior = new FormBehavior("pregnancy_observation", "shared.pregnancyObservationLabel",
                new UpdateFormFilters.PregnancyFilter(),
                new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                new CensusFormPayloadConsumers.ChainedPregnancyObservation());

        FormBehavior visitPregObFormBehavior = new FormBehavior("visit", "shared.visitLabel",
                new UpdateFormFilters.StartAVisit(),
                new UpdateFormPayloadBuilders.StartAVisit(),
                new CensusFormPayloadConsumers.ChainedVisitForPregnancyObservation(pregObFormBehavior));

        householdFormList.add(new FormBehavior("location", "census.locationLabel",
                new CensusFormFilters.AddLocation(),
                new CensusFormPayloadBuilders.AddLocation(),
                new CensusFormPayloadConsumers.AddLocation()));

        individualFormList.add(new FormBehavior("location_evaluation", "census.evaluateLocationLabel",
                new CensusFormFilters.EvaluateLocation(),
                new CensusFormPayloadBuilders.EvaluateLocation(),
                new CensusFormPayloadConsumers.EvaluateLocation()));

        individualFormList.add(new FormBehavior("individual", "census.headOfHousholdLabel",
                new CensusFormFilters.AddHeadOfHousehold(),
                new CensusFormPayloadBuilders.AddHeadOfHousehold(),
                new CensusFormPayloadConsumers.AddHeadOfHousehold(visitPregObFormBehavior)));

        individualFormList.add(new FormBehavior("individual", "census.householdMemberLabel",
                invert(new CensusFormFilters.AddHeadOfHousehold()),
                new CensusFormPayloadBuilders.AddMemberOfHousehold(),
                new CensusFormPayloadConsumers.AddMemberOfHousehold(visitPregObFormBehavior)));

        formsForStates.put(HOUSEHOLD_STATE, householdFormList);
        formsForStates.put(INDIVIDUAL_STATE, individualFormList);
        detailFragsForStates.put(BOTTOM_STATE, new IndividualDetailFragment());
    }

    @Override
    public String getLaunchLabel() {
        return getString("census.launchTitle");
    }

    @Override
    public String getLaunchDescription() {
        return getString("census.launchDescription");
    }

    @Override
    public String getActivityTitle() {
        return getString("census.activityTitle");
    }
}


class UpdateModule extends AbstractNavigatorModule {

    UpdateModule(NavigatorConfig config) {

        super(config);

        List<FormBehavior> individualFormList = new ArrayList<>();
        List<FormBehavior> bottomFormList = new ArrayList<>();

        // Start a Visit FormBehavior
        individualFormList.add(new FormBehavior("visit", "shared.visitLabel",
                new UpdateFormFilters.StartAVisit(),
                new UpdateFormPayloadBuilders.StartAVisit(),
                new UpdateFormPayloadConsumers.StartAVisit()));

        // Register an Internal Inmigration, requires a search to do
        ArrayList<FormSearchPluginModule> searches = new ArrayList<>();
        searches.add(SearchUtils.getIndividualPlugin(ProjectFormFields.Individuals.INDIVIDUAL_UUID, R.string.search_individual_label));
        individualFormList.add(new FormBehavior("in_migration", "update.internalInMigrationLabel",
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.RegisterInternalInMigration(),
                new UpdateFormPayloadConsumers.RegisterInMigration(),
                searches));

        // Register an External InMigration form (chained after individual form)
        FormBehavior externalInMigrationFormBehavior = new FormBehavior("in_migration", "update.externalInMigrationLabel",
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.RegisterExternalInMigration(),
                new UpdateFormPayloadConsumers.RegisterInMigration());

        // Register an Individual for External InMigration (chained with in_migration form)
        individualFormList.add(new FormBehavior("individual", "update.externalInMigrationLabel",
                new UpdateFormFilters.RegisterInMigration(),
                new UpdateFormPayloadBuilders.AddIndividualFromInMigration(),
                new UpdateFormPayloadConsumers.AddIndividualFromInMigration(externalInMigrationFormBehavior)));

        // Register an OutMigration FormBehavior
        bottomFormList.add(new FormBehavior("out_migration", "update.outMigrationLabel",
                new UpdateFormFilters.DeathOrOutMigrationFilter(),
                new UpdateFormPayloadBuilders.RegisterOutMigration(),
                new UpdateFormPayloadConsumers.RegisterOutMigration()));

        // Register a Death FormBehavior
        bottomFormList.add(new FormBehavior("death", "update.deathLabel",
                new UpdateFormFilters.DeathOrOutMigrationFilter(),
                new UpdateFormPayloadBuilders.RegisterDeath(),
                new UpdateFormPayloadConsumers.RegisterDeath()));

        // Register a Pregnancy Observation FormBehavior
        bottomFormList.add(new FormBehavior("pregnancy_observation", "shared.pregnancyObservationLabel",
                new UpdateFormFilters.PregnancyFilter(),
                new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                null));

        // Register a Pregnancy OutCome FormBehavior
        ArrayList<FormSearchPluginModule> daddySearch = new ArrayList<>();
        daddySearch.add(SearchUtils.getIndividualPlugin(ProjectFormFields.PregnancyOutcome.FATHER_UUID, R.string.search_father_label));
        bottomFormList.add(new FormBehavior("pregnancy_outcome", "update.pregnancyOutcomeLabel",
                new UpdateFormFilters.PregnancyFilter(),
                new UpdateFormPayloadBuilders.RecordPregnancyOutcome(),
                null, daddySearch));

        formsForStates.put(INDIVIDUAL_STATE, individualFormList);
        formsForStates.put(BOTTOM_STATE, bottomFormList);
        detailFragsForStates.put(BOTTOM_STATE, new IndividualDetailFragment());
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