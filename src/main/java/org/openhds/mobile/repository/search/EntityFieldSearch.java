package org.openhds.mobile.repository.search;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.openhds.mobile.repository.GatewayRegistry.getGatewayByName;

/**
 * Represents a "search" to fill in a field of a form.
 * <p/>
 * Extends SearchModule to add the name of the form field that
 * needs filling, and the value to fill into it.
 * <p/>
 * TODO: add a consumer to take a QueryResult and return up a related value
 * to be filled into the form field instead of the QueryResult extId.
 * The consumer must implement Parcelable, too.
 */
public class EntityFieldSearch extends SearchModule implements Parcelable {

    private String name;
    private String value;

    public EntityFieldSearch(Gateway gateway, int labelId, String name) {
        super(gateway, labelId);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    // for Parcelable
    private EntityFieldSearch(Parcel parcel) {

        labelId = parcel.readInt();
        name = parcel.readString();
        value = parcel.readString();
        gateway = getGatewayByName(parcel.readString());

        // Android recommends parceling Maps as Bundles
        final List<String> columnNames = new ArrayList<>();
        parcel.readStringList(columnNames);

        final Bundle columnLabels = parcel.readBundle();
        columnsAndLabels = new HashMap<>();
        for (String columnName : columnNames) {
            columnsAndLabels.put(columnName, columnLabels.getInt(columnName));
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

        parcel.writeInt(labelId);
        parcel.writeString(name);
        parcel.writeString(value);

        String gatewayName = gateway.getClass().getName();
        parcel.writeString(gatewayName);

        // Android recommends parceling Maps as Bundles
        final List<String> columnNames = new ArrayList<>(columnsAndLabels.keySet());
        parcel.writeStringList(columnNames);

        Bundle columnLabels = new Bundle();
        for (String columnName : columnNames) {
            columnLabels.putInt(columnName, columnsAndLabels.get(columnName));
        }
        parcel.writeBundle(columnLabels);
    }

    // for Parcelable
    public static final Creator CREATOR = new Creator();

    // for Parcelable
    private static class Creator implements Parcelable.Creator<EntityFieldSearch> {

        public EntityFieldSearch createFromParcel(Parcel in) {
            return new EntityFieldSearch(in);
        }

        public EntityFieldSearch[] newArray(int size) {
            return new EntityFieldSearch[size];
        }
    }
}
