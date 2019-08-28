package org.cimsbioko.navconfig;

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

    private static final String REGION = "region";
    private static final String PROVINCE = "province";
    private static final String DISTRICT = "district";
    private static final String SUBDISTRICT = "subDistrict";
    public static final String LOCALITY = "locality";
    public static final String MAP_AREA = "mapArea";
    public static final String SECTOR = "sector";

    // hard-wired
    public static final String HOUSEHOLD = "household";
    public static final String INDIVIDUAL = "individual";

    private static final Map<String, String> levelLabels = new HashMap<>();
    private static final List<String> adminLevels = new ArrayList<>();
    private static final List<String> levels = new ArrayList<>();

    static {

        levelLabels.put(REGION, "hier.region");
        levelLabels.put(PROVINCE, "hier.province");
        levelLabels.put(DISTRICT, "hier.district");
        levelLabels.put(SUBDISTRICT, "hier.subDistrict");
        levelLabels.put(LOCALITY, "hier.locality");
        levelLabels.put(MAP_AREA, "hier.mapArea");
        levelLabels.put(SECTOR, "hier.sector");

        levelLabels.put(HOUSEHOLD, "hier.household");
        levelLabels.put(INDIVIDUAL, "hier.individual");

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

    Map<String, String> getLevelLabels() {
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
