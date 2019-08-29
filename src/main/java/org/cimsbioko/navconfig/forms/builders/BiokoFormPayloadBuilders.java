package org.cimsbioko.navconfig.forms.builders;

import org.cimsbioko.model.core.FieldWorker;
import org.cimsbioko.model.core.Individual;
import org.cimsbioko.model.core.Location;
import org.cimsbioko.navconfig.HierarchyPath;
import org.cimsbioko.navconfig.ProjectFormFields;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.GatewayRegistry;
import org.cimsbioko.repository.gateway.IndividualGateway;
import org.cimsbioko.repository.gateway.LocationGateway;
import org.cimsbioko.utilities.IdHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cimsbioko.navconfig.Hierarchy.*;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatBuilding;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatDate;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatFloor;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatTime;

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

            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

            formPayload.put(ProjectFormFields.BedNet.BED_NET_CODE, generateNetCode(ctx));

            // pre-fill the householdSize for this particular household
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            List<Individual> individuals = individualGateway.findByResidency(locationUuid).getList();
            String householdSize = Integer.toString(individuals.size());
            formPayload.put(ProjectFormFields.BedNet.HOUSEHOLD_SIZE, householdSize);

            return formPayload;
        }

        private String generateNetCode(LaunchContext ctx) {
            List<DataWrapper> hierPath = ctx.getHierarchyPath().getPath();
            int pathLen = hierPath.size();
            DataWrapper stubLocation = hierPath.get(pathLen - 1),
                    sector = hierPath.get(pathLen - 2),
                    map = hierPath.get(pathLen - 3);
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location household = locationGateway.findById(stubLocation.getUuid()).getFirst();
            String year = new SimpleDateFormat("yy").format(new Date());
            return String.format("%s/%s%sE%03d", year, map.getName(), sector.getName(), household.getBuildingNumber());
        }
    }

    @UsedByJSConfig
    public static class DefaultHousehold implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String, String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            DataWrapper household = ctx.getHierarchyPath().get(HOUSEHOLD);
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, household.getExtId());
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, household.getUuid());
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

            PayloadTools.addMinimalFormPayload(formPayload, ctx);

            List<DataWrapper> hierPath = ctx.getHierarchyPath().getPath();
            int pathLen = hierPath.size();
            DataWrapper household = hierPath.get(pathLen - 1),
                    sector = hierPath.get(pathLen - 2),
                    map = hierPath.get(pathLen - 3);

            String locationExtId = household.getExtId();
            String locationUuid = household.getUuid();

            // Assign the next sequential building number in sector
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location existing = locationGateway.findById(locationUuid).getFirst();
            int nextBuildingNumber = locationGateway.nextBuildingNumberInSector(map.getName(), sector.getName());

            formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, map.getName());
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
            List<DataWrapper> path = ctx.getHierarchyPath().getPath();
            DataWrapper locality = path.get(path.size()-1);
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
            List<DataWrapper> path = ctx.getHierarchyPath().getPath();
            DataWrapper map = path.get(path.size()-1);
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

    @UsedByJSConfig
    public static class Minimal implements FormPayloadBuilder {
        @Override
        public Map<String, String> buildPayload(LaunchContext ctx) {
            Map<String,String> formPayload = new HashMap<>();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            return formPayload;
        }
    }
}
