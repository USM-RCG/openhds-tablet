package org.openhds.mobile.navconfig.db;

import java.util.List;

import android.content.ContentResolver;
import org.openhds.mobile.repository.DataWrapper;

public interface QueryHelper {

	List<DataWrapper> getAll(ContentResolver contentResolver, String level);

	List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper qr, String childLevel);

}
