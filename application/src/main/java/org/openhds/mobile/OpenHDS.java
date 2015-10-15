package org.openhds.mobile;

import android.net.Uri;
import android.provider.BaseColumns;

public class OpenHDS {
	public static final String AUTHORITY = "org.openhds.Application";

	private OpenHDS() {
	}

	public static final String DEFAULT_SORT_ORDER = "_id ASC";

	public static final class Individuals implements BaseColumns {

		private Individuals() {
		}

		public static final String TABLE_NAME = "individuals";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/individuals";
		private static final String PATH_NOTE_ID = "/individuals/";
		private static final String PATH_SG = "/individuals/sg/";

		public static final int NOTE_ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_SG_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SG);
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.individual";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.individual";

        public static final String COLUMN_INDIVIDUAL_UUID = "uuid";

		// general individual columns
		public static final String COLUMN_INDIVIDUAL_EXTID = "extId";
		public static final String COLUMN_INDIVIDUAL_FIRST_NAME = "firstName";
		public static final String COLUMN_INDIVIDUAL_FULL_NAME = "fullName";
		public static final String COLUMN_INDIVIDUAL_LAST_NAME = "lastName";
		public static final String COLUMN_INDIVIDUAL_DOB = "dob";
		public static final String COLUMN_INDIVIDUAL_GENDER = "gender";
		public static final String COLUMN_INDIVIDUAL_MOTHER = "mother";
		public static final String COLUMN_INDIVIDUAL_FATHER = "father";
		public static final String COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID = "currentResidence";
		public static final String COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE = "endType";

		// extensions for bioko project
		public static final String COLUMN_INDIVIDUAL_OTHER_ID = "otherId";
		public static final String COLUMN_INDIVIDUAL_OTHER_NAMES = "otherNames";
		public static final String COLUMN_INDIVIDUAL_AGE = "age";
		public static final String COLUMN_INDIVIDUAL_AGE_UNITS = "ageUnits";
		public static final String COLUMN_INDIVIDUAL_PHONE_NUMBER = "phoneNumber";
		public static final String COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER = "otherPhoneNumber";
		public static final String COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME = "pointOfContactName";
		public static final String COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER = "pointOfContactPhoneNumber";
		public static final String COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE = "languagePreference";
		public static final String COLUMN_INDIVIDUAL_STATUS = "memberStatus";
        public static final String COLUMN_INDIVIDUAL_NATIONALITY = "nationality";
	}

	public static final class Locations implements BaseColumns {
		public static final String TABLE_NAME = "locations";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/locations";
		private static final String PATH_NOTE_ID = "/locations/";

		public static final int NOTE_ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.location";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.location";

        public static final String COLUMN_LOCATION_UUID = "uuid";

		public static final String COLUMN_LOCATION_EXTID = "extId";
		public static final String COLUMN_LOCATION_NAME = "name";
		public static final String COLUMN_LOCATION_LATITUDE = "latitude";
		public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
		public static final String COLUMN_LOCATION_HIERARCHY_UUID = "hierarchyUuid";
		public static final String COLUMN_LOCATION_HIERARCHY_EXTID = "hierarchyExtId";
        public static final String COLUMN_LOCATION_COMMUNITY_NAME = "communityName";
        public static final String COLUMN_LOCATION_COMMUNITY_CODE = "communityCode";
        public static final String COLUMN_LOCATION_LOCALITY_NAME = "localityName";
        public static final String COLUMN_LOCATION_MAP_AREA_NAME = "mapAreaName";
        public static final String COLUMN_LOCATION_SECTOR_NAME = "sectorName";
        public static final String COLUMN_LOCATION_BUILDING_NUMBER = "buildingNumber";
        public static final String COLUMN_LOCATION_FLOOR_NUMBER = "floorNumber";
        public static final String COLUMN_LOCATION_REGION_NAME = "regionName";
        public static final String COLUMN_LOCATION_PROVINCE_NAME = "provinceName";
        public static final String COLUMN_LOCATION_SUB_DISTRICT_NAME = "subDistrictName";
        public static final String COLUMN_LOCATION_DISTRICT_NAME = "districtName";
        public static final String COLUMN_LOCATION_DESCRIPTION = "description";
        public static final String COLUMN_LOCATION_EVALUATION_STATUS = "evaluationStatus";

        public static final String COLUMN_LOCATION_HAS_RECIEVED_BEDNETS= "hasRecievedBedNets";
		public static final String COLUMN_LOCATION_SPRAYING_EVALUATION = "sprayingEvaluation";
    }

	public static final class HierarchyItems implements BaseColumns {
		public static final String TABLE_NAME = "hierarchyitems";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/hierarchyitems";
		private static final String PATH_NOTE_ID = "/hierarchyitems/";

		public static final int NOTE_ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.hierarchyitem";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.hierarchyitem";

        public static final String COLUMN_HIERARCHY_UUID = "uuid";

		public static final String COLUMN_HIERARCHY_EXTID = "extId";
		public static final String COLUMN_HIERARCHY_NAME = "name";
		public static final String COLUMN_HIERARCHY_PARENT = "parent";
		public static final String COLUMN_HIERARCHY_LEVEL = "level";
	}

	public static final class Visits implements BaseColumns {
		public static final String TABLE_NAME = "visits";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/visits";
		private static final String PATH_NOTE_ID = "/visits/";

		public static final int ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.visit";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.visit";

        public static final String COLUMN_VISIT_UUID = "uuid";

		public static final String COLUMN_VISIT_EXTID = "extId";
		public static final String COLUMN_VISIT_DATE = "date";
		public static final String COLUMN_VISIT_LOCATION_UUID = "location_uuid";
		public static final String COLUMN_VISIT_FIELDWORKER_UUID = "fieldWorkerUuid";

	}

	public static final class Relationships implements BaseColumns {
		public static final String TABLE_NAME = "relationships";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/relationships";
		private static final String PATH_NOTE_ID = "/relationships/";

		public static final int ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.relationship";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.relationship";

        public static final String COLUMN_RELATIONSHIP_UUID = "uuid";

		public static final String COLUMN_RELATIONSHIP_INDIVIDUAL_A = "individualA";
		public static final String COLUMN_RELATIONSHIP_INDIVIDUAL_B = "individualB";
		public static final String COLUMN_RELATIONSHIP_TYPE = "relationshipType";
		public static final String COLUMN_RELATIONSHIP_STARTDATE = "startDate";
	}

	public static final class FieldWorkers implements BaseColumns {
		public static final String TABLE_NAME = "fieldworkers";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/fieldworkers";
		private static final String PATH_NOTE_ID = "/fieldworkers/";

		public static final int ID_PATH_POSITION = 1;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.fieldworker";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.fieldworker";

        public static final String COLUMN_FIELD_WORKER_UUID = "uuid";

		public static final String COLUMN_FIELD_WORKER_EXTID = "extId";
        public static final String COLUMN_FIELD_WORKER_ID_PREFIX = "idPrefix";
		public static final String COLUMN_FIELD_WORKER_PASSWORD = "password";
		public static final String COLUMN_FIELD_WORKER_FIRST_NAME = "firstName";
		public static final String COLUMN_FIELD_WORKER_LAST_NAME = "lastName";
	}

	public static final class SocialGroups implements BaseColumns {
		public static final String TABLE_NAME = "socialgroups";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/socialgroups";
		private static final String PATH_LOCATION_ID = "/socialgroups/location/";
		private static final String PATH_INDIVIDUAL_ID = "/socialgroups/individual/";
		private static final String PATH_NOTE_ID = "/socialgroups/";

		public static final int ID_PATH_POSITION = 1;
		public static final int LOCATION_PATH_POSITION = 2;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_LOCATION_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY
				+ PATH_LOCATION_ID);
		public static final Uri CONTENT_LOCATION_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY
				+ PATH_LOCATION_ID + "/*");
		public static final Uri CONTENT_INDIVIDUAL_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY
				+ PATH_INDIVIDUAL_ID);
		public static final Uri CONTENT_INDIVIDUAL_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY
				+ PATH_INDIVIDUAL_ID + "/*");

		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.socialgroups";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.socialgroups";

        public static final String COLUMN_SOCIAL_GROUP_UUID = "uuid";

        public static final String COLUMN_LOCATION_UUID = "location_uuid";

		public static final String COLUMN_SOCIAL_GROUP_NAME = "groupName";
		public static final String COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID = "groupHead_uuid";
	}

	public static final class Memberships implements BaseColumns {
		public static final String TABLE_NAME = "memberships";
		private static final String SCHEME = "content://";

		private static final String PATH_NOTES = "/memberships";
		private static final String PATH_NOTE_ID = "/memberships/";

		public static final int ID_PATH_POSITION = 2;

		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openhds.memberships";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openhds.memberships";

        public static final String COLUMN_MEMBERSHIP_UUID = "uuid";

		public static final String COLUMN_SOCIAL_GROUP_UUID = "socialGroup_uuid";
		public static final String COLUMN_INDIVIDUAL_UUID = "individual_uuid";
		public static final String COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD = "relationshipToHead";
	}





}
