package org.openhds.mobile.projectdata.FormPayloadBuilders;

import android.content.ContentResolver;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.projectdata.BiokoHierarchy.*;

public class BiokoFormPayloadBuilders {

    public static class DistributeBednets implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            //rename the current datetime to be the bednet's distribution datetime
            String distributionDateTime = formPayload.get(ProjectFormFields.General.COLLECTION_DATE_TIME);
            formPayload.put(ProjectFormFields.General.DISTRIBUTION_DATE_TIME, distributionDateTime);

            String locationExtId = navigateActivity.getHierarchyPath()
                    .get(HOUSEHOLD_STATE).getExtId();
            String locationUuid = navigateActivity.getHierarchyPath()
                    .get(HOUSEHOLD_STATE).getUuid();
            formPayload.put(ProjectFormFields.BedNet.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.BedNet.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);

            //pre-fill a netCode in YY-CCC form
            String netCode = generateNetCode(navigateActivity, locationUuid);
            formPayload.put(ProjectFormFields.BedNet.BED_NET_CODE, netCode);

            //pre-fill the householdSize for this particular household
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            ContentResolver contentResolver = navigateActivity.getContentResolver();
            List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findByResidency(locationUuid));
            String householdSize = Integer.toString(individuals.size());
            formPayload.put(ProjectFormFields.BedNet.HOUSEHOLD_SIZE, householdSize);
        }

        public String generateNetCode(NavigateActivity navigateActivity, String locationUuid) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(locationUuid));

            String communityCode = location.getCommunityCode();
            String yearPrefix = Integer.toString (Calendar.getInstance().get(Calendar.YEAR));
            yearPrefix = yearPrefix.substring(2);

            return yearPrefix + "-" + communityCode;
        }

    }

    public static class SprayHousehold implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            FieldWorker fieldWorker = navigateActivity.getCurrentFieldWorker();
            formPayload.put(ProjectFormFields.SprayHousehold.SUPERVISOR_EXT_ID, fieldWorker.getExtId());
            formPayload.put(ProjectFormFields.SprayHousehold.SURVEY_DATE,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));


            String locationExtId = navigateActivity.getHierarchyPath()
                    .get(HOUSEHOLD_STATE).getExtId();
            String locationUuid = navigateActivity.getHierarchyPath()
                    .get(HOUSEHOLD_STATE).getUuid();
            formPayload.put(ProjectFormFields.BedNet.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.BedNet.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);


        }
    }

    public static class SuperOjo implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            FieldWorker fieldWorker = navigateActivity.getCurrentFieldWorker();
            formPayload.put(ProjectFormFields.SprayHousehold.SUPERVISOR_EXT_ID, fieldWorker.getExtId());
            formPayload.put(ProjectFormFields.SuperOjo.OJO_DATE,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));


            String locationExtId = navigateActivity.getHierarchyPath()
                    .get(HOUSEHOLD_STATE).getExtId();
            String locationUuid = navigateActivity.getHierarchyPath()
                    .get(HOUSEHOLD_STATE).getUuid();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);


        }
    }

    public static class DuplicateLocation implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload, NavigateActivity navigateActivity) {

            ContentResolver contentResolver = navigateActivity.getContentResolver();

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            DataWrapper mapArea = navigateActivity.getHierarchyPath().get(MAP_AREA_STATE);
            formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, mapArea.getName());

            DataWrapper sector = navigateActivity.getHierarchyPath().get(SECTOR_STATE);
            formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());

            // Assign the next sequential building number in sector
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Iterator<Location> locationIterator = locationGateway.getIterator(contentResolver,
                    locationGateway.findByHierarchyDescendingBuildingNumber(sector.getUuid()));
            int buildingNumber = 1;
            if (locationIterator.hasNext()) {
                buildingNumber = locationIterator.next().getBuildingNumber() + 1;
            }
            formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, String.format("E%03d", buildingNumber));

            DataWrapper household = navigateActivity.getHierarchyPath().get(HOUSEHOLD_STATE);
            String locationExtId = household.getExtId();
            String locationUuid = household.getUuid();

            Location existing = locationGateway.getFirst(contentResolver,locationGateway.findById(locationUuid));

            formPayload.put(ProjectFormFields.Locations.FLOOR_NUMBER, String.format("P%02d", 1));

            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.Locations.LOCATION_UUID, locationUuid);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, locationExtId);
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, locationUuid);
            formPayload.put(ProjectFormFields.Locations.DESCRIPTION, existing.getDescription());
        }
    }
}
