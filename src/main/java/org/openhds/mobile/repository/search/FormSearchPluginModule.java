package org.openhds.mobile.repository.search;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a "search" to fill in a field of a form.
 *
 * Extends SearchPluginModule to add the name of the form field that
 * needs filling, and the value to fill into it.
 *
 * TODO: add a consumer to take a QueryResult and return up a related value
 * to be filled into the form field instead of the QueryResult extId.
 * The consumer must implement Parcelable, too.
 */
public class FormSearchPluginModule extends SearchPluginModule implements Parcelable {
    private String fieldName;
    private String fieldValue;

    public FormSearchPluginModule(Gateway gateway, int labelId, String fieldName) {
        super(gateway, labelId);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    // for Parcelable
    private FormSearchPluginModule(Parcel parcel) {
        labelId = parcel.readInt();
        fieldName = parcel.readString();
        fieldValue = parcel.readString();

        final String gatewayName = parcel.readString();
        gateway = GatewayRegistry.getGatewayByName(gatewayName);

        // Android recommends parceling Maps as Bundles
        final List<String> columnList = new ArrayList<>();
        parcel.readStringList(columnList);

        final Bundle columnsAndLabelsBundle = parcel.readBundle();
        columnsAndLabels = new HashMap<>();
        for (String key : columnList) {
            columnsAndLabels.put(key, columnsAndLabelsBundle.getInt(key));
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
        parcel.writeString(fieldName);
        parcel.writeString(fieldValue);

        String gatewayName = gateway.getClass().getName();
        parcel.writeString(gatewayName);

        // Android recommends parceling Maps as Bundles
        final List<String> columnList = new ArrayList<String>(columnsAndLabels.keySet());
        parcel.writeStringList(columnList);

        Bundle columnsAndLabelsBundle = new Bundle();
        for (String key : columnList) {
            columnsAndLabelsBundle.putInt(key, columnsAndLabels.get(key));
        }
        parcel.writeBundle(columnsAndLabelsBundle);
    }

    // for Parcelable
    public static final Creator CREATOR = new Creator();

    // for Parcelable
    private static class Creator implements Parcelable.Creator<FormSearchPluginModule> {
        public FormSearchPluginModule createFromParcel(Parcel in) {
            return new FormSearchPluginModule(in);
        }

        public FormSearchPluginModule[] newArray(int size) {
            return new FormSearchPluginModule[size];
        }
    }
}
