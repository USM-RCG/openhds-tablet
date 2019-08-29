package org.cimsbioko.data;

import android.content.ContentValues;
import android.database.Cursor;

import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.model.core.Individual;

import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_ATTRS;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_DOB;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_EXTID;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_GENDER;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_LAST_NAME;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_NATIONALITY;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_OTHER_ID;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_STATUS;
import static org.cimsbioko.App.Individuals.COLUMN_INDIVIDUAL_UUID;
import static org.cimsbioko.navconfig.Hierarchy.INDIVIDUAL;
import static org.cimsbioko.data.CursorConvert.extractString;


/**
 * Convert Individuals to and from database.  Individual-specific queries.
 */
public class IndividualGateway extends Gateway<Individual> {

    private static final IndividualEntityConverter ENTITY_CONVERTER = new IndividualEntityConverter();
    private static final IndividualWrapperConverter WRAPPER_CONVERTER = new IndividualWrapperConverter();
    private static final IndividualContentValuesConverter CONTENT_VALUES_CONVERTER = new IndividualContentValuesConverter();

    IndividualGateway() {
        super(App.Individuals.CONTENT_ID_URI_BASE, COLUMN_INDIVIDUAL_UUID);
    }

    public Query<Individual> findByResidency(String residencyId) {
        return new Query<>(this, tableUri, COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, residencyId, COLUMN_INDIVIDUAL_EXTID);
    }

    @Override
    String getId(Individual entity) {
        return entity.getUuid();
    }

    @Override
    public CursorConverter<Individual> getEntityConverter() {
        return ENTITY_CONVERTER;
    }

    @Override
    public CursorConverter<DataWrapper> getWrapperConverter() {
        return WRAPPER_CONVERTER;
    }

    @Override
    ContentValuesConverter<Individual> getContentValuesConverter() {
        return CONTENT_VALUES_CONVERTER;
    }
}

class IndividualEntityConverter implements CursorConverter<Individual> {

    @Override
    public Individual convert(Cursor c) {
        Individual individual = new Individual();
        individual.setUuid(extractString(c, COLUMN_INDIVIDUAL_UUID));
        individual.setExtId(extractString(c, COLUMN_INDIVIDUAL_EXTID));
        individual.setFirstName(extractString(c, COLUMN_INDIVIDUAL_FIRST_NAME));
        individual.setLastName(extractString(c, COLUMN_INDIVIDUAL_LAST_NAME));
        individual.setDob(extractString(c, COLUMN_INDIVIDUAL_DOB));
        individual.setGender(extractString(c, COLUMN_INDIVIDUAL_GENDER));
        individual.setCurrentResidenceUuid(extractString(c, COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID));
        individual.setOtherId(extractString(c, COLUMN_INDIVIDUAL_OTHER_ID));
        individual.setOtherNames(extractString(c, COLUMN_INDIVIDUAL_OTHER_NAMES));
        individual.setPhoneNumber(extractString(c, COLUMN_INDIVIDUAL_PHONE_NUMBER));
        individual.setOtherPhoneNumber(extractString(c, COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER));
        individual.setPointOfContactName(extractString(c, COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME));
        individual.setStatus(extractString(c, COLUMN_INDIVIDUAL_STATUS));
        individual.setPointOfContactPhoneNumber(extractString(c, COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER));
        individual.setLanguagePreference(extractString(c, COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE));
        individual.setNationality(extractString(c, COLUMN_INDIVIDUAL_NATIONALITY));
        individual.setRelationshipToHead(extractString(c, COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD));
        individual.setAttrs(extractString(c, COLUMN_INDIVIDUAL_ATTRS));
        return individual;
    }
}

class IndividualWrapperConverter implements CursorConverter<DataWrapper> {
    @Override
    public DataWrapper convert(Cursor c) {
        DataWrapper dataWrapper = new DataWrapper();
        dataWrapper.setUuid(extractString(c, COLUMN_INDIVIDUAL_UUID));
        dataWrapper.setExtId(extractString(c, COLUMN_INDIVIDUAL_EXTID));
        dataWrapper.setName(extractString(c, COLUMN_INDIVIDUAL_FIRST_NAME) + " " + extractString(c, COLUMN_INDIVIDUAL_LAST_NAME));
        dataWrapper.setCategory(INDIVIDUAL);

        // for Bioko add individual details to payload
        dataWrapper.getStringsPayload().put(R.string.individual_other_names_label, extractString(c, COLUMN_INDIVIDUAL_OTHER_NAMES));
        dataWrapper.getStringsPayload().put(R.string.individual_language_preference_label, extractString(c, COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE));

        return dataWrapper;
    }
}

class IndividualContentValuesConverter implements ContentValuesConverter<Individual> {

    @Override
    public ContentValues toContentValues(Individual individual) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_INDIVIDUAL_UUID, individual.getUuid());
        contentValues.put(COLUMN_INDIVIDUAL_EXTID, individual.getExtId());
        contentValues.put(COLUMN_INDIVIDUAL_FIRST_NAME, individual.getFirstName());
        contentValues.put(COLUMN_INDIVIDUAL_LAST_NAME, individual.getLastName());
        contentValues.put(COLUMN_INDIVIDUAL_DOB, individual.getDob());
        contentValues.put(COLUMN_INDIVIDUAL_GENDER, individual.getGender());
        contentValues.put(COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, individual.getCurrentResidenceUuid());
        contentValues.put(COLUMN_INDIVIDUAL_OTHER_ID, individual.getOtherId());
        contentValues.put(COLUMN_INDIVIDUAL_OTHER_NAMES, individual.getOtherNames());
        contentValues.put(COLUMN_INDIVIDUAL_PHONE_NUMBER, individual.getPhoneNumber());
        contentValues.put(COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER, individual.getOtherPhoneNumber());
        contentValues.put(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME, individual.getPointOfContactName());
        contentValues.put(COLUMN_INDIVIDUAL_STATUS, individual.getStatus());
        contentValues.put(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER, individual.getPointOfContactPhoneNumber());
        contentValues.put(COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE, individual.getLanguagePreference());
        contentValues.put(COLUMN_INDIVIDUAL_NATIONALITY, individual.getNationality());
        contentValues.put(COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD, individual.getRelationshipToHead());
        contentValues.put(COLUMN_INDIVIDUAL_ATTRS, individual.getAttrs());
        return contentValues;
    }
}
