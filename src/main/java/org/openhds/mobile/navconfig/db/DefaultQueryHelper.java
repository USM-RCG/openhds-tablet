package org.openhds.mobile.navconfig.db;

import android.content.ContentResolver;

import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;

import java.util.ArrayList;
import java.util.List;

import static org.openhds.mobile.navconfig.BiokoHierarchy.DISTRICT;
import static org.openhds.mobile.navconfig.BiokoHierarchy.HOUSEHOLD;
import static org.openhds.mobile.navconfig.BiokoHierarchy.INDIVIDUAL;
import static org.openhds.mobile.navconfig.BiokoHierarchy.LOCALITY;
import static org.openhds.mobile.navconfig.BiokoHierarchy.MAP_AREA;
import static org.openhds.mobile.navconfig.BiokoHierarchy.PROVINCE;
import static org.openhds.mobile.navconfig.BiokoHierarchy.REGION;
import static org.openhds.mobile.navconfig.BiokoHierarchy.SECTOR;
import static org.openhds.mobile.navconfig.BiokoHierarchy.SUBDISTRICT;

public class DefaultQueryHelper implements QueryHelper {

    // These must match the server data.
    // They come from the name column of the locationhierarchylevel table
    public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
    public static final String PROVINCE_HIERARCHY_LEVEL_NAME = "Province";
    public static final String DISTRICT_HIERARCHY_LEVEL_NAME = "District";
    public static final String SUB_DISTRICT_HIERARCHY_LEVEL_NAME = "SubDistrict";
    public static final String LOCALITY_HIERARCHY_LEVEL_NAME = "Locality";
    public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
    public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

    private static QueryHelper instance;

    protected DefaultQueryHelper() {}

    public static synchronized QueryHelper getInstance() {
        if (instance == null) {
            instance = new DefaultQueryHelper();
        }
        return instance;
    }

    public List<DataWrapper> getAll(ContentResolver contentResolver, String level) {
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        switch (level) {
            case REGION:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(REGION_HIERARCHY_LEVEL_NAME), level);
            case PROVINCE:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(PROVINCE_HIERARCHY_LEVEL_NAME), level);
            case DISTRICT:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(DISTRICT_HIERARCHY_LEVEL_NAME), level);
            case SUBDISTRICT:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(SUB_DISTRICT_HIERARCHY_LEVEL_NAME), level);
            case LOCALITY:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(LOCALITY_HIERARCHY_LEVEL_NAME), level);
            case MAP_AREA:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(MAP_AREA_HIERARCHY_LEVEL_NAME), level);
            case SECTOR:
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByLevel(SECTOR_HIERARCHY_LEVEL_NAME), level);
            case HOUSEHOLD:
                LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
                return locationGateway.getQueryResultList(contentResolver, locationGateway.findAll(), level);
            case INDIVIDUAL:
                IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
                return individualGateway.getQueryResultList(contentResolver, individualGateway.findAll(), level);
            default:
                return new ArrayList<>();
        }
    }

    public List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper qr, String childLevel) {
        switch (qr.getCategory()) {
            case REGION:
            case PROVINCE:
            case DISTRICT:
            case SUBDISTRICT:
            case LOCALITY:
            case MAP_AREA:
                LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
                return locationHierarchyGateway.getQueryResultList(contentResolver,
                        locationHierarchyGateway.findByParent(qr.getUuid()), childLevel);
            case SECTOR:
                LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
                return locationGateway.getQueryResultList(contentResolver,
                        locationGateway.findByHierarchy(qr.getUuid()), childLevel);
            case HOUSEHOLD:
                IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
                return individualGateway.getQueryResultList(contentResolver,
                        individualGateway.findByResidency(qr.getUuid()), childLevel);
        }
        return new ArrayList<>();
    }
}
