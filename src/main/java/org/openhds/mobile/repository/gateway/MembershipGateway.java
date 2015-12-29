package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Memberships.*;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;

/**
 * Convert Membership to and from database.  Membership-specific queries.
 *
 * Uses the extId of the individual as the id for the Membership.
 *
 */
public class MembershipGateway extends Gateway<Membership> {

    public MembershipGateway() {
        super(OpenHDS.Memberships.CONTENT_ID_URI_BASE, COLUMN_MEMBERSHIP_UUID, new MembershipConverter());
    }

    // true if membership was inserted, false if updated
    // take care to update at the correct social group AND individual
    @Override
    public boolean insertOrUpdate(ContentResolver contentResolver, Membership membership) {
        ContentValues contentValues = converter.toContentValues(membership);

        
        String socialGroupId = membership.getSocialGroupUuid();
        String individualId = membership.getIndividualUuid();
        Membership existingMembership = getFirst(contentResolver,
                findBySocialGroupAndIndividual(socialGroupId, individualId));

        if (null == existingMembership) {
            return null != insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {COLUMN_SOCIAL_GROUP_UUID, COLUMN_INDIVIDUAL_UUID};
            final String[] columnValues = {socialGroupId, individualId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
            return false;
        }
    }

    public Query findByIndividual(String individualId) {
        return new Query(tableUri, COLUMN_INDIVIDUAL_UUID, individualId, COLUMN_INDIVIDUAL_UUID);
    }

    public Query findBySocialGroupAndIndividual(String socialGroupId, String individualId) {
        final String[] columnNames = {COLUMN_SOCIAL_GROUP_UUID, COLUMN_INDIVIDUAL_UUID};
        final String[] columnValues = {socialGroupId, individualId};
        return new Query(tableUri, columnNames, columnValues, COLUMN_INDIVIDUAL_UUID, EQUALS);
    }

    private static class MembershipConverter implements Converter<Membership> {

        @Override
        public Membership fromCursor(Cursor cursor) {
            Membership membership = new Membership();

            membership.setUuid(extractString(cursor, COLUMN_MEMBERSHIP_UUID));
            membership.setIndividualUuid(extractString(cursor, COLUMN_INDIVIDUAL_UUID));
            membership.setSocialGroupUuid(extractString(cursor, COLUMN_SOCIAL_GROUP_UUID));
            membership.setRelationshipToHead(extractString(cursor, COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD));

            return membership;
        }

        @Override
        public ContentValues toContentValues(Membership membership) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_MEMBERSHIP_UUID, membership.getUuid());
            contentValues.put(COLUMN_INDIVIDUAL_UUID, membership.getIndividualUuid());
            contentValues.put(COLUMN_SOCIAL_GROUP_UUID, membership.getSocialGroupUuid());
            contentValues.put(COLUMN_MEMBERSHIP_RELATIONSHIP_TO_HEAD, membership.getRelationshipToHead());

            return contentValues;
        }

        @Override
        public String getId(Membership membership) {
            return membership.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Membership membership, String state) {
            DataWrapper dataWrapper = new DataWrapper();
            dataWrapper.setName(membership.getRelationshipToHead());
            dataWrapper.setUuid(membership.getUuid());
            dataWrapper.setCategory(state);
            return dataWrapper;
        }
    }
}
