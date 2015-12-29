package org.openhds.mobile.repository;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic representation of a result from any query.
 * <p/>
 * Facilitates generic lists and views of results, for example at various levels of
 * hierarchy navigation.  But it's up to the caller to interpret the QueryResult
 * correctly, for example using the extId and "category" (i.e. hierarchy level).
 * <p/>
 * Payloads may contain arbitrary data, for example to display with result name and extId.
 * <p/>
 * BSH
 */
public class DataWrapper implements Parcelable {


    private String uuid;
    private String category;
    private String extId;
    private String name;
    private Map<Integer, String> stringsPayload = new HashMap<Integer, String>();
    private Map<Integer, Integer> stringIdsPayload = new HashMap<Integer, Integer>();

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, String> getStringsPayload() {
        return stringsPayload;
    }

    public Map<Integer, Integer> getStringIdsPayload() {
        return stringIdsPayload;
    }

    public DataWrapper() {
    }

    @Override
    public String toString() {
        return "QueryResult[name: " + name + " extId: " + extId + " category: "
                + category + " + payload size: " + stringsPayload.size() + "]";
    }

    // for Parcelable
    private DataWrapper(Parcel parcel) {
        category = parcel.readString();
        extId = parcel.readString();
        name = parcel.readString();
        uuid = parcel.readString();

        // INTEGER to STRING
        final List<Integer> stringIds = new ArrayList<Integer>();
        parcel.readList(stringIds, null);

        final Bundle stringsPayloadBundle = parcel.readBundle();
        stringsPayload = new HashMap<>();
        for (Integer key : stringIds) {
            stringsPayload.put(key, stringsPayloadBundle.getString(key.toString()));
        }


        // INTEGER to INTEGER
        final List<Integer> moreStringIds = new ArrayList<Integer>();
        parcel.readList(moreStringIds, null);

        final Bundle stringIdsPayloadBundle = parcel.readBundle();
        stringIdsPayload = new HashMap<>();
        for (Integer key : moreStringIds) {
            stringIdsPayload.put(key, stringIdsPayloadBundle.getInt(key.toString()));
        }


    }

    // for Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    // for Parcelable
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(category);
        parcel.writeString(extId);
        parcel.writeString(name);
        parcel.writeString(uuid);


        // Pull apart the INTEGER to STRING payload and put into the parcel
        final List<Integer> stringIds = new ArrayList<Integer>(stringsPayload.keySet());
        parcel.writeList(stringIds);
        Bundle stringsPayloadBundle = new Bundle();
        for (Integer key : stringIds) {
            stringsPayloadBundle.putString(key.toString(), stringsPayload.get(key));
        }
        parcel.writeBundle(stringsPayloadBundle);

        // Pull apart the INTEGER to INTEGER payload and put into the parcel
        final List<Integer> moreStringIds = new ArrayList<Integer>(stringIdsPayload.keySet());
        parcel.writeList(moreStringIds);
        Bundle stringIdsPayloadBundle = new Bundle();
        for (Integer key : moreStringIds) {
            stringIdsPayloadBundle.putInt(key.toString(), stringIdsPayload.get(key));
        }
        parcel.writeBundle(stringIdsPayloadBundle);
    }

    // for Parcelable
    public static final Creator CREATOR = new Creator();

    // for Parcelable
    private static class Creator implements Parcelable.Creator<DataWrapper> {
        public DataWrapper createFromParcel(Parcel in) {
            return new DataWrapper(in);
        }

        public DataWrapper[] newArray(int size) {
            return new DataWrapper[size];
        }
    }
}
