package org.cimsbioko.search

import android.content.Context
import org.apache.lucene.search.spell.JaroWinklerDistance
import org.cimsbioko.R
import org.cimsbioko.navconfig.UsedByJSConfig
import org.cimsbioko.utilities.ConfigUtils.getSharedPrefs
import java.util.*

/**
 * Utilities for building search indexes from campaign files.
 */
object SearchUtils {

    private val jwd = JaroWinklerDistance()

    @UsedByJSConfig
    fun joinUnique(values: Collection<String>, separator: String): String = values.toSet().joinToString(separator = separator)

    @UsedByJSConfig
    fun joinDissimilar(values: Collection<String>, separator: String, maxSimilarity: Float): String =
        values.fold(mutableSetOf<String>()) { acc, v ->
            for (dsv in acc) if (jwd.getDistance(dsv, v) > maxSimilarity) return@fold acc
            acc.apply { add(v) }
        }.joinToString(separator = separator)

}

object Utils {

    /**
     * Returns whether automatic database re-indexing is enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true if auto-update is enabled, otherwise false
     */
    fun isAutoReindexingEnabled(ctx: Context) = isSearchEnabled(ctx) &&
            getSharedPrefs(ctx).getBoolean(ctx.getString(R.string.auto_index_on_db_update_key), false)

    /**
     * Returns whether full-text search is enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true is search is enabled, otherwise false
     */
    fun isSearchEnabled(ctx: Context): Boolean = getSharedPrefs(ctx).getBoolean(ctx.getString(R.string.use_search_key), false)

}