package org.cimsbioko.navconfig;

import org.cimsbioko.App;

import java.util.HashMap;
import java.util.Map;

public class ProjectFormFields {

    public static final class General {

        public static final String COLLECTION_DATE_TIME = "collectionDateTime";
        public static final String FIELD_WORKER_UUID = "fieldWorkerUuid";
        public static final String ENTITY_UUID = "entityUuid";
        public static final String ENTITY_EXTID = "entityExtId";
        public static final String NEEDS_REVIEW = "needsReview";
        public static final String FIELD_WORKER_EXTID = "fieldWorkerExtId";

        public static final String DISTRIBUTION_DATE_TIME = "distributionDateTime";

        public static final String HOUSEHOLD_STATE_FIELD_NAME = "householdExtId";
    }

    public static final class Locations {

        public static final String HIERARCHY_PARENT_UUID = "hierarchyParentUuid";
        public static final String HIERARCHY_UUID = "hierarchyUuid";
        public static final String HIERARCHY_EXTID = "hierarchyExtId";
        public static final String LOCATION_EXTID = "locationExtId";
        public static final String LOCATION_UUID = "locationUuid";
        public static final String LOCATION_NAME = "locationName";
        public static final String MAP_AREA_NAME = "mapAreaName";
        public static final String SECTOR_NAME = "sectorName";

        public static final String BUILDING_NUMBER = "locationBuildingNumber";
        public static final String FLOOR_NUMBER = "locationFloorNumber";

        public static final String DESCRIPTION = "description";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";

        private static Map<String, String> columnsToFieldNames = new HashMap<>();

        static {
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_HIERARCHY_UUID, HIERARCHY_UUID);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_EXTID, LOCATION_EXTID);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_NAME, LOCATION_NAME);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_MAP_AREA_NAME, MAP_AREA_NAME);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_SECTOR_NAME, SECTOR_NAME);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_BUILDING_NUMBER, BUILDING_NUMBER);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_DESCRIPTION, DESCRIPTION);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_LONGITUDE, LONGITUDE);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_LATITUDE, LATITUDE);
            columnsToFieldNames.put(App.Locations.COLUMN_LOCATION_UUID, General.ENTITY_UUID);
        }

        public static String getFieldNameFromColumn(String column) {
            if (columnsToFieldNames.containsKey(column)) {
                return columnsToFieldNames.get(column);
            } else {
                return null;
            }
        }
    }

    public static final class Individuals {

        public static final String INDIVIDUAL_UUID = "individualUuid";
        public static final String INDIVIDUAL_EXTID = "individualExtId";
        public static final String FIRST_NAME = "individualFirstName";
        public static final String LAST_NAME = "individualLastName";
        public static final String OTHER_NAMES = "individualOtherNames";
        public static final String DATE_OF_BIRTH = "individualDateOfBirth";
        public static final String GENDER = "individualGender";
        public static final String PHONE_NUMBER = "individualPhoneNumber";
        public static final String OTHER_PHONE_NUMBER = "individualOtherPhoneNumber";
        public static final String POINT_OF_CONTACT_NAME = "individualPointOfContactName";
        public static final String POINT_OF_CONTACT_PHONE_NUMBER = "individualPointOfContactPhoneNumber";
        public static final String NATIONALITY = "individualNationality";
        public static final String LANGUAGE_PREFERENCE = "individualLanguagePreference";
        public static final String DIP = "individualDip";

        public static final String RELATIONSHIP_TO_HEAD = "individualRelationshipToHeadOfHousehold";
        public static final String HEAD_PREFILLED_FLAG = "headPrefilledFlag";
        public static final String STATUS = "individualMemberStatus";

        public static final String HOUSEHOLD_UUID = "householdUuid";
        public static final String HOUSEHOLD_EXTID = General.HOUSEHOLD_STATE_FIELD_NAME;

        private static Map<String, String> columnsToFieldNames = new HashMap<>();

        static {
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_EXTID, INDIVIDUAL_EXTID);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, FIRST_NAME);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, LAST_NAME);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES, OTHER_NAMES);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_DOB, DATE_OF_BIRTH);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_GENDER, GENDER);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER, PHONE_NUMBER);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, OTHER_PHONE_NUMBER);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME, POINT_OF_CONTACT_NAME);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER, POINT_OF_CONTACT_PHONE_NUMBER);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, LANGUAGE_PREFERENCE);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID, DIP);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, HOUSEHOLD_UUID);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_STATUS, STATUS);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY, NATIONALITY);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_UUID, General.ENTITY_UUID);
            columnsToFieldNames.put(App.Individuals.COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD, RELATIONSHIP_TO_HEAD);
        }

        public static String getFieldNameFromColumn(String column) {
            if (columnsToFieldNames.containsKey(column)) {
                return columnsToFieldNames.get(column);
            } else {
                return null;
            }
        }
    }

    public static final class BedNet {
        public static final String BED_NET_CODE = "netCode";
        public static final String HOUSEHOLD_SIZE = "householdSize";
    }

    public static final class SprayHousehold {
        public static final String SUPERVISOR_EXT_ID = "supervisorExtId";
    }

    public static final class SuperOjo {
        public static final String OJO_DATE = "ojo_date";
    }

    public static final class CreateMap {
        public static final String LOCALITY_UUID = "localityUuid";
        public static final String MAP_UUID = "mapUuid";
        public static final String MAP_NAME = "mapName";
    }

    public static final class CreateSector {
        public static final String MAP_UUID = "mapUuid";
        public static final String SECTOR_UUID = "sectorUuid";
        public static final String SECTOR_NAME = "sectorName";
    }

    public static final class MalariaIndicatorSurvey {
        public static final String SURVEY_DATE = "survey_date";
    }
}