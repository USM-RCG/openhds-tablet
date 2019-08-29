package org.cimsbioko.navconfig.forms.builders;

import org.cimsbioko.model.core.*;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.navconfig.ProjectFormFields;
import org.cimsbioko.repository.IndividualGateway;
import org.cimsbioko.repository.LocationGateway;
import org.cimsbioko.utilities.IdHelper;
import org.cimsbioko.utilities.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cimsbioko.navconfig.Hierarchy.*;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatBuilding;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatFloor;
import static org.cimsbioko.repository.GatewayRegistry.getLocationGateway;

public class CensusFormPayloadBuilders {

    private static void addNewLocationPayload(Map<String, String> formPayload, LaunchContext ctx) {
        List<DataWrapper> hierPath = ctx.getHierarchyPath().getPath();
        int pathLen = hierPath.size();
        DataWrapper sector = hierPath.get(pathLen - 1), map = hierPath.get(pathLen - 2);
        formPayload.put(ProjectFormFields.General.ENTITY_UUID, IdHelper.generateEntityUuid());
        int nextBuildingNumber = getLocationGateway().nextBuildingNumberInSector(map.getName(), sector.getName());
        formPayload.put(ProjectFormFields.Locations.BUILDING_NUMBER, formatBuilding(nextBuildingNumber, false));
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_EXTID, sector.getExtId());
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_UUID, sector.getUuid());
        formPayload.put(ProjectFormFields.Locations.HIERARCHY_PARENT_UUID, map.getUuid());
        formPayload.put(ProjectFormFields.Locations.SECTOR_NAME, sector.getName());
        formPayload.put(ProjectFormFields.Locations.FLOOR_NUMBER, formatFloor(1, false));
        formPayload.put(ProjectFormFields.Locations.MAP_AREA_NAME, map.getName());
    }

    private static void addNewIndividualPayload(Map<String, String> formPayload, LaunchContext navigateActivity) {
        DataWrapper locationDataWrapper = navigateActivity.getHierarchyPath().get(HOUSEHOLD);
        String individualExtId = IdHelper.generateIndividualExtId(locationDataWrapper);
        formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individualExtId);
        formPayload.put(ProjectFormFields.Individuals.HOUSEHOLD_UUID, navigateActivity.getCurrentSelection().getUuid());
        formPayload.put(ProjectFormFields.Individuals.HOUSEHOLD_EXTID, navigateActivity.getCurrentSelection().getExtId());
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
            Map<String, String> formPayload = new HashMap<>();
            LocationGateway locationGateway = getLocationGateway();
            Location household = locationGateway.findById(ctx.getHierarchyPath().get(HOUSEHOLD).getUuid()).getFirst();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            formPayload.put(ProjectFormFields.General.ENTITY_EXTID, household.getExtId());
            formPayload.put(ProjectFormFields.General.ENTITY_UUID, household.getUuid());
            formPayload.put(ProjectFormFields.Locations.DESCRIPTION, household.getDescription());
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

            IndividualGateway individualGateway = new IndividualGateway();

            List<Individual> residents = individualGateway.findByResidency(household.getUuid()).getList();

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
            formPayload.put(ProjectFormFields.Individuals.INDIVIDUAL_UUID, individual.getUuid());
            return formPayload;
        }
    }
}
