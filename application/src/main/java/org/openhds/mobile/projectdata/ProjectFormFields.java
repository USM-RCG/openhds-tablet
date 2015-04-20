package org.openhds.mobile.projectdata;

import org.openhds.mobile.OpenHDS;

import java.util.HashMap;
import java.util.Map;

public class ProjectFormFields {

    public static final class General {

        public static final String PROCESSED_BY_MIRTH = "processedByMirth";
        public static final String COLLECTION_DATE_TIME = "collectionDateTime";
        public static final String FIELD_WORKER_UUID = "fieldWorkerUuid";
        public static final String ENTITY_UUID = "entityUuid";
        public static final String ENTITY_EXTID = "entityExtId";
        public static final String NEEDS_REVIEW = "needsReview";
        public static final String FIELD_WORKER_EXTID = "fieldWorkerExtId";


        public static final String DISTRIBUTION_DATE_TIME = "distributionDateTime";

        public static final String REGION_STATE_FIELD_NAME = "regionExtId";
        public static final String PROVINCE_STATE_FIELD_NAME = "provinceExtId";
        public static final String DISTRICT_STATE_FIELD_NAME = "districtExtId";
        public static final String SUB_DISTRICT_STATE_FIELD_NAME = "subDistrictExtId";
        public static final String LOCALITY_STATE_FIELD_NAME = "localityExtId";
        public static final String MAP_AREA_STATE_FIELD_NAME = "mapAreaExtId";
        public static final String SECTOR_STATE_FIELD_NAME = "sectorExtId";
        public static final String HOUSEHOLD_STATE_FIELD_NAME = "householdExtId";
        public static final String INDIVIDUAL_STATE_FIELD_NAME = "individualExtId";
        public static final String BOTTOM_STATE_FIELD_NAME = "bottomExtId";

        private static final Map<String, String> stateFieldNames = new HashMap<String, String>();

        static {
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.REGION_STATE,
                    REGION_STATE_FIELD_NAME);
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.PROVINCE_STATE,
                    PROVINCE_STATE_FIELD_NAME);
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.DISTRICT_STATE,
                    DISTRICT_STATE_FIELD_NAME);
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.SUB_DISTRICT_STATE,
                    SUB_DISTRICT_STATE_FIELD_NAME);
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.LOCALITY_STATE,
                    LOCALITY_STATE_FIELD_NAME);
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.MAP_AREA_STATE,
                    MAP_AREA_STATE_FIELD_NAME);
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.SECTOR_STATE,
                    SECTOR_STATE_FIELD_NAME);
            stateFieldNames
                    .put(ProjectActivityBuilder.BiokoHierarchy.HOUSEHOLD_STATE,
                            HOUSEHOLD_STATE_FIELD_NAME);
            stateFieldNames
                    .put(ProjectActivityBuilder.BiokoHierarchy.INDIVIDUAL_STATE,
                            INDIVIDUAL_STATE_FIELD_NAME);
            stateFieldNames.put(
                    ProjectActivityBuilder.BiokoHierarchy.BOTTOM_STATE,
                    BOTTOM_STATE_FIELD_NAME);

        }

        public static String getExtIdFieldNameFromState(String state) {
            if (stateFieldNames.containsKey(state)) {
                return stateFieldNames.get(state);
            } else {
                return null;
            }
        }
    }

    public static final class Locations {
        public static final String HIERERCHY_PARENT_UUID = "hierarchyParentUuid";
        public static final String HIERERCHY_UUID = "hierarchyUuid";
        public static final String HIERERCHY_EXTID = "hierarchyExtId";
        public static final String LOCATION_EXTID = "locationExtId";
        public static final String LOCATION_UUID = "locationUuid";
        public static final String LOCATION_NAME = "locationName";
        public static final String LOCATION_TYPE = "locationType";
        public static final String COMMUNITY_NAME = "communityName";
        public static final String COMMUNITY_CODE = "communityCode";
        public static final String MAP_AREA_NAME = "mapAreaName";
        public static final String LOCALITY_NAME = "localityName";
        public static final String SECTOR_NAME = "sectorName";

        public static final String BUILDING_NUMBER = "locationBuildingNumber";
        public static final String FLOOR_NUMBER = "locationFloorNumber";
        public static final String REGION_NAME = "regionName";
        public static final String PROVINCE_NAME = "provinceName";
        public static final String SUB_DISTRICT_NAME = "subDistrictName";
        public static final String DISTRICT_NAME = "districtName";

        public static final String STATUS = "status";
        public static final String DESCRIPTION = "description";
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";

        public static final String EVALUATION = "evaluation";

        private static Map<String, String> columnsToFieldNames = new HashMap<String, String>();

        static {
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_UUID, HIERERCHY_UUID);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_HIERARCHY_EXTID, HIERERCHY_EXTID);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_EXTID, LOCATION_EXTID);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_NAME, LOCATION_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_NAME, COMMUNITY_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_COMMUNITY_CODE, COMMUNITY_CODE);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_MAP_AREA_NAME, MAP_AREA_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_LOCALITY_NAME, LOCALITY_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_SECTOR_NAME, SECTOR_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_BUILDING_NUMBER, BUILDING_NUMBER);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_FLOOR_NUMBER, FLOOR_NUMBER);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_REGION_NAME, REGION_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_PROVINCE_NAME, PROVINCE_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_SUB_DISTRICT_NAME, SUB_DISTRICT_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_DISTRICT_NAME, DISTRICT_NAME);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_DESCRIPTION, DESCRIPTION);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_EVALUATION_STATUS, STATUS);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_LONGITUDE, LONGITUDE);
            columnsToFieldNames.put(OpenHDS.Locations.COLUMN_LOCATION_LATITUDE, LATITUDE);
            columnsToFieldNames.put(
                    OpenHDS.Locations.COLUMN_LOCATION_UUID,
                    General.ENTITY_UUID);



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




        // for individuals table
        public static final String INDIVIDUAL_EXTID = "individualExtId";
        public static final String INDIVIDUAL_UUID = "individualUuid";
        public static final String FIRST_NAME = "individualFirstName";
        public static final String LAST_NAME = "individualLastName";
        public static final String OTHER_NAMES = "individualOtherNames";
        public static final String DATE_OF_BIRTH = "individualDateOfBirth";
        public static final String AGE = "individualAge";
        public static final String AGE_UNITS = "individualAgeUnits";
        public static final String GENDER = "individualGender";
        public static final String PHONE_NUMBER = "individualPhoneNumber";
        public static final String OTHER_PHONE_NUMBER = "individualOtherPhoneNumber";
        public static final String POINT_OF_CONTACT_NAME = "individualPointOfContactName";
        public static final String POINT_OF_CONTACT_PHONE_NUMBER = "individualPointOfContactPhoneNumber";
        public static final String NATIONALITY = "individualNationality";
        public static final String LANGUAGE_PREFERENCE = "individualLanguagePreference";
        public static final String DIP = "individualDip";
        public static final String MOTHER_EXTID = "individualMotherExtId";
        public static final String FATHER_EXTID = "individualFatherExtId";


        public static final String RELATIONSHIP_TO_HEAD = "individualRelationshipToHeadOfHousehold";
        public static final String HEAD_PREFILLED_FLAG = "headPrefilledFlag";
        public static final String MEMBER_STATUS = "individualMemberStatus";

        public static final String HOUSEHOLD_UUID = "householdUuid";
        public static final String MEMBERSHIP_UUID = "membershipUuid";
        public static final String RELATIONSHIP_UUID = "relationshipUuid";
        public static final String SOCIALGROUP_UUID = "socialgroupUuid";

        public static final String IS_PREGNANT_FLAG = "individualIsPregnantFlag";

        private static Map<String, String> columnsToFieldNames = new HashMap<String, String>();

        static {
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID,
                    INDIVIDUAL_EXTID);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME,
                    FIRST_NAME);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, LAST_NAME);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES,
                    OTHER_NAMES);
            columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_DOB,
                    DATE_OF_BIRTH);
            columnsToFieldNames.put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE,
                    AGE);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_AGE_UNITS, AGE_UNITS);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_GENDER, GENDER);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER,
                    PHONE_NUMBER);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER,
                    OTHER_PHONE_NUMBER);
            columnsToFieldNames
                    .put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME,
                            POINT_OF_CONTACT_NAME);
            columnsToFieldNames
                    .put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER,
                            POINT_OF_CONTACT_PHONE_NUMBER);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE,
                    LANGUAGE_PREFERENCE);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_ID, DIP);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_MOTHER, MOTHER_EXTID);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_FATHER, FATHER_EXTID);
            columnsToFieldNames
                    .put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID,
                            HOUSEHOLD_UUID);
            columnsToFieldNames
                    .put(OpenHDS.Individuals.COLUMN_INDIVIDUAL_STATUS,
                            MEMBER_STATUS);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_NATIONALITY,
                    NATIONALITY);
            columnsToFieldNames.put(
                    OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID,
                    General.ENTITY_UUID);

        }

        public static String getFieldNameFromColumn(String column) {
            if (columnsToFieldNames.containsKey(column)) {
                return columnsToFieldNames.get(column);
            } else {
                return null;
            }
        }
    }

    public static final class Visits {
        public static final String VISIT_EXTID = "visitExtId";
        public static final String VISIT_UUID = "visitUuid";
        public static final String VISIT_DATE = "visitDate";
        public static final String LOCATION_UUID = "locationUuid";
        public static final String LOCATION_EXTID = "locationExtId";
        public static final String FIELDWORKER_UUID = "fieldWorkerUuid";

        private static Map<String, String> columnsToFieldNames = new HashMap<String, String>();

        static {
            columnsToFieldNames.put(
                    OpenHDS.Visits.COLUMN_VISIT_EXTID,
                    VISIT_EXTID);
            columnsToFieldNames.put(
                    OpenHDS.Visits.COLUMN_VISIT_UUID,
                    VISIT_UUID);
            columnsToFieldNames.put(
                    OpenHDS.Visits.COLUMN_VISIT_DATE,
                    VISIT_DATE);
            columnsToFieldNames.put(
                    OpenHDS.Visits.COLUMN_VISIT_LOCATION_UUID,
                    LOCATION_UUID);
            columnsToFieldNames.put(
                    OpenHDS.Visits.COLUMN_VISIT_FIELDWORKER_UUID,
                    FIELDWORKER_UUID);
        }

        public static String getFieldNameFromColumn(String column) {
            if (columnsToFieldNames.containsKey(column)) {
                return columnsToFieldNames.get(column);
            } else {
                return null;
            }
        }
    }

    public static final class InMigrations {
        public static final String IN_MIGRATION_TYPE = "migrationType";
        public static final String IN_MIGRATION_DATE = "migrationDate";
        public static final String IN_MIGRATION_INTERNAL = "internal_inmigration";
        public static final String IN_MIGRATION_EXTERNAL = "external_inmigration";
    }

    public static final class OutMigrations {
        public static final String OUT_MIGRATION_DATE = "outMigrationDate";
        public static final String OUT_MIGRATION_NAME_OF_DESITINATION = "outMigrationNameOfDestination";
        public static final String OUT_MIGRATION_REASON = "outMigrationReason";
    }

    public static final class PregnancyObservation {
        public static final String PREGNANCY_OBSERVATION_RECORDED_DATE = "recordedDate";
    }

    public static final class PregnancyOutcome{
        public static final String SOCIALGROUP_UUID = "socialgroupUuid";
        public static final String MOTHER_UUID = "motherUuid";
        public static final String FATHER_UUID = "fatherUuid";
    }

    public static final class BedNet {
        public static final String BED_NET_CODE = "netCode";
        public static final String HOUSEHOLD_SIZE = "householdSize";
        public static final String LOCATION_EXTID = "locationExtId";
        public static final String LOCATION_UUID = "locationUuid";
    }

}
