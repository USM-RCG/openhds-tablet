package org.openhds.mobile.activity;

import android.os.Parcel;
import android.os.Parcelable;

import org.openhds.mobile.repository.DataWrapper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a path in the navigation hierarchy that can be stored and retrieved automatically by Android.
 */
public class HierarchyPath implements Parcelable {

    LinkedHashMap<String, DataWrapper> path;

    public HierarchyPath() {
        path = new LinkedHashMap<>();
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
     * Returns the value at the deepest level in the path.
     *
     * @return deepest level in the path
     */
    public DataWrapper getLeaf() {
        DataWrapper leaf = null;
        for (Map.Entry<String, DataWrapper> pathElem : path.entrySet()) {
            leaf = pathElem.getValue();
        }
        return leaf;
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
     * @return the depth of the hierarchy path
     */
    public int depth() {
        return path.size();
    }

    public String toString() {
        String SEP = "/";
        StringBuilder b = new StringBuilder(SEP);
        for (Map.Entry<String, DataWrapper> pathElem : path.entrySet()) {
            b.append(pathElem.getValue().getExtId());
            b.append(SEP);
        }
        return b.toString();
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