package org.openhds.mobile.navconfig.forms.builders;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.navconfig.ProjectFormFields;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.utilities.IdHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.navconfig.BiokoHierarchy.*;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatDate;

public class UpdateFormPayloadBuilders {

    /**
     *
     * Helper methods for FormPayloadBuilders
     *
     */

    public static class StartAVisit implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            String visitDate = formatDate(Calendar.getInstance());
            String locationExtId = ctx.getHierarchyPath().get(HOUSEHOLD_STATE).getExtId();
            String locationUuid= ctx.getHierarchyPath().get(HOUSEHOLD_STATE).getUuid();
            String visitExtId = visitDate + "_" + locationExtId;


            formPayload.put(ProjectFormFields.Visits.VISIT_DATE, visitDate);
            formPayload.put(ProjectFormFields.Visits.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.Visits.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, visitExtId);
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

            return formPayload;
        }
    }


    // Individual information must be post-filled in the consumer because it's obtained
    // through a search plugin
    public static class RegisterInternalInMigration implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, ctx.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, ctx.getCurrentVisit().getUuid());

            String locationExtId = ctx.getHierarchyPath().get(HOUSEHOLD_STATE).getExtId();
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD_STATE).getUuid();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);

            formPayload.put(ProjectFormFields.InMigrations.IN_MIGRATION_TYPE, ProjectFormFields.InMigrations.IN_MIGRATION_INTERNAL);

            String migrationDate = formatDate(Calendar.getInstance());
            formPayload.put(ProjectFormFields.InMigrations.IN_MIGRATION_DATE, migrationDate);

            return formPayload;
        }
    }

    // Individual information is chained in
    public static class RegisterExternalInMigration implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, ctx.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, ctx.getCurrentVisit().getUuid());

            String locationExtId = ctx.getHierarchyPath().get(HOUSEHOLD_STATE).getExtId();
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD_STATE).getUuid();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);

            formPayload.put(ProjectFormFields.InMigrations.IN_MIGRATION_TYPE, ProjectFormFields.InMigrations.IN_MIGRATION_EXTERNAL);

            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_UUID));

            String migrationDate = formatDate(Calendar.getInstance());
            formPayload.put(ProjectFormFields.InMigrations.IN_MIGRATION_DATE, migrationDate);

            return formPayload;
        }
    }

    public static class RegisterOutMigration implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext navigateActivity) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            String outMigrationDate = formatDate(Calendar.getInstance());

            String individualExtId = navigateActivity.getHierarchyPath().get(INDIVIDUAL_STATE).getExtId();
            String individualUuid = navigateActivity.getHierarchyPath().get(INDIVIDUAL_STATE).getUuid();

            formPayload.put(ProjectFormFields.OutMigrations.OUT_MIGRATION_DATE, outMigrationDate);

            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_UUID));

            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);
            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, individualUuid);

            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, individualExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, individualUuid);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, navigateActivity.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, navigateActivity.getCurrentVisit().getUuid());

            return formPayload;
        }
    }

    public static class RegisterDeath implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, true);

            String individualExtId;
            String individualUuid;
            DataWrapper dataWrapper = ctx.getHierarchyPath().get(INDIVIDUAL_STATE);
            if (null != dataWrapper) {
                individualExtId = dataWrapper.getExtId();
                individualUuid = dataWrapper.getUuid();
                formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, individualUuid);
                formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);
                formPayload.put(ProjectFormFields.General.ENTITY_UUID, individualUuid);
                formPayload.put(ProjectFormFields.General.ENTITY_EXTID, individualExtId);
            }

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, ctx.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, ctx.getCurrentVisit().getUuid());

            return formPayload;
        }
    }

    public static class RecordPregnancyObservation implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            String observationDate = formatDate(Calendar.getInstance());

            String individualExtId;
            String individualUuid;
            DataWrapper dataWrapper = ctx.getHierarchyPath().get(INDIVIDUAL_STATE);
            if (null == dataWrapper) {
                individualExtId = formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID);
                individualUuid = formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_UUID);
            } else {
                individualExtId = dataWrapper.getExtId();
                individualUuid = dataWrapper.getUuid();
                formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, individualUuid);
                formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);
            }

            formPayload.put(ProjectFormFields.General.ENTITY_UUID, individualUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, individualExtId);

            formPayload.put(ProjectFormFields.Visits.VISIT_EXTID, ctx.getCurrentVisit().getExtId());
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, ctx.getCurrentVisit().getUuid());

            formPayload.put(ProjectFormFields.PregnancyObservation.PREGNANCY_OBSERVATION_RECORDED_DATE, observationDate);

            return formPayload;
        }
    }

    public static class RecordPregnancyOutcome implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            SocialGroupGateway socialGroupGateway = new SocialGroupGateway();
            SocialGroup socialGroup = socialGroupGateway.getFirst(ctx.getContentResolver(),
                    socialGroupGateway.findByLocationUuid(ctx.getHierarchyPath().get(HOUSEHOLD_STATE).getUuid()));

            String motherExtId = ctx.getHierarchyPath().get(INDIVIDUAL_STATE).getExtId();
            String motherUuid = ctx.getHierarchyPath().get(INDIVIDUAL_STATE).getUuid();

            formPayload.put(ProjectFormFields.PregnancyOutcome.MOTHER_UUID, motherUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, motherUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, motherExtId);
            formPayload.put(ProjectFormFields.Visits.VISIT_UUID, ctx.getCurrentVisit().getUuid());
            formPayload.put(ProjectFormFields.PregnancyOutcome.SOCIALGROUP_UUID, socialGroup.getUuid());

            return formPayload;
        }
    }

    public static class AddIndividualFromInMigration implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext navigateActivity) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            DataWrapper locationDataWrapper = navigateActivity.getHierarchyPath().get(HOUSEHOLD_STATE);

            String individualExtId = IdHelper.generateIndividualExtId(navigateActivity.getContentResolver(), locationDataWrapper);

            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);

            formPayload.put(ProjectFormFields.Individuals.HOUSEHOLD_UUID, navigateActivity.getCurrentSelection().getUuid());

            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, individualExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, IdHelper.generateEntityUuid());

            ContentResolver resolver = navigateActivity.getContentResolver();

            SocialGroupGateway socialGroupGateway = new SocialGroupGateway();
            SocialGroup socialGroup = socialGroupGateway.getFirst(resolver,
                    socialGroupGateway.findByLocationUuid(navigateActivity.getCurrentSelection().getUuid()));


            // we need to add the socialgroup, membership, and relationship UUID for when they're created in
            // the consumers. We add them now so they are a part of the form when it is passed up.
            formPayload.put(ProjectFormFields.Individuals.RELATIONSHIP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.MEMBERSHIP_UUID, IdHelper.generateEntityUuid());

            if(null == socialGroup){
                formPayload.put(ProjectFormFields.Individuals.SOCIALGROUP_UUID, IdHelper.generateEntityUuid());
            } else {
                formPayload.put(ProjectFormFields.Individuals.SOCIALGROUP_UUID, socialGroup.getUuid());
            }

            return formPayload;
        }

    }
}
