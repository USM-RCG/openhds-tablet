package org.openhds.mobile.projectdata.FormAdapters;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.HashMap;
import java.util.Map;

import static org.openhds.mobile.OpenHDS.Individuals.*;

public class IndividualFormAdapter {

    public static Individual fromForm(Map<String, String> formInstanceData) {
        Individual individual = new Individual();

        individual.setUuid(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_UUID)));
        individual.setExtId(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_EXTID)));
        individual.setFirstName(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_FIRST_NAME)));
        individual.setLastName(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_LAST_NAME)));
        individual.setDob(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_DOB)));
        individual.setGender(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_GENDER)));
        individual.setMother(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_MOTHER)));
        individual.setFather(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_FATHER)));
        individual.setCurrentResidenceUuid(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_RESIDENCE_LOCATION_UUID)));
        individual.setEndType(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_RESIDENCE_END_TYPE)));
        individual.setOtherId(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_ID)));
        individual.setOtherNames(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_NAMES)));
        individual.setAge(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_AGE)));
        individual.setAgeUnits(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_AGE_UNITS)));
        individual.setPhoneNumber(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_PHONE_NUMBER)));
        individual.setOtherPhoneNumber(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER)));
        individual.setPointOfContactName(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_NAME)));
        individual.setPointOfContactPhoneNumber(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER)));
        individual.setLanguagePreference(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_LANGUAGE_PREFERENCE)));
        individual.setMemberStatus(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_STATUS)));
        individual.setNationality(formInstanceData.get(
                ProjectFormFields.Individuals.getFieldNameFromColumn(COLUMN_INDIVIDUAL_NATIONALITY)));
        return individual;
    }

    public static Map<String, String> toForm( Individual individual) {
        Map<String, String> formFields = new HashMap<String, String>();

        formFields.put(ProjectFormFields.General.ENTITY_UUID, individual.getUuid());
        formFields.put(ProjectFormFields.Individuals.INDIVIDUAL_EXTID, individual.getExtId());
        formFields.put(ProjectFormFields.Individuals.FIRST_NAME, individual.getFirstName());
        formFields.put(ProjectFormFields.Individuals.LAST_NAME, individual.getLastName());
        formFields.put(ProjectFormFields.Individuals.DATE_OF_BIRTH, individual.getDob());
        formFields.put(ProjectFormFields.Individuals.GENDER, individual.getGender());
        formFields.put(ProjectFormFields.Individuals.MOTHER_EXTID, individual.getMother());
        formFields.put(ProjectFormFields.Individuals.FATHER_EXTID, individual.getFather());
        formFields.put(ProjectFormFields.Individuals.DIP, individual.getOtherId());
        formFields.put(ProjectFormFields.Individuals.OTHER_NAMES, individual.getOtherNames());
        formFields.put(ProjectFormFields.Individuals.AGE, individual.getAge());
        formFields.put(ProjectFormFields.Individuals.AGE_UNITS, individual.getAgeUnits());
        formFields.put(ProjectFormFields.Individuals.PHONE_NUMBER, individual.getPhoneNumber());
        formFields.put(ProjectFormFields.Individuals.OTHER_PHONE_NUMBER, individual.getOtherPhoneNumber());
        formFields.put(ProjectFormFields.Individuals.POINT_OF_CONTACT_NAME, individual.getPointOfContactName());
        formFields.put( ProjectFormFields.Individuals.POINT_OF_CONTACT_PHONE_NUMBER, individual.getPointOfContactPhoneNumber());
        formFields.put(ProjectFormFields.Individuals.MEMBER_STATUS, individual.getMemberStatus());
        formFields.put(ProjectFormFields.Individuals.LANGUAGE_PREFERENCE, individual.getLanguagePreference());

        return formFields;
    }
}
