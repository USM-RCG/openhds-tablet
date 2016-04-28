package org.openhds.mobile.navconfig;

import org.openhds.mobile.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the "hierarchy" for the {@link NavigatorConfig}'s
 * {@link NavigatorModule}s. It is simply the list of state names, state
 * labels (for UI), and state sequence.
 */
public class BiokoHierarchy {

    static final BiokoHierarchy INSTANCE = new BiokoHierarchy();

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

    private static final Map<String, Integer> levelLabels = new HashMap<>();
    private static final List<String> levels = new ArrayList<>();

    static {

        levelLabels.put(REGION_STATE, R.string.region_label);
        levelLabels.put(PROVINCE_STATE, R.string.province_label);
        levelLabels.put(DISTRICT_STATE, R.string.district_label);
        levelLabels.put(SUB_DISTRICT_STATE, R.string.sub_district_label);
        levelLabels.put(LOCALITY_STATE, R.string.locality_label);
        levelLabels.put(MAP_AREA_STATE, R.string.map_area_label);
        levelLabels.put(SECTOR_STATE, R.string.sector_label);
        levelLabels.put(HOUSEHOLD_STATE, R.string.household_label);
        levelLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
        levelLabels.put(BOTTOM_STATE, R.string.bottom_label);

        levels.add(REGION_STATE);
        levels.add(PROVINCE_STATE);
        levels.add(DISTRICT_STATE);
        levels.add(SUB_DISTRICT_STATE);
        levels.add(LOCALITY_STATE);
        levels.add(MAP_AREA_STATE);
        levels.add(SECTOR_STATE);
        levels.add(HOUSEHOLD_STATE);
        levels.add(INDIVIDUAL_STATE);
        levels.add(BOTTOM_STATE);
    }

    public Map<String, Integer> getLevelLabels() {
        return levelLabels;
    }

    public List<String> getLevels() {
        return levels;
    }

}
