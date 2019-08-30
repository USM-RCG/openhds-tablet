package org.cimsbioko.navconfig.forms.builders;

import org.cimsbioko.model.core.*;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.navconfig.forms.UsedByJSConfig;
import org.cimsbioko.data.DataWrapper;
import org.cimsbioko.utilities.IdHelper;
import org.cimsbioko.utilities.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cimsbioko.navconfig.Hierarchy.*;
import static org.cimsbioko.navconfig.forms.KnownFields.ENTITY_EXTID;
import static org.cimsbioko.navconfig.forms.KnownFields.ENTITY_UUID;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatBuilding;
import static org.cimsbioko.navconfig.forms.builders.PayloadTools.formatFloor;
import static org.cimsbioko.data.GatewayRegistry.getIndividualGateway;
import static org.cimsbioko.data.GatewayRegistry.getLocationGateway;

public class CensusFormPayloadBuilders {

    private static void addNewLocationPayload(Map<String, String> formPayload, LaunchContext ctx) {
        List<DataWrapper> hierPath = ctx.getHierarchyPath().getPath();
        int pathLen = hierPath.size();
        DataWrapper sector = hierPath.get(pathLen - 1), map = hierPath.get(pathLen - 2);
        formPayload.put(ENTITY_UUID, IdHelper.generateEntityUuid());
        int nextBuildingNumber = getLocationGateway().nextBuildingNumberInSector(map.getName(), sector.getName());
        formPayload.put("locationBuildingNumber", formatBuilding(nextBuildingNumber, false));
        formPayload.put("hierarchyExtId", sector.getExtId());
        formPayload.put("hierarchyUuid", sector.getUuid());
        formPayload.put("hierarchyParentUuid", map.getUuid());
        formPayload.put("sectorName", sector.getName());
        formPayload.put("locationFloorNumber", formatFloor(1, false));
        formPayload.put("mapAreaName", map.getName());
    }

    private static void addNewIndividualPayload(Map<String, String> formPayload, LaunchContext navigateActivity) {
        DataWrapper locationDataWrapper = navigateActivity.getHierarchyPath().get(HOUSEHOLD);
        String individualExtId = IdHelper.generateIndividualExtId(locationDataWrapper);
        formPayload.put("individualExtId", individualExtId);
        formPayload.put("householdUuid", navigateActivity.getCurrentSelection().getUuid());
        formPayload.put("householdExtId", navigateActivity.getCurrentSelection().getExtId());
        formPayload.put(ENTITY_EXTID, individualExtId);
        formPayload.put(ENTITY_UUID, IdHelper.generateEntityUuid());
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
            Location household = getLocationGateway().findById(ctx.getHierarchyPath().get(HOUSEHOLD).getUuid()).getFirst();
            PayloadTools.addMinimalFormPayload(formPayload, ctx);
            formPayload.put(ENTITY_EXTID, household.getExtId());
            formPayload.put(ENTITY_UUID, household.getUuid());
            formPayload.put("description", household.getDescription());
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

            List<Individual> residents = getIndividualGateway().findByResidency(household.getUuid()).getList();

            // pre-fill contact name and number as best we can without household role info
            if (residents.size() == 1) {
                Individual head = residents.get(0);
                formPayload.put("individualPointOfContactName", Individual.getFullName(head));
                formPayload.put("individualPointOfContactPhoneNumber", head.getPhoneNumber());
            } else {
                for (Individual resident : residents) {
                    String contactName = resident.getPointOfContactName(),
                            contactNumber = resident.getPointOfContactPhoneNumber();
                    if (!StringUtils.isEmpty(contactName) && !StringUtils.isEmpty(contactNumber)) {
                        formPayload.put("individualPointOfContactName", contactName);
                        formPayload.put("individualPointOfContactPhoneNumber", contactNumber);
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
            formPayload.put("headPrefilledFlag", "true");
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
            formPayload.put("individualUuid", individual.getUuid());
            return formPayload;
        }
    }
}
