package org.openhds.mobile.navconfig;

import android.content.ContentResolver;
import android.os.Parcel;
import android.os.Parcelable;

import org.openhds.mobile.navconfig.db.DefaultQueryHelper;
import org.openhds.mobile.navconfig.db.QueryHelper;
import org.openhds.mobile.repository.DataWrapper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a path in the navigation hierarchy that can be stored and retrieved automatically by Android.
 */
public class HierarchyPath implements Parcelable, Cloneable {

    private static final String PATH_SEPARATOR = "|";

    private static final Pattern LEAF_PATTERN = Pattern.compile("(\\w+):([a-f0-9]+)");

    private LinkedHashMap<String, DataWrapper> path;

    public HierarchyPath() {
        path = new LinkedHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
        HierarchyPath copy = (HierarchyPath) super.clone();
        copy.path = (LinkedHashMap) this.path.clone();
        return copy;
    }

    /**
     * @return the named levels in the path in traversal order.
     */
    public Set<String> getLevels() {
        return path.keySet();
    }

    /**
     * Fetches the value at the specified level in the path.
     *
     * @param level the named level of interest
     * @return the value in the path at the specified level
     */
    public DataWrapper get(String level) {
        return path.get(level);
    }

    /**
     * Traverses one level deeper in the hierarchy.
     *
     * @param level the name of the level to add
     * @param value the value to add at the level
     */
    public void down(String level, DataWrapper value) {
        path.put(level, value);
    }

    /**
     * After calling this, the path will only contain values leading up to the named level.
     *
     * @param level the level to traverse up to (the new path leaf)
     */
    public void truncate(String level) {
        Iterator<Map.Entry<String, DataWrapper>> pathIter = path.entrySet().iterator();
        boolean seenLevel = false;
        while (pathIter.hasNext()) {
            Map.Entry<String, DataWrapper> pathElem = pathIter.next();
            if (!seenLevel && pathElem.getKey().equals(level)) {
                seenLevel = true;
            }
            if (seenLevel) {
                pathIter.remove();
            }
        }
    }

    /**
     * Resets to an empty path.
     */
    public void clear() {
        path.clear();
    }

    /**
     * @return the depth of the hierarchy path
     */
    public int depth() {
        return path.size();
    }

    public String toString() {
        return toNodeString();
    }

    private DataWrapper getLeafValue() {
        DataWrapper leaf = null;
        for (DataWrapper value : path.values()) {
            leaf = value;
        }
        return leaf;
    }

    private String toNodeString() {
        if (path.isEmpty()) {
            return "";
        } else {
            DataWrapper leaf = getLeafValue();
            return String.format("%s:%s", leaf.getCategory(), leaf.getUuid());
        }
    }

    public static HierarchyPath fromString(ContentResolver resolver, String pathStr) {
        Matcher leafMatcher = LEAF_PATTERN.matcher(pathStr);
        if (leafMatcher.matches()) {
            QueryHelper helper = DefaultQueryHelper.getInstance();
            DataWrapper leafNode = helper.get(resolver, leafMatcher.group(1), leafMatcher.group(2));
            return fromLeafString(resolver, leafNode);
        } else {
            return fromPathString(resolver, pathStr);
        }
    }

    private static HierarchyPath fromPathString(ContentResolver resolver, String pathStr) {
        QueryHelper helper = DefaultQueryHelper.getInstance();
        HierarchyPath path = null;
        List<String> configuredLevels = NavigatorConfig.getInstance().getLevels();
        if (pathStr != null) {
            String[] pathPieces = pathStr.split("[" + PATH_SEPARATOR + "]");
            if (pathPieces.length <= configuredLevels.size()) {
                path = new HierarchyPath();
                for (int i = 0; i < pathPieces.length; i++) {
                    String p = pathPieces[i], level = configuredLevels.get(i);
                    DataWrapper value = helper.get(resolver, level, p);
                    if (value != null) {
                        path.down(level, value);
                    } else {
                        path = null;
                        break;
                    }
                }
            }
        }
        return path;
    }

    private static HierarchyPath fromLeafString(ContentResolver resolver, DataWrapper leaf) {

        QueryHelper helper = DefaultQueryHelper.getInstance();

        // Traverse up the hierarchy using child-parent relationships, tracking the nodes traversed
        Stack<DataWrapper> traversed = new Stack<>();
        traversed.push(leaf);
        DataWrapper child = leaf, parent;
        do {
            parent = helper.getParent(resolver, child.getCategory(), child.getUuid());
            if (parent != null) {
                traversed.push(parent);
                child = parent;
            }
        } while (parent != null);

        // Reconstruct a path from the traversed nodes if it reached the top of the hierarchy
        if (NavigatorConfig.getInstance().getTopLevel().equals(traversed.peek().getCategory())) {
            HierarchyPath path = new HierarchyPath();
            while (!traversed.empty()) {
                DataWrapper node = traversed.pop();
                path.down(node.getCategory(), node);
            }
            return path;
        }

        return null;
    }

    public boolean equals(Object other) {

        if (this == other) {
            return true;  // same object
        }

        if (!(other instanceof HierarchyPath)) {
            return false;  // objects of different type
        }

        HierarchyPath otherPath = (HierarchyPath) other;

        if (this.depth() != otherPath.depth() || !getLevels().equals(otherPath.getLevels())) {
            return false;  // level composition isn't the same
        }

        for (String level : getLevels()) {
            if ((get(level) == null && otherPath.get(level) != null) || !get(level).equals(otherPath.get(level))) {
                return false;  // not all levels matched
            }
        }
        return true;  // all level values matched
    }

    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int depth = depth();
        dest.writeInt(depth);
        dest.writeStringArray(path.keySet().toArray(new String[depth]));
        dest.writeTypedArray(path.values().toArray(new DataWrapper[depth]), 0);
    }

    public static final Parcelable.Creator CREATOR = new Creator();

    private static class Creator implements Parcelable.Creator<HierarchyPath> {

        @Override
        public HierarchyPath createFromParcel(Parcel source) {
            HierarchyPath result = null;
            int depth = source.readInt();
            String[] levels = new String[depth];
            source.readStringArray(levels);
            DataWrapper[] values = new DataWrapper[depth];
            source.readTypedArray(values, DataWrapper.CREATOR);
            if (depth == levels.length && levels.length == values.length) {
                result = new HierarchyPath();
                for (int l = 0; l < depth; l++) {
                    result.down(levels[l], values[l]);
                }
            }
            return result;
        }

        @Override
        public HierarchyPath[] newArray(int size) {
            return new HierarchyPath[size];
        }
    }
}