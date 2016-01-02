package org.openhds.mobile.projectdata;

import org.openhds.mobile.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the "hierarchy" for the {@link ProjectActivityBuilder}'s
 * {@link NavigatePluginModule}s. It is simply the list of state names, state
 * labels (for UI), and state sequence.
 */
public class BiokoHierarchy implements HierarchyInfo {

    static final BiokoHierarchy INSTANCE = new BiokoHierarchy();

    public static final String HIERARCHY_NAME = "biokoHierarchy";

    public static final String REGION_STATE = "region";
    public static final String PROVINCE_STATE = "province";
    public static final String DISTRICT_STATE = "district";
    public static final String SUB_DISTRICT_STATE = "subDistrict";
    public static final String LOCALITY_STATE = "locality";
    public static final String MAP_AREA_STATE = "mapArea";
    public static final String SECTOR_STATE = "sector";
    public static final String HOUSEHOLD_STATE = "household";
    public static final String INDIVIDUAL_STATE = "individual";
    public static final String BOTTOM_STATE = "bottom";

    private static final Map<String, Integer> stateLabels = new HashMap<>();
    private static final List<String> stateSequence = new ArrayList<>();

    static {

        stateLabels.put(REGION_STATE, R.string.region_label);
        stateLabels.put(PROVINCE_STATE, R.string.province_label);
        stateLabels.put(DISTRICT_STATE, R.string.district_label);
        stateLabels.put(SUB_DISTRICT_STATE, R.string.sub_district_label);
        stateLabels.put(LOCALITY_STATE, R.string.locality_label);
        stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
        stateLabels.put(SECTOR_STATE, R.string.sector_label);
        stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
        stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
        stateLabels.put(BOTTOM_STATE, R.string.bottom_label);

        stateSequence.add(REGION_STATE);
        stateSequence.add(PROVINCE_STATE);
        stateSequence.add(DISTRICT_STATE);
        stateSequence.add(SUB_DISTRICT_STATE);
        stateSequence.add(LOCALITY_STATE);
        stateSequence.add(MAP_AREA_STATE);
        stateSequence.add(SECTOR_STATE);
        stateSequence.add(HOUSEHOLD_STATE);
        stateSequence.add(INDIVIDUAL_STATE);
        stateSequence.add(BOTTOM_STATE);
    }

    @Override
    public Map<String, Integer> getStateLabels() {
        return stateLabels;
    }

    @Override
    public List<String> getStateSequence() {
        return stateSequence;
    }

    @Override
    public String getHierarchyName() {
        return HIERARCHY_NAME;
    }
}
