package org.openhds.mobile.navconfig.forms.builders;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.*;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.UsedByJSConfig;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.navconfig.ProjectFormFields;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.*;
import org.openhds.mobile.utilities.IdHelper;
import org.openhds.mobile.utilities.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.navconfig.BiokoHierarchy.*;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatBuilding;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatFloor;

public class CensusFormPayloadBuilders {

    private static void addNewLocationPayload(Map<String, String> formPayload, LaunchContext ctx) {

        ContentResolver contentResolver = ctx.getContentResolver();
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

        DataWrapper sectorDataWrapper = ctx.getHierarchyPath().get(SECTOR);

        LocationHierarchy sector = locationHierarchyGateway.getFirst(contentResolver, locationHierarchyGateway.findById(sectorDataWrapper.getUuid()));
        LocationHierarchy mapArea = locationHierarchyGateway.getFirst(contentResolver, locationHierarchyGateway.findById(sector.getParentUuid()));
        LocationHierarchy locality = locationHierarchyGateway.getFirst(contentResolver, locationHierarchyGateway.findById(mapArea.getParentUuid()));

        int nextBuildingNumber = locationGateway.nextBuildingNumberInSector(ctx.getApplicationContext(), mapArea.getName(), sector.getName());

        formPayload.put(ProjectFormFields.General.ENTITY_UUID, IdHelper.generateEntityUuid());
        formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, formatBuilding(nextBuildingNumber, false));
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_EXTID, sector.getExtId());
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_UUID, sector.getUuid());
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_PARENT_UUID, sector.getParentUuid());
        formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());
        formPayload.put(ProjectFormFields.Locations.FLOOR_NUMBER, formatFloor(1, false));
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

    @UsedByJSConfig
    public static class AddLocation implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            addNewLocationPayload(formPayload, ctx);
            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class LocationEvaluation implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            DataWrapper household = ctx.getHierarchyPath().get(HOUSEHOLD);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, household.getExtId());
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, household.getUuid());
            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class AddMemberOfHousehold implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String,String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            addNewIndividualPayload(formPayload, ctx);

            DataWrapper household = ctx.getHierarchyPath().get(HOUSEHOLD);

            ContentResolver resolver = ctx.getContentResolver();
            IndividualGateway individualGateway = new IndividualGateway();

            List<Individual> residents = individualGateway.getList(
                    resolver, individualGateway.findByResidency(household.getUuid()));

            // pre-fill contact name and number as best we can without household role info
            if (residents.size() == 1) {
                Individual head = residents.get(0);
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, Individual.getFullName(head));
                formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, head.getPhoneNumber());
            } else {
                for (Individual resident : residents) {
                    String contactName = resident.getPointOfContactName(),
                            contactNumber = resident.getPointOfContactPhoneNumber();
                    if (!StringUtils.isEmpty(contactName) && !StringUtils.isEmpty(contactNumber)) {
                        formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, contactName);
                        formPayload.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, contactNumber);
                        break;
                    }
                }
            }

            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class AddHeadOfHousehold implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            addNewIndividualPayload(formPayload, ctx);
            formPayload.put(ProjectFormFields.Individuals.HEAD_PREFILLED_FLAG, "true");
            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class Fingerprints implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            DataWrapper individual = ctx.getHierarchyPath().get(INDIVIDUAL);
            formPayload.put(ProjectFormFields.Fingerprints.INDIVIDUAL_UUID, individual.getUuid());
            return formPayload;
        }
    }
}
