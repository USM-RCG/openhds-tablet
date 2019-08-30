package org.cimsbioko.navconfig;


public class ProjectFormFields {

    public static final class General {

        public static final String COLLECTION_DATE_TIME = "collectionDateTime";
        public static final String FIELD_WORKER_UUID = "fieldWorkerUuid";
        public static final String ENTITY_UUID = "entityUuid";
        public static final String ENTITY_EXTID = "entityExtId";
        public static final String NEEDS_REVIEW = "needsReview";
        public static final String FIELD_WORKER_EXTID = "fieldWorkerExtId";

        public static final String DISTRIBUTION_DATE_TIME = "distributionDateTime";
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
        public static final String HOUSEHOLD_EXTID = "householdExtId";
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
