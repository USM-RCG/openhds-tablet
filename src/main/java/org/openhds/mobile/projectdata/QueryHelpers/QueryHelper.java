package org.openhds.mobile.projectdata.QueryHelpers;

import java.util.List;

import android.content.ContentResolver;
import org.openhds.mobile.repository.DataWrapper;

public interface QueryHelper {
	public List<DataWrapper> getAll(ContentResolver contentResolver,
			String state);

	public List<DataWrapper> getChildren(ContentResolver contentResolver,
			DataWrapper qr, String childState);

}
