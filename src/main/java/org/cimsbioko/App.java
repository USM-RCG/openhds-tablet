package org.cimsbioko;

import android.app.Application;
import android.net.Uri;
import android.provider.BaseColumns;

public class App extends Application {

    public static final String AUTHORITY = "org.cimsbioko";
    private static final String SCHEME = "content://";
    public static final String CONTENT_BASE = SCHEME + AUTHORITY;
    public static final Uri CONTENT_BASE_URI = Uri.parse(SCHEME + AUTHORITY);

    private static App singleton;

    @Override
    public void onCreate() {
        singleton = this;
        super.onCreate();
    }

    public static App getApp() {
        return singleton;
    }

    public static final String DEFAULT_SORT_ORDER = "_id ASC";

    public static final class Individuals implements BaseColumns {

        public static final String TABLE_NAME = "individuals";

        private static final String PATH_NOTE_ID = "/individuals/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(CONTENT_BASE + PATH_NOTE_ID);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.individual";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.individual";

        public static final String COLUMN_INDIVIDUAL_UUID = "uuid";

        // general individual columns
        public static final String COLUMN_INDIVIDUAL_EXTID = "extId";
        public static final String COLUMN_INDIVIDUAL_FIRST_NAME = "firstName";
        public static final String COLUMN_INDIVIDUAL_FULL_NAME = "fullName";
        public static final String COLUMN_INDIVIDUAL_LAST_NAME = "lastName";
        public static final String COLUMN_INDIVIDUAL_DOB = "dob";
        public static final String COLUMN_INDIVIDUAL_GENDER = "gender";
        public static final String COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID = "currentResidence";
        public static final String COLUMN_INDIVIDUAL_ATTRS = "attrs";

        // extensions for bioko project
        public static final String COLUMN_INDIVIDUAL_OTHER_ID = "otherId";
        public static final String COLUMN_INDIVIDUAL_OTHER_NAMES = "otherNames";
        public static final String COLUMN_INDIVIDUAL_PHONE_NUMBER = "phoneNumber";
        public static final String COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER = "otherPhoneNumber";
        public static final String COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME = "pointOfContactName";
        public static final String COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER = "pointOfContactPhoneNumber";
        public static final String COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE = "languagePreference";
        public static final String COLUMN_INDIVIDUAL_STATUS = "status";
        public static final String COLUMN_INDIVIDUAL_NATIONALITY = "nationality";
        public static final String COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD = "relationshipToHead";
    }

    public static final class Locations implements BaseColumns {

        public static final String TABLE_NAME = "locations";

        private static final String PATH_NOTE_ID = "/locations/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(CONTENT_BASE + PATH_NOTE_ID);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.location";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.location";

        public static final String COLUMN_LOCATION_UUID = "uuid";
        public static final String COLUMN_LOCATION_EXTID = "extId";
        public static final String COLUMN_LOCATION_NAME = "name";
        public static final String COLUMN_LOCATION_LATITUDE = "latitude";
        public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
        public static final String COLUMN_LOCATION_HIERARCHY_UUID = "hierarchyUuid";
        public static final String COLUMN_LOCATION_MAP_AREA_NAME = "mapAreaName";
        public static final String COLUMN_LOCATION_SECTOR_NAME = "sectorName";
        public static final String COLUMN_LOCATION_BUILDING_NUMBER = "buildingNumber";
        public static final String COLUMN_LOCATION_DESCRIPTION = "description";
        public static final String COLUMN_LOCATION_ATTRS = "attrs";
    }

    public static final class HierarchyItems implements BaseColumns {

        public static final String TABLE_NAME = "hierarchyitems";

        private static final String PATH_NOTE_ID = "/hierarchyitems/";

        public static final int NOTE_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(CONTENT_BASE + PATH_NOTE_ID);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.hierarchyitem";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.hierarchyitem";

        public static final String COLUMN_HIERARCHY_UUID = "uuid";

        public static final String COLUMN_HIERARCHY_EXTID = "extId";
        public static final String COLUMN_HIERARCHY_NAME = "name";
        public static final String COLUMN_HIERARCHY_PARENT = "parent";
        public static final String COLUMN_HIERARCHY_LEVEL = "level";
        public static final String COLUMN_HIERARCHY_ATTRS = "attrs";
    }

    public static final class FieldWorkers implements BaseColumns {

        public static final String TABLE_NAME = "fieldworkers";

        private static final String PATH_NOTE_ID = "/fieldworkers/";

        public static final int ID_PATH_POSITION = 1;

        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(CONTENT_BASE + PATH_NOTE_ID);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.fieldworker";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.fieldworker";

        public static final String COLUMN_FIELD_WORKER_UUID = "uuid";

        public static final String COLUMN_FIELD_WORKER_EXTID = "extId";
        public static final String COLUMN_FIELD_WORKER_ID_PREFIX = "idPrefix";
        public static final String COLUMN_FIELD_WORKER_PASSWORD = "password";
        public static final String COLUMN_FIELD_WORKER_FIRST_NAME = "firstName";
        public static final String COLUMN_FIELD_WORKER_LAST_NAME = "lastName";
    }

}
