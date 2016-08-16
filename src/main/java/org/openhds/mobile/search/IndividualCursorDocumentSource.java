package org.openhds.mobile.search;

import android.database.Cursor;

import static org.openhds.mobile.search.Utils.extractDissimilarNames;
import static org.openhds.mobile.search.Utils.extractUniquePhones;
import static org.openhds.mobile.search.Utils.join;

class IndividualCursorDocumentSource extends SimpleCursorDocumentSource {

    private String nameColumn, phoneColumn;

    IndividualCursorDocumentSource(Cursor c, String nameCol, String phoneCol) {
        super(c);
        nameColumn = nameCol;
        phoneColumn = phoneCol;
    }

    @Override
    public String getFieldValue(int index) {
        String fieldName = fields[index].name();
        if (nameColumn.equals(fieldName)) {
            return getNameValue(index);
        } else if (phoneColumn.equals(fieldName)) {
            return getPhoneValue(index);
        } else {
            return super.getFieldValue(index);
        }
    }

    private String getPhoneValue(int index) {
        String rawValue = cursor.getString(index);
        if (rawValue != null) {
            return join(extractUniquePhones(rawValue), " ");
        }
        return null;
    }

    private String getNameValue(int index) {
        String rawValue = cursor.getString(index);
        if (rawValue != null) {
            return join(extractDissimilarNames(rawValue), " ");
        }
        return null;
    }


}
