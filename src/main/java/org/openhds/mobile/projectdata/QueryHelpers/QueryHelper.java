package org.openhds.mobile.projectdata.QueryHelpers;

import java.util.List;

import android.content.ContentResolver;
import org.openhds.mobile.repository.DataWrapper;

public interface QueryHelper {

	List<DataWrapper> getAll(ContentResolver contentResolver,
							 String state);

	List<DataWrapper> getChildren(ContentResolver contentResolver,
								  DataWrapper qr, String childState);

}
