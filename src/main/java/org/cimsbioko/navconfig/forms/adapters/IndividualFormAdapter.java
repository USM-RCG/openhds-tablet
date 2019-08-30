package org.cimsbioko.navconfig.forms.adapters;

import org.cimsbioko.model.core.Individual;

import java.util.Map;

import static org.cimsbioko.navconfig.ProjectFormFields.General.ENTITY_UUID;
import static org.cimsbioko.navconfig.ProjectFormFields.Individuals.*;

public class IndividualFormAdapter {

    public static Individual fromForm(Map<String, String> data) {
        Individual i = new Individual();
        i.setUuid(data.get(ENTITY_UUID));
        i.setExtId(data.get(INDIVIDUAL_EXTID));
        i.setFirstName(data.get(FIRST_NAME));
        i.setLastName(data.get(LAST_NAME));
        i.setDob(data.get(DATE_OF_BIRTH));
        i.setGender(data.get(GENDER));
        i.setCurrentResidenceUuid(data.get(HOUSEHOLD_UUID));
        i.setOtherId(data.get(DIP));
        i.setOtherNames(data.get(OTHER_NAMES));
        i.setPhoneNumber(data.get(PHONE_NUMBER));
        i.setOtherPhoneNumber(data.get(OTHER_PHONE_NUMBER));
        i.setPointOfContactName(data.get(POINT_OF_CONTACT_NAME));
        i.setPointOfContactPhoneNumber(data.get(POINT_OF_CONTACT_PHONE_NUMBER));
        i.setLanguagePreference(data.get(LANGUAGE_PREFERENCE));
        i.setStatus(data.get(STATUS));
        i.setNationality(data.get(NATIONALITY));
        i.setRelationshipToHead(data.get(RELATIONSHIP_TO_HEAD));
        return i;
    }
}
