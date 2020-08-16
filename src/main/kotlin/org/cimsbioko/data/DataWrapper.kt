package org.cimsbioko.data

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import org.cimsbioko.navconfig.UsedByJSConfig
import org.cimsbioko.navconfig.db.DefaultQueryHelper.get
import java.util.*

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

    var stringsPayload: MutableMap<Int, String?> = HashMap()

    override fun toString(): String =
            "QueryResult[name: $name extId: $extId category: $category + payload size: ${stringsPayload.size}]"

    val hierarchyId: String
        get() = "$category:$uuid"

    private constructor(parcel: Parcel) : this(
            category = parcel.readString()!!,
            extId = parcel.readString()!!,
            name = parcel.readString()!!,
            uuid = parcel.readString()!!
    ) {
        stringsPayload = HashMap<Int, String?>().also { map ->
            ArrayList<Int>().also { parcel.readList(it, null) }.let { list ->
                parcel.readBundle(DataWrapper::class.java.classLoader)?.let { bundle ->
                    for (key in list) {
                        map[key] = bundle.getString(key.toString())
                    }
                }
            }
        }
    }

    // for Parcelable
    override fun describeContents(): Int = 0

    // for Parcelable
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(parcel) {
            writeString(category)
            writeString(extId)
            writeString(name)
            writeString(uuid)
            stringsPayload.keys.toList().let { list ->
                writeList(list)
                writeBundle(Bundle().apply { for (key in list) putString(key.toString(), stringsPayload[key]) })
            }
        }
    }

    // for Parcelable
    class Creator : Parcelable.Creator<DataWrapper?> {
        override fun createFromParcel(`in`: Parcel): DataWrapper? = DataWrapper(`in`)
        override fun newArray(size: Int): Array<DataWrapper?> = arrayOfNulls(size)
    }

    companion object {

        fun getByHierarchyId(id: String): DataWrapper? = id.split(":".toRegex()).let { parts ->
            if (parts.size == 2) parts.let { (level, uuid) -> get(level, uuid) } else null
        }

        @JvmField
        val CREATOR = Creator()
    }
}