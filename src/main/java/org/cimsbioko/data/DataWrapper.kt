package org.cimsbioko.data

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
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
        val name: String
) : Parcelable {

    var stringsPayload: MutableMap<Int, String?> = HashMap()
    var stringIdsPayload: MutableMap<Int, Int> = HashMap()

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
        stringIdsPayload = HashMap<Int, Int>().also { map ->
            ArrayList<Int>().also { parcel.readList(it, null) }.let { list ->
                parcel.readBundle(DataWrapper::class.java.classLoader)?.let { bundle ->
                    for (key in list) {
                        map[key] = bundle.getInt(key.toString())
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
            stringIdsPayload.keys.toList().let { list ->
                writeList(list)
                writeBundle(Bundle().apply { for (key in list) putInt(key.toString(), stringIdsPayload[key] ?: 0) })
            }
        }
    }

    // for Parcelable
    class Creator : Parcelable.Creator<DataWrapper?> {
        override fun createFromParcel(`in`: Parcel): DataWrapper? = DataWrapper(`in`)
        override fun newArray(size: Int): Array<DataWrapper?> = arrayOfNulls(size)
    }

    companion object {

        @JvmStatic
        fun getByHierarchyId(hierId: String): DataWrapper? = hierId.split(":".toRegex()).let {
            if (it.size > 2) {
                val (level, uuid) = it
                get(level, uuid)
            } else null
        }

        @JvmField
        val CREATOR = Creator()
    }
}