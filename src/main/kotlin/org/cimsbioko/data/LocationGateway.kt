package org.cimsbioko.data

import android.content.ContentValues
import android.database.Cursor
import org.cimsbioko.App.Locations
import org.cimsbioko.R
import org.cimsbioko.data.CursorConvert.extractString
import org.cimsbioko.model.core.Location
import org.cimsbioko.navconfig.Hierarchy

/**
 * Convert Locations to and from database.  Location-specific queries.
 */
class LocationGateway internal constructor()
    : Gateway<Location>(Locations.CONTENT_ID_URI_BASE, Locations.COLUMN_LOCATION_UUID) {

    fun findByHierarchy(hierarchyId: String): Query<Location> =
            Query(this, tableUri, Locations.COLUMN_LOCATION_HIERARCHY_UUID, hierarchyId, Locations.COLUMN_LOCATION_EXTID)

    override fun getId(entity: Location): String = entity.uuid!!

    override val entityConverter: CursorConverter<Location> by lazy { LocationEntityConverter() }
    override val wrapperConverter: CursorConverter<DataWrapper> by lazy { LocationWrapperConverter() }
    override val contentValuesConverter: ContentValuesConverter<Location> by lazy { LocationContentValuesConverter() }
}

private class LocationEntityConverter : CursorConverter<Location> {
    override fun convert(c: Cursor): Location = Location().apply {
        uuid = extractString(c, Locations.COLUMN_LOCATION_UUID)
        extId = extractString(c, Locations.COLUMN_LOCATION_EXTID)
        hierarchyUuid = extractString(c, Locations.COLUMN_LOCATION_HIERARCHY_UUID)
        latitude = extractString(c, Locations.COLUMN_LOCATION_LATITUDE)
        longitude = extractString(c, Locations.COLUMN_LOCATION_LONGITUDE)
        name = extractString(c, Locations.COLUMN_LOCATION_NAME)
        description = extractString(c, Locations.COLUMN_LOCATION_DESCRIPTION)
        longitude = extractString(c, Locations.COLUMN_LOCATION_LONGITUDE)
        latitude = extractString(c, Locations.COLUMN_LOCATION_LATITUDE)
        attrs = extractString(c, Locations.COLUMN_LOCATION_ATTRS)
    }
}

private class LocationWrapperConverter : CursorConverter<DataWrapper> {
    override fun convert(c: Cursor): DataWrapper = DataWrapper(
            extractString(c, Locations.COLUMN_LOCATION_UUID)!!,
            Hierarchy.HOUSEHOLD,
            extractString(c, Locations.COLUMN_LOCATION_EXTID)!!,
            extractString(c, Locations.COLUMN_LOCATION_NAME)!!
    ).apply {
        stringsPayload[R.string.location_description_label] = extractString(c, Locations.COLUMN_LOCATION_DESCRIPTION)
    }
}

private class LocationContentValuesConverter : ContentValuesConverter<Location> {
    override fun toContentValues(entity: Location): ContentValues = ContentValues().apply {
        with(entity) {
            put(Locations.COLUMN_LOCATION_UUID, uuid)
            put(Locations.COLUMN_LOCATION_EXTID, extId)
            put(Locations.COLUMN_LOCATION_HIERARCHY_UUID, hierarchyUuid)
            put(Locations.COLUMN_LOCATION_LATITUDE, latitude)
            put(Locations.COLUMN_LOCATION_LONGITUDE, longitude)
            put(Locations.COLUMN_LOCATION_NAME, name)
            put(Locations.COLUMN_LOCATION_DESCRIPTION, description)
            put(Locations.COLUMN_LOCATION_LONGITUDE, longitude)
            put(Locations.COLUMN_LOCATION_LATITUDE, latitude)
            put(Locations.COLUMN_LOCATION_ATTRS, attrs)
        }
    }
}