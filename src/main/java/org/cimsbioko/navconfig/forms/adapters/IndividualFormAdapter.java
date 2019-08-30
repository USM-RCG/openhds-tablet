package org.cimsbioko.navconfig.forms.adapters;

import org.cimsbioko.model.core.Individual;

import java.util.Map;

import static org.cimsbioko.App.Individuals.*;
import static org.cimsbioko.navconfig.ProjectFormFields.Individuals.getFieldNameFromColumn;

public class IndividualFormAdapter {

    public static Individual fromForm(Map<String, String> data) {
        Individual i = new Individual();
        i.setUuid(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_UUID)));
        i.setExtId(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_EXTID)));
        i.setFirstName(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_FIRST_NAME)));
        i.setLastName(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_LAST_NAME)));
        i.setDob(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_DOB)));
        i.setGender(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_GENDER)));
        i.setCurrentResidenceUuid(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID)));
        i.setOtherId(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_ID)));
        i.setOtherNames(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_NAMES)));
        i.setPhoneNumber(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_PHONE_NUMBER)));
        i.setOtherPhoneNumber(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER)));
        i.setPointOfContactName(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME)));
        i.setPointOfContactPhoneNumber(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER)));
        i.setLanguagePreference(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE)));
        i.setStatus(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_STATUS)));
        i.setNationality(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_NATIONALITY)));
        i.setRelationshipToHead(data.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_RELATIONSHIP_TO_HEAD)));
        return i;
    }
}
