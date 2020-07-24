package org.cimsbioko.data;

import android.content.ContentValues;
import android.database.Cursor;
import org.cimsbioko.App;
import org.cimsbioko.R;
import org.cimsbioko.model.core.Individual;
import org.jetbrains.annotations.NotNull;

import static org.cimsbioko.App.Individuals.*;
import static org.cimsbioko.data.CursorConvert.extractString;
import static org.cimsbioko.navconfig.Hierarchy.INDIVIDUAL;


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
        return new Query<>(this, getTableUri(), COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID, residencyId, COLUMN_INDIVIDUAL_EXTID);
    }

    @Override
    public String getId(Individual entity) {
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
    public ContentValuesConverter<Individual> getContentValuesConverter() {
        return CONTENT_VALUES_CONVERTER;
    }
}

class IndividualEntityConverter implements CursorConverter<Individual> {

    @Override
    @NotNull
    public Individual convert(@NotNull Cursor c) {
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
    @NotNull
    public DataWrapper convert(@NotNull Cursor c) {
        DataWrapper result = new DataWrapper(
                extractString(c, COLUMN_INDIVIDUAL_UUID),
                INDIVIDUAL,
                extractString(c, COLUMN_INDIVIDUAL_EXTID),
                extractString(c, COLUMN_INDIVIDUAL_FIRST_NAME) + " " + extractString(c, COLUMN_INDIVIDUAL_LAST_NAME)
        );
        result.getStringsPayload().put(R.string.individual_other_names_label, extractString(c, COLUMN_INDIVIDUAL_OTHER_NAMES));
        result.getStringsPayload().put(R.string.individual_language_preference_label, extractString(c, COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE));
        return result;
    }
}

class IndividualContentValuesConverter implements ContentValuesConverter<Individual> {

    @Override
    @NotNull
    public ContentValues toContentValues(@NotNull Individual individual) {
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
