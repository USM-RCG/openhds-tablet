package org.openhds.mobile.navconfig.forms.adapters;

import org.openhds.mobile.model.core.Individual;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Individuals.*;
import static org.openhds.mobile.navconfig.ProjectFormFields.Individuals.getFieldNameFromColumn;

public class IndividualFormAdapter {

    public static Individual fromForm(Map<String, String> formInstanceData) {
        Individual individual = new Individual();
        individual.setUuid(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_UUID)));
        individual.setExtId(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_EXTID)));
        individual.setFirstName(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_FIRST_NAME)));
        individual.setLastName(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_LAST_NAME)));
        individual.setDob(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_DOB)));
        individual.setGender(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_GENDER)));
        individual.setMother(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_MOTHER)));
        individual.setFather(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_FATHER)));
        individual.setCurrentResidenceUuid(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID)));
        individual.setEndType(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE)));
        individual.setOtherId(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_ID)));
        individual.setOtherNames(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_NAMES)));
        individual.setAge(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_AGE)));
        individual.setAgeUnits(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_AGE_UNITS)));
        individual.setPhoneNumber(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_PHONE_NUMBER)));
        individual.setOtherPhoneNumber(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER)));
        individual.setPointOfContactName(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME)));
        individual.setPointOfContactPhoneNumber(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER)));
        individual.setLanguagePreference(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE)));
        individual.setMemberStatus(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_STATUS)));
        individual.setNationality(formInstanceData.get(getFieldNameFromColumn(COLUMN_INDIVIDUAL_NATIONALITY)));
        return individual;
    }
}
