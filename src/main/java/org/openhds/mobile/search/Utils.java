package org.openhds.mobile.search;

import org.apache.lucene.search.spell.JaroWinklerDistance;

import java.util.HashSet;
import java.util.Set;


public class Utils {

    private static final float MAX_SIMILARITY = 0.99f;
    private static final JaroWinklerDistance jwd = new JaroWinklerDistance();

    static Set<String> extractUniquePhones(String phoneValue) {
        Set<String> phones = new HashSet<>();
        for (String phone : phoneValue.trim().split("\\s+")) {
            phones.add(phone);
        }
        return phones;
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
}
