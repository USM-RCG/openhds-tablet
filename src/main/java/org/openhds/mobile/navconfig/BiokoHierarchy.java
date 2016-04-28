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

    public static final String REGION = "region";
    public static final String PROVINCE = "province";
    public static final String DISTRICT = "district";
    public static final String SUBDISTRICT = "subDistrict";
    public static final String LOCALITY = "locality";
    public static final String MAP_AREA = "mapArea";
    public static final String SECTOR = "sector";
    public static final String HOUSEHOLD = "household";
    public static final String INDIVIDUAL = "individual";
    public static final String BOTTOM = "bottom";

    private static final Map<String, Integer> levelLabels = new HashMap<>();
    private static final List<String> levels = new ArrayList<>();

    static {

        levelLabels.put(REGION, R.string.region_label);
        levelLabels.put(PROVINCE, R.string.province_label);
        levelLabels.put(DISTRICT, R.string.district_label);
        levelLabels.put(SUBDISTRICT, R.string.sub_district_label);
        levelLabels.put(LOCALITY, R.string.locality_label);
        levelLabels.put(MAP_AREA, R.string.map_area_label);
        levelLabels.put(SECTOR, R.string.sector_label);
        levelLabels.put(HOUSEHOLD, R.string.household_label);
        levelLabels.put(INDIVIDUAL, R.string.individual_label);
        levelLabels.put(BOTTOM, R.string.bottom_label);

        levels.add(REGION);
        levels.add(PROVINCE);
        levels.add(DISTRICT);
        levels.add(SUBDISTRICT);
        levels.add(LOCALITY);
        levels.add(MAP_AREA);
        levels.add(SECTOR);
        levels.add(HOUSEHOLD);
        levels.add(INDIVIDUAL);
        levels.add(BOTTOM);
    }

    Map<String, Integer> getLevelLabels() {
        return levelLabels;
    }

    List<String> getLevels() {
        return levels;
    }

}
