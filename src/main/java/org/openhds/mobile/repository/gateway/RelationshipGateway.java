package org.openhds.mobile.repository.gateway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.repository.Converter;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.Query;

import static org.openhds.mobile.OpenHDS.Relationships.*;
import static org.openhds.mobile.repository.RepositoryUtils.EQUALS;
import static org.openhds.mobile.repository.RepositoryUtils.extractString;
import static org.openhds.mobile.repository.RepositoryUtils.insert;
import static org.openhds.mobile.repository.RepositoryUtils.update;


/**
 * Convert Relationship to and from database.  Relationship-specific queries.
 *
 *
 */
public class RelationshipGateway extends Gateway<Relationship> {

    public RelationshipGateway() {
        super(OpenHDS.Relationships.CONTENT_ID_URI_BASE, COLUMN_RELATIONSHIP_UUID, new RelationshipConverter());
    }

    // true if relationship was inserted, false if updated
    // take care to update at BOTH correct individuals
    @Override
    public boolean insertOrUpdate(ContentResolver contentResolver, Relationship relationship) {
        ContentValues contentValues = converter.toContentValues(relationship);

        String individualAId = relationship.getIndividualAUuid();
        String individualBId = relationship.getIndividualBUuid();
        Relationship existingRelationship = getFirst(contentResolver,
                findByBothIndividuals(individualAId, individualBId));

        if (null == existingRelationship) {
            return null != insert(contentResolver, tableUri, contentValues);
        } else {
            final String[] columnNames = {COLUMN_RELATIONSHIP_INDIVIDUAL_A, COLUMN_RELATIONSHIP_INDIVIDUAL_B};
            final String[] columnValues = {individualAId, individualBId};
            update(contentResolver, tableUri, contentValues, columnNames, columnValues);
            return false;
        }
    }

    public Query findByBothIndividuals(String individualAId, String individualBId) {
        final String[] columnNames = {COLUMN_RELATIONSHIP_INDIVIDUAL_A, COLUMN_RELATIONSHIP_INDIVIDUAL_B};
        final String[] columnValues = {individualAId, individualBId};
        return new Query(tableUri, columnNames, columnValues, COLUMN_RELATIONSHIP_INDIVIDUAL_A, EQUALS);
    }


    private static class RelationshipConverter implements Converter<Relationship> {

        @Override
        public Relationship fromCursor(Cursor cursor) {
            Relationship relationship = new Relationship();

            relationship.setUuid(extractString(cursor, COLUMN_RELATIONSHIP_UUID));
            relationship.setIndividualAUuid(extractString(cursor, COLUMN_RELATIONSHIP_INDIVIDUAL_A));
            relationship.setIndividualBUuid(extractString(cursor, COLUMN_RELATIONSHIP_INDIVIDUAL_B));
            relationship.setStartDate(extractString(cursor, COLUMN_RELATIONSHIP_STARTDATE));
            relationship.setType(extractString(cursor, COLUMN_RELATIONSHIP_TYPE));

            return relationship;
        }

        @Override
        public ContentValues toContentValues(Relationship relationship) {
            ContentValues contentValues = new ContentValues();

            contentValues.put(COLUMN_RELATIONSHIP_UUID, relationship.getUuid());
            contentValues.put(COLUMN_RELATIONSHIP_INDIVIDUAL_A, relationship.getIndividualAUuid());
            contentValues.put(COLUMN_RELATIONSHIP_INDIVIDUAL_B, relationship.getIndividualBUuid());
            contentValues.put(COLUMN_RELATIONSHIP_STARTDATE, relationship.getStartDate());
            contentValues.put(COLUMN_RELATIONSHIP_TYPE, relationship.getType());

            return contentValues;
        }

        @Override
        public String getId(Relationship relationship) {
            return relationship.getUuid();
        }

        @Override
        public DataWrapper toDataWrapper(ContentResolver contentResolver, Relationship relationship, String state) {
            DataWrapper dataWrapper = new DataWrapper();

            dataWrapper.setUuid(relationship.getUuid());
            dataWrapper.setName(relationship.getType());
            dataWrapper.setCategory(state);

            return dataWrapper;
        }
    }
}
