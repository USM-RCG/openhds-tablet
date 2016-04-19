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
import org.openhds.mobile.projectdata.QueryHelpers.CensusQueryHelper;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // These modules are passed to HierarchyNavigatorActivity and inject it project specific data and hierarchy information
    public static class BiokoActivityModule implements NavigatePluginModule {

        private static final Map<String, List<FormBehavior>> formsForStates = new HashMap<>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<>();
        public static class BiokoUiHelper implements ModuleUiHelper {
            @Override
            public int getModuleLabelStringId() {
                return R.string.bioko_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.bioko_portal_description;
            }

            @Override
            public int getModulePortalDrawableId() {
                return R.drawable.bioko_hierarchy_selector;
            }

            @Override
            public int getModuleTitleStringId() {
                return R.string.bioko_activity_title;
            }

            @Override
            public int getDataSelectionDrawableId() {
                return R.drawable.bioko_data_selector;
            }

            @Override
            public int getHierarchySelectionDrawableId() {
                return R.drawable.bioko_hierarchy_selector;
            }
        }

        static {

            List<FormBehavior> individualForms = new ArrayList<>();

            individualForms.add(new FormBehavior("bed_net",
                    R.string.distribute_bednets,
                    new BiokoFormFilters.DistributeBednets(),
                    new BiokoFormPayloadBuilders.DistributeBednets(),
                    new BiokoFormPayloadConsumers.DistributeBednets()));

            individualForms.add(new FormBehavior("spraying",
                    R.string.spray_household,
                    new BiokoFormFilters.SprayHousehold(),
                    new BiokoFormPayloadBuilders.SprayHousehold(),
                    new BiokoFormPayloadConsumers.SprayHousehold()));

            individualForms.add(new FormBehavior("super_ojo",
                    R.string.super_ojo,
                    new BiokoFormFilters.SuperOjo(),
                    new BiokoFormPayloadBuilders.SuperOjo(),
                    new BiokoFormPayloadConsumers.SuperOjo()));

            individualForms.add(new FormBehavior("duplicate_location",
                    R.string.duplicate_location,
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
        public QueryHelper getQueryHelper() {
            return new CensusQueryHelper();
        }

        @Override
        public ModuleUiHelper getModuleUiHelper() {
            return new BiokoUiHelper();
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
        public static class CensusUiHelper implements ModuleUiHelper {

            @Override
            public int getModuleLabelStringId() {
                return R.string.census_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.census_portal_description;
            }

            @Override
            public int getModulePortalDrawableId() {
                return R.drawable.census_hierarchy_selector;
            }

            @Override
            public int getModuleTitleStringId() {
                return R.string.census_activity_title;
            }

            @Override
            public int getDataSelectionDrawableId() {
                return R.drawable.census_data_selector;
            }

            @Override
            public int getHierarchySelectionDrawableId() {
                return R.drawable.census_hierarchy_selector;
            }
        }

        public static FormBehavior visitPregObFormBehavior;

        public static FormBehavior pregObFormBehavior;

        public static FormBehavior addLocationFormBehavior;

        static {

            visitPregObFormBehavior = new FormBehavior("visit",
                    R.string.start_a_visit,
                    new UpdateFormFilters.StartAVisit(),
                    new UpdateFormPayloadBuilders.StartAVisit(),
                    new CensusFormPayloadConsumers.ChainedVisitForPregnancyObservation());

            pregObFormBehavior = new FormBehavior("pregnancy_observation",
                    R.string.record_pregnancy_observation,
                    new UpdateFormFilters.RecordPregnancyObservation(),
                    new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                    new CensusFormPayloadConsumers.ChainedPregnancyObservation());

            addLocationFormBehavior = new FormBehavior("location",
                    R.string.create_location,
                    new CensusFormFilters.AddLocation(),
                    new CensusFormPayloadBuilders.AddLocation(),
                    new CensusFormPayloadConsumers.AddLocation());

            ArrayList<FormBehavior> householdFormList = new ArrayList<>();
            ArrayList<FormBehavior> individualFormList = new ArrayList<>();

            householdFormList.add(addLocationFormBehavior);

            individualFormList.add(new FormBehavior("location_evaluation",
                    R.string.evaluate_location_label,
                    new CensusFormFilters.EvaluateLocation(),
                    new CensusFormPayloadBuilders.EvaluateLocation(),
                    new CensusFormPayloadConsumers.EvaluateLocation()));

            individualFormList.add(new FormBehavior("individual",
                    R.string.create_head_of_household_label,
                    new CensusFormFilters.AddHeadOfHousehold(),
                    new CensusFormPayloadBuilders.AddHeadOfHousehold(),
                    new CensusFormPayloadConsumers.AddHeadOfHousehold()));

            individualFormList.add(new FormBehavior("individual",
                    R.string.add_member_of_household_label,
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
        public QueryHelper getQueryHelper() {
            return new CensusQueryHelper();
        }

        @Override
        public ModuleUiHelper getModuleUiHelper() {
            return new CensusUiHelper();
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
        public static class UpdateUiHelper implements ModuleUiHelper {

            @Override
            public int getModuleLabelStringId() {
                return R.string.update_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.update_portal_description;
            }

            @Override
            public int getModulePortalDrawableId() {
                return R.drawable.update_hierarchy_selector;
            }

            @Override
            public int getModuleTitleStringId() {
                return R.string.update_activity_title;
            }

            @Override
            public int getDataSelectionDrawableId() {
                return R.drawable.update_data_selector;
            }

            @Override
            public int getHierarchySelectionDrawableId() {
                return R.drawable.update_hierarchy_selector;
            }

        }


        public static FormBehavior externalInMigrationFormBehavior;

        static {

            ArrayList<FormBehavior> individualFormList = new ArrayList<>();
            ArrayList<FormBehavior> bottomFormList = new ArrayList<>();

            // Start a Visit FormBehavior
            individualFormList.add(new FormBehavior("visit",
                    R.string.start_a_visit,
                    new UpdateFormFilters.StartAVisit(),
                    new UpdateFormPayloadBuilders.StartAVisit(),
                    new UpdateFormPayloadConsumers.StartAVisit()));

            // Register an Internal Inmigration, requires a search to do
            ArrayList<FormSearchPluginModule> searches = new ArrayList<>();
            searches.add(SearchUtils.getIndividualPlugin(ProjectFormFields.Individuals.INDIVIDUAL_UUID, R.string.search_individual_label));
            individualFormList.add(new FormBehavior("in_migration",
                    R.string.internal_in_migration,
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.RegisterInternalInMigration(),
                    new UpdateFormPayloadConsumers.RegisterInMigration(),
                    searches));


            // Register an External InMigration form (chained after individual form)
                    externalInMigrationFormBehavior = new FormBehavior("in_migration",
                    R.string.external_in_migration,
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.RegisterExternalInMigration(),
                    new UpdateFormPayloadConsumers.RegisterInMigration());


            // Register an Individual for External InMigration (chained with in_migration form)
            individualFormList.add(new FormBehavior("individual",
                    R.string.external_in_migration,
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.AddIndividualFromInMigration(),
                    new UpdateFormPayloadConsumers.AddIndividualFromInMigration()));

            // Register an OutMigration FormBehavior
            bottomFormList.add(new FormBehavior("out_migration",
                    R.string.out_migration,
                    new UpdateFormFilters.RegisterOutMigration(),
                    new UpdateFormPayloadBuilders.RegisterOutMigration(),
                    new UpdateFormPayloadConsumers.RegisterOutMigration()));

            // Register a Death FormBehavior
            bottomFormList.add(new FormBehavior("death",
                    R.string.register_death,
                    new UpdateFormFilters.RegisterDeath(),
                    new UpdateFormPayloadBuilders.RegisterDeath(),
                    new UpdateFormPayloadConsumers.RegisterDeath()));

            // Register a Pregnancy Observation FormBehavior
            bottomFormList.add(new FormBehavior("pregnancy_observation",
                    R.string.record_pregnancy_observation,
                    new UpdateFormFilters.RecordPregnancyObservation(),
                    new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                    null));

            // Register a Pregnancy OutCome FormBehavior
            ArrayList<FormSearchPluginModule> daddySearch = new ArrayList<>();
            daddySearch.add(SearchUtils.getIndividualPlugin(ProjectFormFields.PregnancyOutcome.FATHER_UUID, R.string.search_father_label));
            bottomFormList.add(new FormBehavior("pregnancy_outcome",
                    R.string.record_pregnancy_outcome,
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
        public QueryHelper getQueryHelper() {

            return new CensusQueryHelper();
        }

        @Override
        public ModuleUiHelper getModuleUiHelper() {
            return new UpdateUiHelper();
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
}
