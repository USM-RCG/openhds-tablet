package org.cimsbioko.navconfig

import android.os.Parcel
import android.os.Parcelable
import org.cimsbioko.data.DataWrapper
import org.cimsbioko.navconfig.NavigatorConfig.Companion.instance
import org.cimsbioko.navconfig.db.DefaultQueryHelper
import org.cimsbioko.navconfig.db.QueryHelper
import java.util.*
import java.util.regex.Pattern

/**
 * Represents a path in the navigation hierarchy that can be stored and retrieved automatically by Android.
 */
class HierarchyPath(path: Map<String, DataWrapper> = emptyMap()) : Parcelable {

    private val p: MutableMap<String, DataWrapper> = path.toMutableMap()

    /**
     * @return a deep copy of this hierarchy path
     */
    fun clone(): HierarchyPath = HierarchyPath(path = p)

    /**
     * @return the named levels in the path in traversal order.
     */
    val levels: List<String>
        get() = p.keys.toList()

    /**
     * @return the named levels in the path in traversal order.
     */
    val path: List<DataWrapper>
        get() = p.values.toList()

    /**
     * Fetches the value at the specified level in the path.
     *
     * @param level the named level of interest
     * @return the value in the path at the specified level
     */
    operator fun get(level: String): DataWrapper? = p[level]

    /**
     * Traverses one level deeper in the hierarchy.
     *
     * @param level the name of the level to add
     * @param value the value to add at the level
     */
    fun down(level: String, value: DataWrapper) {
        p[level] = value
    }

    /**
     * After calling this, the path will only contain values leading up to the named level.
     *
     * @param level the level to traverse up to (the new path leaf)
     */
    fun truncate(level: String) {
        var seenLevel = false
        with(p.entries.iterator()) {
            while (hasNext()) {
                val pathElem = next()
                if (!seenLevel && pathElem.key == level) seenLevel = true
                if (seenLevel) remove()
            }
        }
    }

    /**
     * Resets to an empty path.
     */
    fun clear() = p.clear()

    /**
     * @return the depth of the hierarchy path
     */
    fun depth(): Int = p.size

    override fun toString(): String = leafValue?.let { "${it.category}:${it.uuid}" } ?: ""

    private val leafValue: DataWrapper?
        get() = p.values.lastOrNull()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is HierarchyPath -> false
        else -> p == other.p
    }

    override fun hashCode(): Int = toString().hashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(depth())
        dest.writeStringArray(p.keys.toTypedArray())
        dest.writeTypedArray(p.values.toTypedArray(), 0)
    }

    private class Creator : Parcelable.Creator<HierarchyPath> {
        override fun createFromParcel(source: Parcel): HierarchyPath {
            val depth = source.readInt()
            val levels = arrayOfNulls<String>(depth)
            val values = arrayOfNulls<DataWrapper>(depth)
            source.readStringArray(levels)
            source.readTypedArray(values, DataWrapper.CREATOR)
            return HierarchyPath().apply {
                for (l in 0 until depth) {
                    down(levels[l]!!, values[l]!!)
                }
            }
        }

        override fun newArray(size: Int): Array<HierarchyPath?> = arrayOfNulls(size)
    }

    companion object {

        private const val PATH_SEPARATOR = "|"
        private val LEAF_PATTERN = Pattern.compile("(\\w+):([a-f0-9]+)")

        fun fromString(pathStr: String?): HierarchyPath? = pathStr?.let { path ->
            with(LEAF_PATTERN.matcher(path)) {
                if (matches()) DefaultQueryHelper[group(1), group(2)]?.let { fromLeafString(it) }
                else fromPathString(path)
            }
        }

        private fun fromPathString(pathStr: String): HierarchyPath? {
            val helper: QueryHelper = DefaultQueryHelper
            val levels = instance.levels
            return pathStr.split("[$PATH_SEPARATOR]".toRegex()).toTypedArray().let { path ->
                if (path.size <= levels.size) {
                    HierarchyPath().apply {
                        for ((i, p) in path.withIndex()) {
                            levels[i].let { level ->
                                helper[level, p]?.let { value ->
                                    down(level, value)
                                } ?: return null
                            }
                        }
                    }
                } else null
            }
        }

        private fun fromLeafString(leaf: DataWrapper): HierarchyPath? {
            val helper: QueryHelper = DefaultQueryHelper
            // Traverse up the hierarchy using child-parent relationships, tracking the nodes traversed
            val traversed = Stack<DataWrapper>().apply { push(leaf) }
            var child: DataWrapper = leaf
            var parent: DataWrapper?
            do {
                parent = child.let { helper.getParent(it.category, it.uuid) }?.also {
                    traversed.push(it)
                    child = it
                }
            } while (parent != null)

            // ...then reconstruct a path from the traversed nodes if it reached the top of the hierarchy
            return if (instance.topLevel == traversed.peek().category) {
                HierarchyPath().apply { while (traversed.isNotEmpty()) traversed.pop().let { down(it.category, it) } }
            } else null
        }

        @JvmField
        val CREATOR: Parcelable.Creator<HierarchyPath> = Creator()
    }
}