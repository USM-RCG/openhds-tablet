package org.openhds.mobile.projectdata.FormPayloadConsumers;

import android.content.ContentResolver;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.projectdata.FormAdapters.IndividualFormAdapter;
import org.openhds.mobile.projectdata.FormAdapters.LocationFormAdapter;
import org.openhds.mobile.projectdata.FormAdapters.VisitFormAdapter;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;
import org.openhds.mobile.utilities.IdHelper;

import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.projectdata.BiokoHierarchy.HOUSEHOLD_STATE;
import static org.openhds.mobile.projectdata.BiokoHierarchy.SECTOR_STATE;

public class CensusFormPayloadConsumers {

    private static void ensureLocationSectorExists(Map<String, String> formPayload, ContentResolver contentResolver) {

        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        LocationHierarchy mapArea = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(formPayload.get(ProjectFormFields.Locations.HIERERCHY_PARENT_UUID)));
        String sectorName =  formPayload.get(ProjectFormFields.Locations.SECTOR_NAME);
        String sectorExtId = mapArea.getExtId() + sectorName;
        LocationHierarchy sector = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findByExtId(sectorExtId));

        if(sector == null){
            sector = new LocationHierarchy();
            sector.setUuid(IdHelper.generateEntityUuid());
            sector.setParentUuid(mapArea.getUuid());
            sector.setExtId(sectorExtId);
            sector.setName(sectorName);
            sector.setLevel(SECTOR_STATE);
            locationHierarchyGateway.insertOrUpdate(contentResolver,sector);

            // Modify the payload to refer to the created sector
            formPayload.put(ProjectFormFields.General.NEEDS_REVIEW, ProjectResources.General.FORM_NEEDS_REVIEW);
            formPayload.put(ProjectFormFields.Locations.HIERERCHY_UUID, sector.getUuid());
            formPayload.put(ProjectFormFields.Locations.HIERERCHY_PARENT_UUID, sector.getParentUuid());
            formPayload.put(ProjectFormFields.Locations.HIERERCHY_EXTID, sector.getExtId());
        }
    }

    private static Location insertOrUpdateLocation(Map<String, String> formPayload, ContentResolver contentResolver) {
        Location location = LocationFormAdapter.fromForm(formPayload);
        GatewayRegistry.getLocationGateway().insertOrUpdate(contentResolver, location);
        return location;
    }

    private static Individual insertOrUpdateIndividual(
            Map<String, String> formPayLoad, NavigateActivity navigateActivity) {

        Individual individual = IndividualFormAdapter.fromForm(formPayLoad);
        individual.setEndType(ProjectResources.Individual.RESIDENCY_END_TYPE_NA);

        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        individualGateway.insertOrUpdate(contentResolver, individual);

        return individual;
    }

    public static class AddLocation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            ensureLocationSectorExists(formPayload, contentResolver);
            insertOrUpdateLocation(formPayload, contentResolver);
            return new ConsumerResults(true, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, formPayload.get(ProjectFormFields.Locations.LOCATION_EXTID));
        }
    }

    public static class EvaluateLocation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

            location.setLocationEvaluationStatus(formPayload.get(ProjectFormFields.Locations.EVALUATION));

            locationGateway.insertOrUpdate(navigateActivity.getContentResolver(), location);

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {

        }
    }

    public static class AddMemberOfHousehold implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {

            Map<String, DataWrapper> hierarchyPath = navigateActivity
                    .getHierarchyPath();
            DataWrapper selectedLocation = hierarchyPath
                    .get(HOUSEHOLD_STATE);

            String relationshipType = formPayload
                    .get(ProjectFormFields.Individuals.RELATIONSHIP_TO_HEAD);
            Individual individual = insertOrUpdateIndividual(formPayload,
                    navigateActivity);
            String startDate = formPayload
                    .get(ProjectFormFields.General.COLLECTION_DATE_TIME);

            SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();

            // get head of household by household id
            SocialGroup socialGroup = socialGroupGateway.getFirst(contentResolver,
                    socialGroupGateway.findByLocationUuid(selectedLocation.getUuid()));


            Individual currentHeadOfHousehold = individualGateway.getFirst(contentResolver,
                    individualGateway.findById(socialGroup.getGroupHeadUuid()));

            // INSERT or UPDATE RELATIONSHIP
            RelationshipGateway relationshipGateway = GatewayRegistry.getRelationshipGateway();
            Relationship relationship = new Relationship(individual, currentHeadOfHousehold, relationshipType, startDate, formPayload.get(ProjectFormFields.Individuals.RELATIONSHIP_UUID));
            relationshipGateway.insertOrUpdate(contentResolver, relationship);

            // INSERT or UPDATE MEMBERSHIP
            MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
            Membership membership = new Membership(individual, socialGroup, relationshipType, formPayload.get(ProjectFormFields.Individuals.MEMBERSHIP_UUID));
            membershipGateway.insertOrUpdate(contentResolver, membership);

            ConsumerResults pregnantResults;
            if(null !=  (pregnantResults = checkIfPregnant(formPayload))){
                return pregnantResults;
            }
            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }

    public static class AddHeadOfHousehold implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {

            Map<String, DataWrapper> hierarchyPath = navigateActivity
                    .getHierarchyPath();
            DataWrapper selectedLocation = hierarchyPath
                    .get(HOUSEHOLD_STATE);

            // head of the household is always "self" to the head of household
            String relationshipType = "1";

            // Pull out useful strings from the formPayload
            String startDate = formPayload
                    .get(ProjectFormFields.General.COLLECTION_DATE_TIME);
            Individual individual = insertOrUpdateIndividual(formPayload,
                    navigateActivity);

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();

            // Update the name of the location
            Location location = locationGateway.getFirst(contentResolver,
                    locationGateway.findById(selectedLocation.getUuid()));
            String locationName = individual.getLastName();
            location.setName(locationName);
            selectedLocation.setName(locationName);
            locationGateway.insertOrUpdate(contentResolver, location);

            // create social group
            SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
            SocialGroup socialGroup = new SocialGroup(selectedLocation.getUuid(), selectedLocation.getExtId(), individual, formPayload.get(ProjectFormFields.Individuals.SOCIALGROUP_UUID));
            socialGroupGateway.insertOrUpdate(contentResolver, socialGroup);

            // create membership
            MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
            Membership membership = new Membership(individual, socialGroup, relationshipType, formPayload.get(ProjectFormFields.Individuals.MEMBERSHIP_UUID));
            membershipGateway.insertOrUpdate(contentResolver, membership);

            // Set head of household's relationship to himself.
            RelationshipGateway relationshipGateway = GatewayRegistry.getRelationshipGateway();
            Relationship relationship = new Relationship(individual, individual, relationshipType, startDate, formPayload.get(ProjectFormFields.Individuals.RELATIONSHIP_UUID));
            relationshipGateway.insertOrUpdate(contentResolver, relationship);

            ConsumerResults pregnantResults;
            if(null !=  (pregnantResults = checkIfPregnant(formPayload))){
                return pregnantResults;
            }
            return new ConsumerResults(true, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // head of the household is always "self" to the head of household
            formPayload.put(ProjectFormFields.Individuals.MEMBER_STATUS, "1");
        }

    }

    public static class EditIndividual implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload,
                                                  NavigateActivity navigateActivity) {
            new AddMemberOfHousehold().consumeFormPayload(formPayload,
                    navigateActivity);
            return new ConsumerResults(false, null, null);

        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }

    }

    private static ConsumerResults checkIfPregnant(Map<String,String> formPayload){

        String pregnant = formPayload.get(ProjectFormFields.Individuals.IS_PREGNANT_FLAG);

        if(null != pregnant && pregnant.equals("Yes")) {
            Map<String, String> hints = new HashMap<>();
            hints.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));
            hints.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, formPayload.get(ProjectFormFields.General.ENTITY_UUID));
            hints.put(ProjectFormFields.Locations.LOCATION_EXTID, formPayload.get(ProjectFormFields.General.HOUSEHOLD_STATE_FIELD_NAME));
            return new ConsumerResults(false, ProjectActivityBuilder.CensusActivityModule.visitPregObFormBehaviour, hints);
        }

        return null;
    }

    // Used for Form Launch Sequences
    public static class ChainedVisitForPregnancyObservation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            Visit visit = VisitFormAdapter.fromForm(formPayload);

            VisitGateway visitGateway = GatewayRegistry.getVisitGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            visitGateway.insertOrUpdate(contentResolver, visit);

            navigateActivity.startVisit(visit);


            navigateActivity.getPreviousConsumerResults().getFollowUpFormHints().put(ProjectFormFields.General.ENTITY_UUID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_UUID));
            navigateActivity.getPreviousConsumerResults().getFollowUpFormHints().put(ProjectFormFields.General.ENTITY_EXTID, formPayload.get(ProjectFormFields.Individuals.INDIVIDUAL_EXTID));

            return new ConsumerResults(false, ProjectActivityBuilder.CensusActivityModule.pregObFormBehaviour, navigateActivity.getPreviousConsumerResults().getFollowUpFormHints());
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {
            // TODO Auto-generated method stub

        }
    }

    public static class ChainedPregnancyObservation implements FormPayloadConsumer {

        @Override
        public ConsumerResults consumeFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            // Since this is happening as part of a sequence it made sense to me to automatically close the
            // visit after completion of the sequence.
            navigateActivity.finishVisit();

            return new ConsumerResults(false, null, null);
        }

        @Override
        public void postFillFormPayload(Map<String, String> formPayload) {

        }

    }
}
