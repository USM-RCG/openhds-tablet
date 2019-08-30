package org.cimsbioko.navconfig.forms.adapters;

import org.cimsbioko.model.core.Individual;

import java.util.Map;

import static org.cimsbioko.navconfig.forms.KnownFields.ENTITY_UUID;

public class IndividualFormAdapter {

    public static Individual fromForm(Map<String, String> data) {
        Individual i = new Individual();
        i.setUuid(data.get(ENTITY_UUID));
        i.setExtId(data.get("individualExtId"));
        i.setFirstName(data.get("individualFirstName"));
        i.setLastName(data.get("individualLastName"));
        i.setDob(data.get("individualDateOfBirth"));
        i.setGender(data.get("individualGender"));
        i.setCurrentResidenceUuid(data.get("householdUuid"));
        i.setOtherId(data.get("individualDip"));
        i.setOtherNames(data.get("individualOtherNames"));
        i.setPhoneNumber(data.get("individualPhoneNumber"));
        i.setOtherPhoneNumber(data.get("individualOtherPhoneNumber"));
        i.setPointOfContactName(data.get("individualPointOfContactName"));
        i.setPointOfContactPhoneNumber(data.get("individualPointOfContactPhoneNumber"));
        i.setLanguagePreference(data.get("individualLanguagePreference"));
        i.setStatus(data.get("individualMemberStatus"));
        i.setNationality(data.get("individualNationality"));
        i.setRelationshipToHead(data.get("individualRelationshipToHeadOfHousehold"));
        return i;
    }
}
