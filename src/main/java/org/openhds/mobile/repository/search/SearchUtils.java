package org.openhds.mobile.repository.search;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.repository.GatewayRegistry;

/**
 * Utility methods for working with Gateways, SearchFragment
 * and FormSearchActivity.
 *
 * For example, get pre-configured FormSearchPluginModules for
 * each gateway with "typical" labels and columns to search.
 */
public class SearchUtils {

    // Search for a field worker based on name and id.
    public static FormSearchPluginModule getFieldWorkerPlugin(String fieldName) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getFieldWorkerGateway(), R.string.search_field_worker_label, fieldName);
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_FIRST_NAME, R.string.field_worker_first_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_LAST_NAME, R.string.field_worker_last_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.FieldWorkers.COLUMN_FIELD_WORKER_EXTID, R.string.field_worker_id_label);

        return plugin;
    }

    // Search for an individual based name and phone number.
    public static FormSearchPluginModule getIndividualPlugin(String fieldName, int searchLabel) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getIndividualGateway(), searchLabel, fieldName);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, R.string.individual_first_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, R.string.individual_last_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, R.string.individual_personal_phone_number_label);

        return plugin;
    }

    // Search for a location based on name, id, and location hierarchy names.
    public static FormSearchPluginModule getLocationPlugin(String fieldName) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getLocationGateway(), R.string.search_location_label, fieldName);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_NAME, R.string.location_name_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_EXTID, R.string.location_ext_id_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME, R.string.location_sector_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME, R.string.location_map_area_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME, R.string.location_locality_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME, R.string.location_community_label);
        plugin.getColumnsAndLabels().put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_CODE, R.string.location_community_code_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER, R.string.location_building_number_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER, R.string.location_floor_number_label);
        plugin.getColumnsAndLabels().put(
                OpenHDS.Locations.COLUMN_LOCATION_HAS_RECIEVED_BEDNETS, R.string.location_has_recieved_bednets_label);

        return plugin;
    }

    // Search for a social group based on name and id.
    public static FormSearchPluginModule getSocialGroupPlugin(String fieldName) {
        FormSearchPluginModule plugin = new FormSearchPluginModule(
                GatewayRegistry.getSocialGroupGateway(), R.string.search_social_group_label, fieldName);
        plugin.getColumnsAndLabels().put(
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_NAME, R.string.social_group_name);
        plugin.getColumnsAndLabels().put(
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_UUID, R.string.social_group_ext_id);
        plugin.getColumnsAndLabels().put(
                OpenHDS.SocialGroups.COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID, R.string.social_group_head_ext_id);

        return plugin;
    }

}
