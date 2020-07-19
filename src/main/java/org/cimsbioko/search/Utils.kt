package org.cimsbioko.search

import android.content.Context
import org.apache.lucene.search.spell.JaroWinklerDistance
import org.cimsbioko.R
import org.cimsbioko.utilities.ConfigUtils.getSharedPrefs
import java.util.*

object Utils {

    private const val MAX_SIMILARITY = 0.99f
    private val jwd = JaroWinklerDistance()

    @JvmStatic
    fun extractUniquePhones(phoneValue: String): Set<String> = phoneValue.trim { it <= ' ' }.split("\\s+".toRegex()).toSet()

    @JvmStatic
    fun extractDissimilarNames(nameValue: String): Set<String> {

        fun MutableSet<String>.addIfDissimilar(value: String) {
            for (v in this) {
                if (jwd.getDistance(v, value) > MAX_SIMILARITY) return
            }
            add(value)
        }

        val names: MutableSet<String> = HashSet()
        for (name in nameValue.trim { it <= ' ' }.toLowerCase(Locale.getDefault()).split("\\s+".toRegex())) {
            names.addIfDissimilar(name.replace("\\W+".toRegex(), ""))
        }
        return names
    }

    @JvmStatic
    fun join(values: Set<String>, separator: String) = buildString {
        for (name in values) {
            if (isNotEmpty()) {
                append(separator)
            }
            append(name)
        }
    }

    /**
     * Returns whether automatic database re-indexing is enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true if auto-update is enabled, otherwise false
     */
    @JvmStatic
    fun isAutoReindexingEnabled(ctx: Context) = isSearchEnabled(ctx) &&
            getSharedPrefs(ctx).getBoolean(ctx.getString(R.string.auto_index_on_db_update_key), false)

    /**
     * Returns whether full-text search is enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true is search is enabled, otherwise false
     */
    @JvmStatic
    fun isSearchEnabled(ctx: Context): Boolean = getSharedPrefs(ctx).getBoolean(ctx.getString(R.string.use_search_key), false)

}