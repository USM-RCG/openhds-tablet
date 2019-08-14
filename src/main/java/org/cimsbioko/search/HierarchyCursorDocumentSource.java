package org.cimsbioko.search;

import android.database.Cursor;

import org.cimsbioko.navconfig.NavigatorConfig;

public class HierarchyCursorDocumentSource extends SimpleCursorDocumentSource {

    private String levelCol;
    private NavigatorConfig config;

    HierarchyCursorDocumentSource(Cursor c, String levelCol) {
        super(c);
        this.levelCol = levelCol;
        config = NavigatorConfig.getInstance();
    }

    @Override
    public String getFieldValue(int index) {
        String fieldName = getFieldName(index);
        if (levelCol.equals(fieldName)) {
            return config.getLevelForServerLevel(super.getFieldValue(index));
        } else {
            return super.getFieldValue(index);
        }
    }
}
