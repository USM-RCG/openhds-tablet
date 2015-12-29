package org.openhds.mobile.projectdata.QueryHelpers;

import android.content.ContentResolver;

import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;

import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.projectdata.BiokoHierarchy.*;

public class CensusQueryHelper implements QueryHelper {

    // These must match the server data.
    // They come from the name column of the locationhierarchylevel table
    public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
    public static final String PROVINCE_HIERARCHY_LEVEL_NAME = "Province";
    public static final String DISTRICT_HIERARCHY_LEVEL_NAME = "District";
    public static final String SUB_DISTRICT_HIERARCHY_LEVEL_NAME = "SubDistrict";
    public static final String LOCALITY_HIERARCHY_LEVEL_NAME = "Locality";
    public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
    public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

    public CensusQueryHelper() {}

    public List<DataWrapper> getAll(ContentResolver contentResolver, String state) {

        if (state.equals(REGION_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(REGION_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(PROVINCE_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(PROVINCE_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(DISTRICT_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(DISTRICT_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(SUB_DISTRICT_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(SUB_DISTRICT_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(LOCALITY_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(LOCALITY_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(MAP_AREA_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(MAP_AREA_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(SECTOR_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByLevel(SECTOR_HIERARCHY_LEVEL_NAME), state);

        } else if (state.equals(HOUSEHOLD_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            return locationGateway.getQueryResultList(contentResolver, locationGateway.findAll(), state);

        } else if (state.equals(INDIVIDUAL_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            return individualGateway.getQueryResultList(contentResolver, individualGateway.findAll(), state);
        }

        return new ArrayList<DataWrapper>();
    }

    public List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper qr, String childState) {
        String state = qr.getCategory();

        if (state.equals(REGION_STATE)
                || state.equals(PROVINCE_STATE)
                || state.equals(DISTRICT_STATE)
                || state.equals(SUB_DISTRICT_STATE)
                || state.equals(LOCALITY_STATE)
                || state.equals(MAP_AREA_STATE)) {

            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            return locationHierarchyGateway.getQueryResultList(contentResolver,
                    locationHierarchyGateway.findByParent(qr.getUuid()), childState);

        } else if (state.equals(SECTOR_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            return locationGateway.getQueryResultList(contentResolver,
                    locationGateway.findByHierarchyDescendingBuildingNumber(qr.getUuid()), childState);

        } else if (state.equals(HOUSEHOLD_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            return individualGateway.getQueryResultList(contentResolver,
                    individualGateway.findByResidency(qr.getUuid()), childState);
        }

        return new ArrayList<DataWrapper>();
    }
}
