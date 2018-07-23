package org.openhds.mobile.navconfig.forms.builders;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.navconfig.HierarchyPath;
import org.openhds.mobile.navconfig.ProjectFormFields;
import org.openhds.mobile.navconfig.forms.LaunchContext;
import org.openhds.mobile.navconfig.forms.UsedByJSConfig;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.utilities.IdHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.navconfig.BiokoHierarchy.*;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatBuilding;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatDate;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatFloor;
import static org.openhds.mobile.navconfig.forms.builders.PayloadTools.formatTime;

public class BiokoFormPayloadBuilders {

    @UsedByJSConfig
    public static class DistributeBednets implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);

            // Re-use the collection time to be the bednet's distribution datetime
            String distributionDateTime = formPayload.get(ProjectFormFields.General.COLLECTION_DATE_TIME);
            formPayload.put(ProjectFormFields.General.DISTRIBUTION_DATE_TIME, distributionDateTime);

            DataWrapper location = ctx.getHierarchyPath().get(HOUSEHOLD);
            String locationExtId = location.getExtId();
            String locationUuid = location.getUuid();

            formPayload.put(ProjectFormFields.BedNet.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.BedNet.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

            formPayload.put(ProjectFormFields.BedNet.BED_NET_CODE, generateNetCode(ctx));

            // pre-fill the householdSize for this particular household
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = ctx.getContentResolver();
            List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findByResidency(locationUuid));
            String householdSize = Integer.toString(individuals.size());
            formPayload.put(ProjectFormFields.BedNet.HOUSEHOLD_SIZE, householdSize);

            return formPayload;
        }

        private String generateNetCode(LaunchContext ctx) {
            HierarchyPath hierPath = ctx.getHierarchyPath();
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            ContentResolver contentResolver = ctx.getContentResolver();
            Location household = locationGateway.getFirst(contentResolver, locationGateway.findById(hierPath.get(HOUSEHOLD).getUuid()));
            String map = hierPath.get(MAP_AREA).getName(), sector = hierPath.get(SECTOR).getName();
            String year = new SimpleDateFormat("yy").format(new Date());
            return String.format("%s/%s%sE%03d", year, map, sector, household.getBuildingNumber());
        }
    }

    @UsedByJSConfig
    public static class SprayHousehold implements FormPayloadBuilder {

        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String, String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);

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

    @UsedByJSConfig
    public static class CreateMap implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            DataWrapper locality = ctx.getHierarchyPath().get(LOCALITY);
            formPayload.put(ProjectFormFields.CreateMap.LOCALITY_UUID, locality.getUuid());
            formPayload.put(ProjectFormFields.CreateMap.MAP_UUID, IdHelper.generateEntityUuid());
            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class CreateSector implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            DataWrapper map = ctx.getHierarchyPath().get(MAP_AREA);
            formPayload.put(ProjectFormFields.CreateSector.MAP_UUID, map.getUuid());
            formPayload.put(ProjectFormFields.CreateSector.SECTOR_UUID, IdHelper.generateEntityUuid());
            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class MalariaIndicatorSurvey implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String, String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            PayloadTools.flagForReview(formPayload, false);
            DataWrapper household = ctx.getHierarchyPath().get(HOUSEHOLD);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, household.getExtId());
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, household.getUuid());
            formPayload.put(ProjectFormFields.MalariaIndicatorSurvey.SURVEY_DATE, formatDate(Calendar.getInstance()));
            return formPayload;
        }
    }

    @UsedByJSConfig
    public static class Sbcc implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {

            Map<String,String> formPayload = new HashMap<>();

            PayloadTools.addMinimalFormPayload(formPayload, ctx);

            HierarchyPath path = ctx.getHierarchyPath();
            DataWrapper household = path.get(HOUSEHOLD), individual = path.get(INDIVIDUAL);

            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, household.getExtId());
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, household.getUuid());

            if (individual != null) {
                formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individual.getExtId());
                formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, individual.getUuid());
            }

            return formPayload;
        }
    }
}
