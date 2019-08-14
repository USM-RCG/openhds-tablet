package org.cimsbioko.navconfig.db;

import android.content.ContentResolver;

import org.cimsbioko.repository.DataWrapper;

import java.util.List;

public interface QueryHelper {

    List<DataWrapper> getAll(ContentResolver contentResolver, String level);

    List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper qr, String childLevel);

    DataWrapper get(ContentResolver resolver, String level, String uuid);

    DataWrapper getParent(ContentResolver resolver, String level, String uuid);

}
