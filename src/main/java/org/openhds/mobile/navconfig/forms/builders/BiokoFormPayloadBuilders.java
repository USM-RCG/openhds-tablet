package org.openhds.mobile.navconfig.forms.builders;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.navconfig.ProjectFormFields;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.UsedByJSConfig;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.navconfig.BiokoHierarchy.*;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatBuilding;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatFloor;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatTime;

public class BiokoFormPayloadBuilders {

    @UsedByJSConfig
    public static class DistributeBednets implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            //rename the current datetime to be the bednet's distribution datetime
            String distributionDateTime = formPayload.get(ProjectFormFields.General.COLLECTION_DATE_TIME);
            formPayload.put(ProjectFormFields.General.DISTRIBUTION_DATE_TIME, distributionDateTime);

            String locationExtId = ctx.getHierarchyPath().get(HOUSEHOLD).getExtId();
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD).getUuid();

            formPayload.put(ProjectFormFields.BedNet.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.BedNet.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

            //pre-fill a netCode in YY-CCC form
            String netCode = generateNetCode(ctx, locationUuid);
            formPayload.put(ProjectFormFields.BedNet.BED_NET_CODE, netCode);

            //pre-fill the householdSize for this particular household
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = ctx.getContentResolver();
            List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findByResidency(locationUuid));
            String householdSize = Integer.toString(individuals.size());
            formPayload.put(ProjectFormFields.BedNet.HOUSEHOLD_SIZE, householdSize);

            return formPayload;
        }

        public String generateNetCode(LaunchContext ctx, String locationUuid) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location location = locationGateway.getFirst(ctx.getContentResolver(), locationGateway.findById(locationUuid));

            String communityCode = location.getCommunityCode();
            String yearPrefix = Integer.toString (Calendar.getInstance().get(Calendar.YEAR));
            yearPrefix = yearPrefix.substring(2);

            return yearPrefix + "-" + communityCode;
        }

    }

    @UsedByJSConfig
    public static class SprayHousehold implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            FieldWorker fieldWorker = ctx.getCurrentFieldWorker();
            formPayload.put(ProjectFormFields.SprayHousehold.SUPERVISOR_EXT_ID, fieldWorker.getExtId());
            formPayload.put(ProjectFormFields.SprayHousehold.SURVEY_DATE, formatTime(Calendar.getInstance()));


            String locationExtId = ctx.getHierarchyPath().get(HOUSEHOLD).getExtId();
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD).getUuid();
            formPayload.put(ProjectFormFields.BedNet.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.BedNet.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class SuperOjo implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            FieldWorker fieldWorker = ctx.getCurrentFieldWorker();
            formPayload.put(ProjectFormFields.SprayHousehold.SUPERVISOR_EXT_ID, fieldWorker.getExtId());
            formPayload.put(ProjectFormFields.SuperOjo.OJO_DATE, formatTime(Calendar.getInstance()));


            String locationExtId = ctx.getHierarchyPath().get(HOUSEHOLD).getExtId();
            String locationUuid = ctx.getHierarchyPath().get(HOUSEHOLD).getUuid();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class DuplicateLocation implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String,String> formPayload = new HashMap<>();

            ContentResolver contentResolver = ctx.getContentResolver();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);

            DataWrapper mapArea = ctx.getHierarchyPath().get(MAP_AREA);
            DataWrapper sector = ctx.getHierarchyPath().get(SECTOR);
            DataWrapper household = ctx.getHierarchyPath().get(HOUSEHOLD);

            String locationExtId = household.getExtId();
            String locationUuid = household.getUuid();

            // Assign the next sequential building number in sector
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location existing = locationGateway.getFirst(contentResolver,locationGateway.findById(locationUuid));
            int nextBuildingNumber = locationGateway.nextBuildingNumberInSector(
                    ctx.getApplicationContext(), mapArea.getName(), sector.getName());

            formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, mapArea.getName());
            formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());
            formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, formatBuilding(nextBuildingNumber, true));
            formPayload.put(ProjectFormFields.Locations.FLOOR_NUMBER, formatFloor(1, true));
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);
            formPayload.put(ProjectFormFields.Locations.DESCRIPTION, existing.getDescription());

            return formPayload;
        }
    }
}
