package org.openhds.mobile.navconfig.db;

import android.content.ContentResolver;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.navconfig.BiokoHierarchy;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.gateway.Gateway;
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
import static org.openhds.mobile.repository.GatewayRegistry.getIndividualGateway;
import static org.openhds.mobile.repository.GatewayRegistry.getLocationGateway;
import static org.openhds.mobile.repository.GatewayRegistry.getLocationHierarchyGateway;

public class DefaultQueryHelper implements QueryHelper {

    private static QueryHelper instance;

    protected DefaultQueryHelper() {
    }

    public static synchronized QueryHelper getInstance() {
        if (instance == null) {
            instance = new DefaultQueryHelper();
        }
        return instance;
    }

    private Gateway<?> getLevelGateway(String level) {
        switch (level) {
            case REGION:
            case PROVINCE:
            case DISTRICT:
            case SUBDISTRICT:
            case LOCALITY:
            case MAP_AREA:
            case SECTOR:
                return getLocationHierarchyGateway();
            case HOUSEHOLD:
                return getLocationGateway();
            case INDIVIDUAL:
                return getIndividualGateway();
            default:
                return null;
        }
    }

    public List<DataWrapper> getAll(ContentResolver resolver, String level) {
        switch (level) {
            case REGION:
            case PROVINCE:
            case DISTRICT:
            case SUBDISTRICT:
            case LOCALITY:
            case MAP_AREA:
            case SECTOR:
                String serverLevel = NavigatorConfig.getInstance().getServerLevel(level);
                LocationHierarchyGateway hierGateway = getLocationHierarchyGateway();
                return hierGateway.getQueryResultList(resolver, hierGateway.findByLevel(serverLevel), level);
            case HOUSEHOLD:
            case INDIVIDUAL:
                Gateway<?> gateway = getLevelGateway(level);
                if (gateway != null) {
                    return gateway.getQueryResultList(resolver, gateway.findAll(), level);
                }
        }
        return new ArrayList<>();
    }

    public List<DataWrapper> getChildren(ContentResolver resolver, DataWrapper parent, String childLevel) {
        switch (parent.getCategory()) {
            case REGION:
            case PROVINCE:
            case DISTRICT:
            case SUBDISTRICT:
            case LOCALITY:
            case MAP_AREA:
                LocationHierarchyGateway locationHierarchyGateway = getLocationHierarchyGateway();
                return locationHierarchyGateway.getQueryResultList(resolver,
                        locationHierarchyGateway.findByParent(parent.getUuid()), childLevel);
            case SECTOR:
                LocationGateway locationGateway = getLocationGateway();
                return locationGateway.getQueryResultList(resolver,
                        locationGateway.findByHierarchy(parent.getUuid()), childLevel);
            case HOUSEHOLD:
                IndividualGateway individualGateway = getIndividualGateway();
                return individualGateway.getQueryResultList(resolver,
                        individualGateway.findByResidency(parent.getUuid()), childLevel);
        }
        return new ArrayList<>();
    }

    @Override
    public DataWrapper get(ContentResolver resolver, String level, String uuid) {
        Gateway gw = getLevelGateway(level);
        return gw != null ? gw.getFirstQueryResult(resolver, gw.findById(uuid), level) : null;
    }

    @Override
    public DataWrapper getParent(ContentResolver resolver, String level, String uuid) {
        String parentLevel = NavigatorConfig.getInstance().getParentLevel(level);
        switch (level) {
            case PROVINCE:
            case DISTRICT:
            case SUBDISTRICT:
            case LOCALITY:
            case MAP_AREA:
            case SECTOR:
                LocationHierarchyGateway hierarchyGateway = getLocationHierarchyGateway();
                LocationHierarchy lh = hierarchyGateway.getFirst(resolver, hierarchyGateway.findById(uuid));
                return get(resolver, parentLevel, lh.getParentUuid());
            case HOUSEHOLD:
                LocationGateway locationGateway = getLocationGateway();
                Location l = locationGateway.getFirst(resolver, locationGateway.findById(uuid));
                return get(resolver, parentLevel, l.getHierarchyUuid());
            case INDIVIDUAL:
                IndividualGateway individualGateway = getIndividualGateway();
                Individual i = individualGateway.getFirst(resolver, individualGateway.findById(uuid));
                return get(resolver, parentLevel, i.getCurrentResidenceUuid());
            default:
                return null;
        }
    }

}
