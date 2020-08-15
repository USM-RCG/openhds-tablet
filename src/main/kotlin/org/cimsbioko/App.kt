package org.cimsbioko

import android.app.Application
import android.net.Uri
import android.provider.BaseColumns
import org.cimsbioko.scripting.ContextFactory.Companion.register

class App : Application() {

    override fun onCreate() {
        instance = this
        super.onCreate()
        register()
    }

    object Individuals {

        val CONTENT_ID_URI_BASE: Uri = Uri.parse("$CONTENT_BASE/individuals/")
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.individual"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.individual"

        const val TABLE_NAME = "individuals"
        const val COLUMN_INDIVIDUAL_UUID = "uuid"

        const val ID = BaseColumns._ID

        // general individual columns
        const val COLUMN_INDIVIDUAL_EXTID = "extId"
        const val COLUMN_INDIVIDUAL_FIRST_NAME = "firstName"
        const val COLUMN_INDIVIDUAL_LAST_NAME = "lastName"
        const val COLUMN_INDIVIDUAL_DOB = "dob"
        const val COLUMN_INDIVIDUAL_GENDER = "gender"
        const val COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID = "currentResidence"
        const val COLUMN_INDIVIDUAL_ATTRS = "attrs"

        // extensions for bioko project
        const val COLUMN_INDIVIDUAL_OTHER_ID = "otherId"
        const val COLUMN_INDIVIDUAL_OTHER_NAMES = "otherNames"
        const val COLUMN_INDIVIDUAL_PHONE_NUMBER = "phoneNumber"
        const val COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER = "otherPhoneNumber"
        const val COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME = "pointOfContactName"
        const val COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER = "pointOfContactPhoneNumber"
        const val COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE = "languagePreference"
        const val COLUMN_INDIVIDUAL_STATUS = "status"
        const val COLUMN_INDIVIDUAL_NATIONALITY = "nationality"
        const val COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD = "relationshipToHead"
    }

    object Locations : BaseColumns {

        val CONTENT_ID_URI_BASE: Uri = Uri.parse("$CONTENT_BASE/locations/")
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.location"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.location"

        const val TABLE_NAME = "locations"

        const val ID = BaseColumns._ID
        const val COLUMN_LOCATION_UUID = "uuid"
        const val COLUMN_LOCATION_EXTID = "extId"
        const val COLUMN_LOCATION_NAME = "name"
        const val COLUMN_LOCATION_LATITUDE = "latitude"
        const val COLUMN_LOCATION_LONGITUDE = "longitude"
        const val COLUMN_LOCATION_HIERARCHY_UUID = "hierarchyUuid"
        const val COLUMN_LOCATION_DESCRIPTION = "description"
        const val COLUMN_LOCATION_ATTRS = "attrs"
    }

    object HierarchyItems : BaseColumns {

        val CONTENT_ID_URI_BASE: Uri = Uri.parse("$CONTENT_BASE/hierarchyitems/")
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.hierarchyitem"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.hierarchyitem"

        const val TABLE_NAME = "hierarchyitems"

        const val ID = BaseColumns._ID
        const val COLUMN_HIERARCHY_UUID = "uuid"
        const val COLUMN_HIERARCHY_EXTID = "extId"
        const val COLUMN_HIERARCHY_NAME = "name"
        const val COLUMN_HIERARCHY_PARENT = "parent"
        const val COLUMN_HIERARCHY_LEVEL = "level"
        const val COLUMN_HIERARCHY_ATTRS = "attrs"
    }

    object FieldWorkers : BaseColumns {

        val CONTENT_ID_URI_BASE: Uri = Uri.parse("$CONTENT_BASE/fieldworkers/")
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cims.fieldworker"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.cims.fieldworker"

        const val TABLE_NAME = "fieldworkers"

        const val ID = BaseColumns._ID
        const val COLUMN_FIELD_WORKER_UUID = "uuid"
        const val COLUMN_FIELD_WORKER_EXTID = "extId"
        const val COLUMN_FIELD_WORKER_ID_PREFIX = "idPrefix"
        const val COLUMN_FIELD_WORKER_PASSWORD = "password"
        const val COLUMN_FIELD_WORKER_FIRST_NAME = "firstName"
        const val COLUMN_FIELD_WORKER_LAST_NAME = "lastName"
    }

    companion object {

        private const val SCHEME = "content://"
        const val AUTHORITY = "org.cimsbioko"
        const val CONTENT_BASE = "$SCHEME$AUTHORITY"

        const val DEFAULT_SORT_ORDER = "_id ASC"

        lateinit var instance: App
            private set

        fun getApp() = instance

        val CONTENT_BASE_URI: Uri = Uri.parse(CONTENT_BASE)
    }
}