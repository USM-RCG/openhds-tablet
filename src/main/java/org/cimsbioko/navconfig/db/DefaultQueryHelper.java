package org.cimsbioko.navconfig.db;

import org.cimsbioko.model.core.Individual;
import org.cimsbioko.model.core.Location;
import org.cimsbioko.model.core.LocationHierarchy;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.repository.DataWrapper;
import org.cimsbioko.repository.gateway.Gateway;
import org.cimsbioko.repository.gateway.IndividualGateway;
import org.cimsbioko.repository.gateway.LocationGateway;
import org.cimsbioko.repository.gateway.LocationHierarchyGateway;

import java.util.ArrayList;
import java.util.List;

import static org.cimsbioko.navconfig.BiokoHierarchy.*;
import static org.cimsbioko.repository.GatewayRegistry.*;

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

    public List<DataWrapper> getAll(String level) {
        switch (level) {
            case REGION:
            case PROVINCE:
            case DISTRICT:
            case SUBDISTRICT:
            case LOCALITY:
            case MAP_AREA:
            case SECTOR:
                LocationHierarchyGateway hierGateway = getLocationHierarchyGateway();
                return hierGateway.getQueryResultList(hierGateway.findByLevel(level), level);
            case HOUSEHOLD:
            case INDIVIDUAL:
                Gateway<?> gateway = getLevelGateway(level);
                if (gateway != null) {
                    return gateway.getQueryResultList(gateway.findAll(), level);
                }
        }
        return new ArrayList<>();
    }

    public List<DataWrapper> getChildren(DataWrapper parent, String childLevel) {
        if (parent != null) {
            switch (parent.getCategory()) {
                case REGION:
                case PROVINCE:
                case DISTRICT:
                case SUBDISTRICT:
                case LOCALITY:
                case MAP_AREA:
                    LocationHierarchyGateway locationHierarchyGateway = getLocationHierarchyGateway();
                    return locationHierarchyGateway.getQueryResultList(
                            locationHierarchyGateway.findByParent(parent.getUuid()), childLevel);
                case SECTOR:
                    LocationGateway locationGateway = getLocationGateway();
                    return locationGateway.getQueryResultList(
                            locationGateway.findByHierarchy(parent.getUuid()), childLevel);
                case HOUSEHOLD:
                    IndividualGateway individualGateway = getIndividualGateway();
                    return individualGateway.getQueryResultList(
                            individualGateway.findByResidency(parent.getUuid()), childLevel);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public DataWrapper get(String level, String uuid) {
        Gateway gw = getLevelGateway(level);
        return gw != null ? gw.getFirstQueryResult(gw.findById(uuid), level) : null;
    }

    @Override
    public DataWrapper getParent(String level, String uuid) {
        String parentLevel = NavigatorConfig.getInstance().getParentLevel(level);
        switch (level) {
            case PROVINCE:
            case DISTRICT:
            case SUBDISTRICT:
            case LOCALITY:
            case MAP_AREA:
            case SECTOR:
                LocationHierarchyGateway hierarchyGateway = getLocationHierarchyGateway();
                LocationHierarchy lh = hierarchyGateway.getFirst(hierarchyGateway.findById(uuid));
                return get(parentLevel, lh.getParentUuid());
            case HOUSEHOLD:
                LocationGateway locationGateway = getLocationGateway();
                Location l = locationGateway.getFirst(locationGateway.findById(uuid));
                return get(parentLevel, l.getHierarchyUuid());
            case INDIVIDUAL:
                IndividualGateway individualGateway = getIndividualGateway();
                Individual i = individualGateway.getFirst(individualGateway.findById(uuid));
                return get(parentLevel, i.getCurrentResidenceUuid());
            default:
                return null;
        }
    }

}
