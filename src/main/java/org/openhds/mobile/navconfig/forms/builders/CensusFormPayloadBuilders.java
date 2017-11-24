package org.openhds.mobile.navconfig.forms.builders;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.*;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.navconfig.ProjectFormFields;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.*;
import org.openhds.mobile.utilities.IdHelper;

import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.navconfig.BiokoHierarchy.*;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatBuilding;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatFloor;


public class CensusFormPayloadBuilders {

    private static void addNewLocationPayload(Map<String, String> formPayload, LaunchContext ctx) {
        ContentResolver contentResolver = ctx.getContentResolver();
        DataWrapper sectorDataWrapper = ctx.getHierarchyPath().get(SECTOR);
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        LocationHierarchy sector = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(sectorDataWrapper.getUuid()));
        LocationHierarchy mapArea = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(sector.getParentUuid()));
        LocationHierarchy locality = locationHierarchyGateway.getFirst(contentResolver,
                locationHierarchyGateway.findById(mapArea.getParentUuid()));
        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
        int nextBuildingNumber = locationGateway.nextBuildingNumberInSector(
                ctx.getApplicationContext(), mapArea.getName(), sector.getName());
        String[] communityNameAndCode = locationGateway.communityForSector(ctx.getApplicationContext(), sector.getUuid());
        formPayload.put(ProjectFormFields.General.ENTITY_UUID, IdHelper.generateEntityUuid());
        formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, formatBuilding(nextBuildingNumber, false));
        formPayload.put(ProjectFormFields.Locations.COMMUNITY_NAME, communityNameAndCode[0]);
        formPayload.put(ProjectFormFields.Locations.COMMUNITY_CODE, communityNameAndCode[1]);
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_EXTID, sector.getExtId());
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_UUID, sector.getUuid());
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_PARENT_UUID, sector.getParentUuid());
        formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());
        formPayload.put(ProjectFormFields.Locations.FLOOR_NUMBER, formatFloor(1, false));
        formPayload.put(ProjectFormFields.Locations.LOCALITY_NAME, locality.getName());
        formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, mapArea.getName());
    }

    private static void addNewIndividualPayload(Map<String, String> formPayload, LaunchContext navigateActivity) {
        DataWrapper locationDataWrapper = navigateActivity.getHierarchyPath().get(HOUSEHOLD);
        String individualExtId = IdHelper.generateIndividualExtId(navigateActivity.getContentResolver(), locationDataWrapper);
        formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);
        formPayload.put(ProjectFormFields.Individuals.HOUSEHOLD_UUID, navigateActivity.getCurrentSelection().getUuid());
        formPayload.put(ProjectFormFields.General.ENTITY_EXTID, individualExtId);
        formPayload.put(ProjectFormFields.General.ENTITY_UUID, IdHelper.generateEntityUuid());
    }

    public static class AddLocation implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);
            addNewLocationPayload(formPayload, ctx);
            return formPayload;
        }
    }

    public static class AddMemberOfHousehold implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);
            addNewIndividualPayload(formPayload, ctx);
            ContentResolver resolver = ctx.getContentResolver();
            SocialGroupGateway socialGroupGateway = new SocialGroupGateway();
            SocialGroup socialGroup = socialGroupGateway.getFirst(resolver,
                    socialGroupGateway.findByLocationUuid(ctx.getCurrentSelection().getUuid()));
            IndividualGateway individualGateway = new IndividualGateway();
            //HoH is found by searching by extId, since we're currently dependent on the groupHead property of socialgroup
            //Set as the individual's extId
            Individual headOfHousehold = individualGateway.getFirst(resolver, individualGateway.findById(socialGroup.getGroupHeadUuid()));
            // set's the member's point of contact info to the HoH
            if(null != headOfHousehold.getPhoneNumber() && !headOfHousehold.getPhoneNumber().isEmpty()) {
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, Individual.getFullName(headOfHousehold));
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, headOfHousehold.getPhoneNumber());
            } else {
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, headOfHousehold.getPointOfContactName());
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, headOfHousehold.getPointOfContactPhoneNumber());
            }
            formPayload.put(ProjectFormFields.Individuals.MEMBERSHIP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.SOCIALGROUP_UUID, socialGroup.getUuid());
            return formPayload;
        }
    }

    public static class AddHeadOfHousehold implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);
            addNewIndividualPayload(formPayload, ctx);
            // we need to add the socialgroup, membership, and relationship UUID for when they're created in
            // the consumers. We add them now so they are a part of the form when it is passed up.
            formPayload.put(ProjectFormFields.Individuals.SOCIALGROUP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.MEMBERSHIP_UUID, IdHelper.generateEntityUuid());
            formPayload.put(ProjectFormFields.Individuals.HEAD_PREFILLED_FLAG, "true");
            return formPayload;
        }
    }
}
