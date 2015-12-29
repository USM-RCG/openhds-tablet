package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.SocialGroups.*;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;


/**
 * Convert SocialGroup to and from database.  SocialGroup-specific queries.
 */
public class SocialGroupGateway extends Gateway<SocialGroup> {

    public SocialGroupGateway() {
        super(OpenHDS.SocialGroups.CONTENT_ID_URI_BASE, COLUMN_SOCIAL_GROUP_UUID, new SocialGroupConverter());
    }

    public Query findByLocationUuid(String locationUuid) {
        return new Query(
                tableUri, COLUMN_LOCATION_UUID, locationUuid, COLUMN_SOCIAL_GROUP_UUID);
    }

    private static class SocialGroupConverter implements Converter<SocialGroup> {

        @Override
        public SocialGroup fromCursor(Cursor cursor) {
            SocialGroup socialGroup = new SocialGroup();

            socialGroup.setUuid(extractString(cursor, COLUMN_SOCIAL_GROUP_UUID));
            socialGroup.setLocationUuid(extractString(cursor, COLUMN_LOCATION_UUID));
            socialGroup.setGroupHeadUuid(extractString(cursor, COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID));
            socialGroup.setGroupName(extractString(cursor, COLUMN_SOCIAL_GROUP_NAME));

            return socialGroup;
        }

        @Override
        public ContentValues toContentValues(SocialGroup socialGroup) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_SOCIAL_GROUP_UUID, socialGroup.getUuid());
            contentValues.put(COLUMN_LOCATION_UUID, socialGroup.getLocationUuid());
            contentValues.put(COLUMN_SOCIAL_GROUP_HEAD_INDIVIDUAL_UUID, socialGroup.getGroupHeadUuid());
            contentValues.put(COLUMN_SOCIAL_GROUP_NAME, socialGroup.getGroupName());

            return contentValues;
        }

        @Override
        public String getId(SocialGroup socialGroup) {
            return socialGroup.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, SocialGroup socialGroup, String state) {
            DataWrapper dataWrapper = new DataWrapper();

            dataWrapper.setUuid(socialGroup.getUuid());
            dataWrapper.setName(socialGroup.getGroupName());
            dataWrapper.setCategory(state);

            return dataWrapper;
        }
    }
}
