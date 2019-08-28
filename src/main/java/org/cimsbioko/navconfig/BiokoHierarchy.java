package org.cimsbioko.navconfig;

import org.cimsbioko.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines the "hierarchy" for the {@link NavigatorConfig}'s {@link NavigatorModule}s. It is simply the list of level
 * names, labels (for UI), and their ordering from highest to lowest.
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

    // hard-wired
    public static final String HOUSEHOLD = "household";
    public static final String INDIVIDUAL = "individual";

    private static final Map<String, Integer> levelLabels = new HashMap<>();
    private static final List<String> adminLevels = new ArrayList<>();
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

        adminLevels.add(REGION);
        adminLevels.add(PROVINCE);
        adminLevels.add(DISTRICT);
        adminLevels.add(SUBDISTRICT);
        adminLevels.add(LOCALITY);
        adminLevels.add(MAP_AREA);
        adminLevels.add(SECTOR);

        levels.addAll(adminLevels);
        levels.add(HOUSEHOLD);
        levels.add(INDIVIDUAL);
    }

    Map<String, Integer> getLevelLabels() {
        return levelLabels;
    }

    List<String> getAdminLevels() {
        return adminLevels;
    }

    List<String> getLevels() {
        return levels;
    }

    public String getParentLevel(String level) {
        switch (level) {
            case PROVINCE:
                return REGION;
            case DISTRICT:
                return PROVINCE;
            case SUBDISTRICT:
                return DISTRICT;
            case LOCALITY:
                return SUBDISTRICT;
            case MAP_AREA:
                return LOCALITY;
            case SECTOR:
                return MAP_AREA;
            case HOUSEHOLD:
                return SECTOR;
            case INDIVIDUAL:
                return HOUSEHOLD;
            default:
                return null;
        }
    }

}
