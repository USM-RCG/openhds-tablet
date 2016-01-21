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
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        switch (state) {
            case REGION_STATE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(REGION_HIERARCHY_LEVEL_NAME), state);
            case PROVINCE_STATE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(PROVINCE_HIERARCHY_LEVEL_NAME), state);
            case DISTRICT_STATE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(DISTRICT_HIERARCHY_LEVEL_NAME), state);
            case SUB_DISTRICT_STATE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(SUB_DISTRICT_HIERARCHY_LEVEL_NAME), state);
            case LOCALITY_STATE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(LOCALITY_HIERARCHY_LEVEL_NAME), state);
            case MAP_AREA_STATE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(MAP_AREA_HIERARCHY_LEVEL_NAME), state);
            case SECTOR_STATE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(SECTOR_HIERARCHY_LEVEL_NAME), state);
            case HOUSEHOLD_STATE:
                LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
                return locationGateway.getQueryResultList(contentResolver, locationGateway.findAll(), state);
            case INDIVIDUAL_STATE:
                IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
                return individualGateway.getQueryResultList(contentResolver, individualGateway.findAll(), state);
            default:
                return new ArrayList<>();
        }
    }

    public List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper qr, String childState) {
        switch (qr.getCategory()) {
            case REGION_STATE:
            case PROVINCE_STATE:
            case DISTRICT_STATE:
            case SUB_DISTRICT_STATE:
            case LOCALITY_STATE:
            case MAP_AREA_STATE:
                LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByParent(qr.getUuid()), childState);
            case SECTOR_STATE:
                LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
                return locationGateway.getQueryResultList(contentResolver,
                        locationGateway.findByHierarchy(qr.getUuid()), childState);
            case HOUSEHOLD_STATE:
                IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
                return individualGateway.getQueryResultList(contentResolver,
                        individualGateway.findByResidency(qr.getUuid()), childState);
        }
        return new ArrayList<>();
    }
}
