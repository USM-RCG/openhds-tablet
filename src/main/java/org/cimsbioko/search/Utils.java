package org.cimsbioko.search;

import android.content.Context;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.cimsbioko.R;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.cimsbioko.utilities.ConfigUtils.getSharedPrefs;

public class Utils {

    private static final float MAX_SIMILARITY = 0.99f;
    private static final JaroWinklerDistance jwd = new JaroWinklerDistance();

    static Set<String> extractUniquePhones(String phoneValue) {
        return new HashSet<>(asList(phoneValue.trim().split("\\s+")));
    }

    static Set<String> extractDissimilarNames(String nameValue) {
        Set<String> names = new HashSet<>();
        for (String name : nameValue.trim().toLowerCase().split("\\s+")) {
            name = name.replaceAll("\\W+", "");
            if (!containsSimilar(names, name)) {
                names.add(name);
            }
        }
        return names;
    }

    static String join(Set<String> values, String separator) {
        StringBuilder buf = new StringBuilder();
        for (String name : values) {
            if (buf.length() > 0) {
                buf.append(separator);
            }
            buf.append(name);
        }
        return buf.toString();
    }

    static boolean containsSimilar(Set<String> values, String value) {
        for (String v : values) {
            if (jwd.getDistance(v, value) > MAX_SIMILARITY)
                return true;
        }
        return false;
    }

    /**
     * Returns whether automatic database re-indexing is enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true if auto-update is enabled, otherwise false
     */
    public static boolean isAutoReindexingEnabled(Context ctx) {
        return isSearchEnabled(ctx) && getSharedPrefs(ctx).getBoolean(ctx.getString(R.string.auto_index_on_db_update_key), false);
    }

    /**
     * Returns whether full-text search is enabled based on user settings.
     *
     * @param ctx the app context to use for accessing preferences
     * @return true is search is enabled, otherwise false
     */
    public static boolean isSearchEnabled(Context ctx) {
        return getSharedPrefs(ctx).getBoolean(ctx.getString(R.string.use_search_key), false);
    }

}
