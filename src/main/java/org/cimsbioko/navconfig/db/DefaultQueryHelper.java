package org.cimsbioko.navconfig.db;

import org.cimsbioko.model.core.Individual;
import org.cimsbioko.model.core.Location;
import org.cimsbioko.model.core.LocationHierarchy;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.data.DataWrapper;
import org.cimsbioko.data.Gateway;
import org.cimsbioko.data.LocationHierarchyGateway;

import java.util.ArrayList;
import java.util.List;

import static org.cimsbioko.navconfig.Hierarchy.*;
import static org.cimsbioko.data.GatewayRegistry.*;

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

    private boolean isAdminLevel(String level) {
        return NavigatorConfig.getInstance().getAdminLevels().contains(level);
    }

    private boolean isTopLevel(String level) {
        return NavigatorConfig.getInstance().getTopLevel().equals(level);
    }

    private boolean isLastAdminLevel(String level) {
        NavigatorConfig config = NavigatorConfig.getInstance();
        List<String> adminLevels = config.getAdminLevels();
        return !adminLevels.isEmpty() && adminLevels.get(adminLevels.size() - 1).equals(level);
    }

    private Gateway<?> getLevelGateway(String level) {
        if (isAdminLevel(level)) {
            return getLocationHierarchyGateway();
        } else {
            switch (level) {
                case HOUSEHOLD:
                    return getLocationGateway();
                case INDIVIDUAL:
                    return getIndividualGateway();
                default:
                    return null;
            }
        }
    }

    public List<DataWrapper> getAll(String level) {
        if (isAdminLevel(level)) {
            LocationHierarchyGateway hierGateway = getLocationHierarchyGateway();
            return hierGateway.findByLevel(level).getWrapperList();
        }
        switch (level) {
            case HOUSEHOLD:
            case INDIVIDUAL:
                Gateway<?> gateway = getLevelGateway(level);
                if (gateway != null) {
                    return gateway.findAll().getWrapperList();
                }
        }
        return new ArrayList<>();
    }

    public List<DataWrapper> getChildren(DataWrapper parent, String childLevel) {
        if (parent != null) {
            String level = parent.getCategory();
            if (isAdminLevel(level)) {
                if (!isLastAdminLevel(level)) {
                    LocationHierarchyGateway locationHierarchyGateway = getLocationHierarchyGateway();
                    return locationHierarchyGateway.findByParent(parent.getUuid()).getWrapperList();
                } else {
                    return getLocationGateway().findByHierarchy(parent.getUuid()).getWrapperList();
                }
            } else if (HOUSEHOLD.equals(level)) {
                return getIndividualGateway().findByResidency(parent.getUuid()).getWrapperList();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public DataWrapper get(String level, String uuid) {
        Gateway gw = getLevelGateway(level);
        return gw != null ? gw.findById(uuid).getFirstWrapper() : null;
    }

    @Override
    public DataWrapper getParent(String level, String uuid) {
        NavigatorConfig config = NavigatorConfig.getInstance();
        String parentLevel = config.getParentLevel(level);
        if (isAdminLevel(level) && !isTopLevel(level)) {
            LocationHierarchyGateway hierarchyGateway = getLocationHierarchyGateway();
            LocationHierarchy lh = hierarchyGateway.findById(uuid).getFirst();
            return get(parentLevel, lh.getParentUuid());
        } else {
            switch (level) {
                case HOUSEHOLD:
                    Location l = getLocationGateway().findById(uuid).getFirst();
                    return get(parentLevel, l.getHierarchyUuid());
                case INDIVIDUAL:
                    Individual i = getIndividualGateway().findById(uuid).getFirst();
                    return get(parentLevel, i.getCurrentResidenceUuid());
                default:
                    return null;
            }
        }
    }
}
