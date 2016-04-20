package org.openhds.mobile.projectdata;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.fragment.navigate.detail.IndividualDetailFragment;
import org.openhds.mobile.model.form.FormBehavior;
import org.openhds.mobile.projectdata.FormFilters.BiokoFormFilters;
import org.openhds.mobile.projectdata.FormFilters.CensusFormFilters;
import org.openhds.mobile.projectdata.FormFilters.UpdateFormFilters;
import org.openhds.mobile.projectdata.FormPayloadBuilders.BiokoFormPayloadBuilders;
import org.openhds.mobile.projectdata.FormPayloadBuilders.CensusFormPayloadBuilders;
import org.openhds.mobile.projectdata.FormPayloadBuilders.UpdateFormPayloadBuilders;
import org.openhds.mobile.projectdata.FormPayloadConsumers.BiokoFormPayloadConsumers;
import org.openhds.mobile.projectdata.FormPayloadConsumers.CensusFormPayloadConsumers;
import org.openhds.mobile.projectdata.FormPayloadConsumers.UpdateFormPayloadConsumers;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static org.openhds.mobile.projectdata.BiokoHierarchy.BOTTOM_STATE;
import static org.openhds.mobile.projectdata.BiokoHierarchy.HOUSEHOLD_STATE;
import static org.openhds.mobile.projectdata.BiokoHierarchy.INDIVIDUAL_STATE;

public class ProjectActivityBuilder {

    public static final String ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA";

    public enum Module {

        CENSUS(CensusActivityModule.class),
        UPDATE(UpdateActivityModule.class),
        BIOKO(BiokoActivityModule.class);

        private Class<? extends NavigatePluginModule> type;

        Module(Class<? extends NavigatePluginModule> type) {
            this.type = type;
        }

        public NavigatePluginModule newInstance() throws IllegalAccessException, InstantiationException {
            return type.newInstance();
        }
    }

    public static String getString(String key) {
        return ResourceBundle.getBundle("modulestrings").getString(key);
    }

    // These modules are passed to HierarchyNavigatorActivity and inject it project specific data and hierarchy information
    public static class BiokoActivityModule implements NavigatePluginModule {

        private static final Map<String, List<FormBehavior>> formsForStates = new HashMap<>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<>();

        static {

            List<FormBehavior> individualForms = new ArrayList<>();

            individualForms.add(new FormBehavior("bed_net",
                    "bioko.bednetsLabel",
                    new BiokoFormFilters.DistributeBednets(),
                    new BiokoFormPayloadBuilders.DistributeBednets(),
                    new BiokoFormPayloadConsumers.DistributeBednets()));

            individualForms.add(new FormBehavior("spraying",
                    "bioko.sprayingLabel",
                    new BiokoFormFilters.SprayHousehold(),
                    new BiokoFormPayloadBuilders.SprayHousehold(),
                    new BiokoFormPayloadConsumers.SprayHousehold()));

            individualForms.add(new FormBehavior("super_ojo",
                    "bioko.superOjoLabel",
                    new BiokoFormFilters.SuperOjo(),
                    new BiokoFormPayloadBuilders.SuperOjo(),
                    new BiokoFormPayloadConsumers.SuperOjo()));

            individualForms.add(new FormBehavior("duplicate_location",
                    "bioko.duplicateLocationLabel",
                    new BiokoFormFilters.DuplicateLocation(),
                    new BiokoFormPayloadBuilders.DuplicateLocation(),
                    new BiokoFormPayloadConsumers.DuplicateLocation()));

            formsForStates.put(INDIVIDUAL_STATE, individualForms);

            // these details are off by 1: details for an individual should be
            // shown when you click a specific individual which is technically
            // in the bottom state.
            detailFragsForStates.put(BOTTOM_STATE,  new IndividualDetailFragment());
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

        @Override
        public HierarchyInfo getHierarchyInfo() {
            return BiokoHierarchy.INSTANCE;
        }

        @Override
        public List<FormBehavior> getFormsForState(String state) {
            List<FormBehavior> formsForState = formsForStates.get(state);
            if (formsForState == null)
                formsForState = Collections.emptyList();
            return formsForState;
        }

        @Override
        public Map<String, DetailFragment> getDetailFragsForStates() {
            return detailFragsForStates;
        }
    }

    public static class CensusActivityModule implements NavigatePluginModule {

        private static final Map<String, List<FormBehavior>> formsForStates = new HashMap<>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<>();

        public static FormBehavior visitPregObFormBehavior;

        public static FormBehavior pregObFormBehavior;

        public static FormBehavior addLocationFormBehavior;

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

        static {

            visitPregObFormBehavior = new FormBehavior("visit",
                    "shared.visitLabel",
                    new UpdateFormFilters.StartAVisit(),
                    new UpdateFormPayloadBuilders.StartAVisit(),
                    new CensusFormPayloadConsumers.ChainedVisitForPregnancyObservation());

            pregObFormBehavior = new FormBehavior("pregnancy_observation",
                    "shared.pregnancyObservationLabel",
                    new UpdateFormFilters.RecordPregnancyObservation(),
                    new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                    new CensusFormPayloadConsumers.ChainedPregnancyObservation());

            addLocationFormBehavior = new FormBehavior("location",
                    "census.locationLabel",
                    new CensusFormFilters.AddLocation(),
                    new CensusFormPayloadBuilders.AddLocation(),
                    new CensusFormPayloadConsumers.AddLocation());

            ArrayList<FormBehavior> householdFormList = new ArrayList<>();
            ArrayList<FormBehavior> individualFormList = new ArrayList<>();

            householdFormList.add(addLocationFormBehavior);

            individualFormList.add(new FormBehavior("location_evaluation",
                    "census.evaluateLocationLabel",
                    new CensusFormFilters.EvaluateLocation(),
                    new CensusFormPayloadBuilders.EvaluateLocation(),
                    new CensusFormPayloadConsumers.EvaluateLocation()));

            individualFormList.add(new FormBehavior("individual",
                    "census.headOfHousholdLabel",
                    new CensusFormFilters.AddHeadOfHousehold(),
                    new CensusFormPayloadBuilders.AddHeadOfHousehold(),
                    new CensusFormPayloadConsumers.AddHeadOfHousehold()));

            individualFormList.add(new FormBehavior("individual",
                    "census.householdMemberLabel",
                    new CensusFormFilters.AddMemberOfHousehold(),
                    new CensusFormPayloadBuilders.AddMemberOfHousehold(),
                    new CensusFormPayloadConsumers.AddMemberOfHousehold()));

            formsForStates.put(HOUSEHOLD_STATE, householdFormList);
            formsForStates.put(INDIVIDUAL_STATE, individualFormList);

            // these details are off by 1: details for an individual should be
            // shown when you click a specific individual which is technically
            // in the bottom state.
            detailFragsForStates.put(BOTTOM_STATE, new IndividualDetailFragment());
        }

        @Override
        public Map<String, DetailFragment> getDetailFragsForStates() {
            return detailFragsForStates;
        }

        @Override
        public HierarchyInfo getHierarchyInfo() {
            return BiokoHierarchy.INSTANCE;
        }

        @Override
        public List<FormBehavior> getFormsForState(String state) {
            List<FormBehavior> formsForState = formsForStates.get(state);
            if (formsForState == null)
                formsForState = Collections.emptyList();
            return formsForState;
        }
    }

    public static class UpdateActivityModule implements NavigatePluginModule {

        private static final Map<String, List<FormBehavior>> formsForStates = new HashMap<>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<>();

        public static FormBehavior externalInMigrationFormBehavior;


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

        static {

            ArrayList<FormBehavior> individualFormList = new ArrayList<>();
            ArrayList<FormBehavior> bottomFormList = new ArrayList<>();

            // Start a Visit FormBehavior
            individualFormList.add(new FormBehavior("visit",
                    "shared.visitLabel",
                    new UpdateFormFilters.StartAVisit(),
                    new UpdateFormPayloadBuilders.StartAVisit(),
                    new UpdateFormPayloadConsumers.StartAVisit()));

            // Register an Internal Inmigration, requires a search to do
            ArrayList<FormSearchPluginModule> searches = new ArrayList<>();
            searches.add(SearchUtils.getIndividualPlugin(ProjectFormFields.Individuals.INDIVIDUAL_UUID, R.string.search_individual_label));
            individualFormList.add(new FormBehavior("in_migration",
                    "update.internalInMigrationLabel",
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.RegisterInternalInMigration(),
                    new UpdateFormPayloadConsumers.RegisterInMigration(),
                    searches));

            // Register an External InMigration form (chained after individual form)
            externalInMigrationFormBehavior = new FormBehavior("in_migration",
                    "update.externalInMigrationLabel",
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.RegisterExternalInMigration(),
                    new UpdateFormPayloadConsumers.RegisterInMigration());

            // Register an Individual for External InMigration (chained with in_migration form)
            individualFormList.add(new FormBehavior("individual",
                    "update.externalInMigrationLabel",
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.AddIndividualFromInMigration(),
                    new UpdateFormPayloadConsumers.AddIndividualFromInMigration()));

            // Register an OutMigration FormBehavior
            bottomFormList.add(new FormBehavior("out_migration",
                    "update.outMigrationLabel",
                    new UpdateFormFilters.RegisterOutMigration(),
                    new UpdateFormPayloadBuilders.RegisterOutMigration(),
                    new UpdateFormPayloadConsumers.RegisterOutMigration()));

            // Register a Death FormBehavior
            bottomFormList.add(new FormBehavior("death",
                    "update.deathLabel",
                    new UpdateFormFilters.RegisterDeath(),
                    new UpdateFormPayloadBuilders.RegisterDeath(),
                    new UpdateFormPayloadConsumers.RegisterDeath()));

            // Register a Pregnancy Observation FormBehavior
            bottomFormList.add(new FormBehavior("pregnancy_observation",
                    "shared.pregnancyObservationLabel",
                    new UpdateFormFilters.RecordPregnancyObservation(),
                    new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                    null));

            // Register a Pregnancy OutCome FormBehavior
            ArrayList<FormSearchPluginModule> daddySearch = new ArrayList<>();
            daddySearch.add(SearchUtils.getIndividualPlugin(ProjectFormFields.PregnancyOutcome.FATHER_UUID, R.string.search_father_label));
            bottomFormList.add(new FormBehavior("pregnancy_outcome",
                    "update.pregnancyOutcomeLabel",
                    new UpdateFormFilters.RecordPregnancyOutcome(),
                    new UpdateFormPayloadBuilders.RecordPregnancyOutcome(),
                    null,
                    daddySearch));

            formsForStates.put(INDIVIDUAL_STATE, individualFormList);
            formsForStates.put(BOTTOM_STATE, bottomFormList);

            // these details are off by 1: details for an individual should be
            // shown when you click a specific individual which is technically
            // in the bottom state.
            detailFragsForStates.put(BOTTOM_STATE, new IndividualDetailFragment());
        }

        @Override
        public Map<String, DetailFragment> getDetailFragsForStates() {
            return detailFragsForStates;
        }

        @Override
        public HierarchyInfo getHierarchyInfo() {
            return BiokoHierarchy.INSTANCE;
        }

        @Override
        public List<FormBehavior> getFormsForState(String state) {
            List<FormBehavior> formsForState = formsForStates.get(state);
            if (formsForState == null)
                formsForState = Collections.emptyList();
            return formsForState;
        }
    }

    public static final class FormType {

        private static final Map<String, String> labels = new HashMap<>();

        static {
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
                labels.put(mapping[0], mapping[1]);
            }
        }

        public static String getLabel(String formId) {
            if (labels.containsKey(formId)) {
                return getString(labels.get(formId));
            } else {
                return formId;
            }
        }
    }
}
