package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Individuals.
 */
public class IndividualParser extends EntityParser<Individual> {

    private static final String pageName = "individual";

    @Override
    protected Individual toEntity(DataPage dataPage) {
        Individual individual = new Individual();
        individual.setUuid(dataPage.getFirstString(asList(pageName, "uuid")));
        individual.setExtId(dataPage.getFirstString(asList(pageName, "extId")));
        individual.setFirstName(dataPage.getFirstString(asList(pageName, "firstName")));
        individual.setOtherNames(dataPage.getFirstString(asList(pageName, "middleName")));
        individual.setLastName(dataPage.getFirstString(asList(pageName, "lastName")));
        individual.setDob(dataPage.getFirstString(asList(pageName, "dob")));
        individual.setGender(dataPage.getFirstString(asList(pageName, "gender")));
        individual.setCurrentResidenceUuid(dataPage.getFirstString(asList(pageName, "residenceLocation")));
        individual.setEndType(dataPage.getFirstString(asList(pageName, "residenceEndType")));
        individual.setOtherId(dataPage.getFirstString(asList(pageName, "dip")));
        individual.setAge(dataPage.getFirstString(asList(pageName, "age")));
        individual.setAgeUnits(dataPage.getFirstString(asList(pageName, "ageUnits")));
        individual.setPhoneNumber(dataPage.getFirstString(asList(pageName, "phone")));
        individual.setOtherPhoneNumber(dataPage.getFirstString(asList(pageName, "otherPhone")));
        individual.setPointOfContactName(dataPage.getFirstString(asList(pageName, "pOCName")));
        individual.setPointOfContactPhoneNumber(dataPage.getFirstString(asList(pageName, "pOCPhone")));
        individual.setMemberStatus(dataPage.getFirstString(asList(pageName, "memberStatus")));
        individual.setLanguagePreference(dataPage.getFirstString(asList(pageName, "language")));
        individual.setNationality(dataPage.getFirstString(asList(pageName, "nationality")));
        return individual;
    }
}
