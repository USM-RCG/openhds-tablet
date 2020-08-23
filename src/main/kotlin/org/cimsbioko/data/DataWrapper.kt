package org.cimsbioko.data

import android.os.Parcel
import android.os.Parcelable
import org.cimsbioko.model.core.HierarchyItem
import org.cimsbioko.navconfig.UsedByJSConfig
import org.cimsbioko.navconfig.db.DefaultQueryHelper

/**
 * Generic representation of a result from any query.
 *
 * Facilitates generic lists and views of results, for example at various levels of
 * hierarchy navigation.  But it's up to the caller to interpret the QueryResult
 * correctly, for example using the extId and "category" (i.e. hierarchy level).
 *
 * Payloads may contain arbitrary data, for example to display with result name and extId.
 */
data class DataWrapper(
        val uuid: String,
        val category: String,
        val extId: String,
        @set:UsedByJSConfig
        var name: String
) : Parcelable {

    val unwrapped: HierarchyItem?
        get() = DefaultQueryHelper[category, uuid]?.first

    val hierarchyId: String
        get() = "$category:$uuid"

    private constructor(parcel: Parcel) : this(
            category = parcel.readString()!!,
            extId = parcel.readString()!!,
            name = parcel.readString()!!,
            uuid = parcel.readString()!!
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(parcel) {
            writeString(category)
            writeString(extId)
            writeString(name)
            writeString(uuid)
        }
    }

    class Creator : Parcelable.Creator<DataWrapper?> {
        override fun createFromParcel(`in`: Parcel): DataWrapper? = DataWrapper(`in`)
        override fun newArray(size: Int): Array<DataWrapper?> = arrayOfNulls(size)
    }

    companion object {
        @JvmField
        val CREATOR = Creator()
    }
}