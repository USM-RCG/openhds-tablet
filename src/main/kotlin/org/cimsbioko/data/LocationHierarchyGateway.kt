package org.cimsbioko.data

import android.content.ContentValues
import android.database.Cursor
import org.cimsbioko.App.HierarchyItems
import org.cimsbioko.data.CursorConvert.extractString
import org.cimsbioko.model.core.LocationHierarchy

/**
 * Convert LocationHierarchy items to and from database.  LocationHierarchy-specific queries.
 */
class LocationHierarchyGateway internal constructor()
    : Gateway<LocationHierarchy>(HierarchyItems.CONTENT_ID_URI_BASE, HierarchyItems.COLUMN_HIERARCHY_UUID) {

    fun findByLevel(level: String?): Query<LocationHierarchy> =
            Query(this, tableUri, HierarchyItems.COLUMN_HIERARCHY_LEVEL, level, HierarchyItems.COLUMN_HIERARCHY_UUID)

    fun findByParent(parentId: String?): Query<LocationHierarchy> =
            Query(this, tableUri, HierarchyItems.COLUMN_HIERARCHY_PARENT, parentId, HierarchyItems.COLUMN_HIERARCHY_EXTID)

    override fun getId(entity: LocationHierarchy): String = entity.uuid

    override val entityConverter: CursorConverter<LocationHierarchy> by lazy { LocationHierarchyEntityConverter() }
    override val wrapperConverter: CursorConverter<DataWrapper> by lazy { LocationHierarchyWrapperConverter() }
    override val contentValuesConverter: ContentValuesConverter<LocationHierarchy> by lazy { LocationHierarchyContentValuesConverter() }
}

private class LocationHierarchyEntityConverter : CursorConverter<LocationHierarchy> {
    override fun convert(c: Cursor): LocationHierarchy = LocationHierarchy(
            uuid = extractString(c, HierarchyItems.COLUMN_HIERARCHY_UUID)!!,
            extId = extractString(c, HierarchyItems.COLUMN_HIERARCHY_EXTID)!!,
            name = extractString(c, HierarchyItems.COLUMN_HIERARCHY_NAME)!!,
            level = extractString(c, HierarchyItems.COLUMN_HIERARCHY_LEVEL)!!
    ).apply {
        parentUuid = extractString(c, HierarchyItems.COLUMN_HIERARCHY_PARENT)
        attrs = extractString(c, HierarchyItems.COLUMN_HIERARCHY_ATTRS)
    }
}

private class LocationHierarchyWrapperConverter : CursorConverter<DataWrapper> {
    override fun convert(c: Cursor): DataWrapper {
        return DataWrapper(
                extractString(c, HierarchyItems.COLUMN_HIERARCHY_UUID)!!,
                extractString(c, HierarchyItems.COLUMN_HIERARCHY_LEVEL)!!,
                extractString(c, HierarchyItems.COLUMN_HIERARCHY_EXTID)!!,
                extractString(c, HierarchyItems.COLUMN_HIERARCHY_NAME)!!
        )
    }
}

private class LocationHierarchyContentValuesConverter : ContentValuesConverter<LocationHierarchy> {
    override fun toContentValues(entity: LocationHierarchy): ContentValues {
        val contentValues = ContentValues()
        contentValues.put(HierarchyItems.COLUMN_HIERARCHY_UUID, entity.uuid)
        contentValues.put(HierarchyItems.COLUMN_HIERARCHY_EXTID, entity.extId)
        contentValues.put(HierarchyItems.COLUMN_HIERARCHY_NAME, entity.name)
        contentValues.put(HierarchyItems.COLUMN_HIERARCHY_LEVEL, entity.level)
        contentValues.put(HierarchyItems.COLUMN_HIERARCHY_PARENT, entity.parentUuid)
        contentValues.put(HierarchyItems.COLUMN_HIERARCHY_ATTRS, entity.attrs)
        return contentValues
    }
}