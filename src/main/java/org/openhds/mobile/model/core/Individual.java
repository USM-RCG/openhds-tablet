package org.openhds.mobile.model.core;

import java.io.Serializable;

public class Individual implements Serializable {

	private static final long serialVersionUID = 4865035836250357347L;

    private String uuid;
	private String extId;
	private String firstName;
	private String middleName;
	private String lastName;
	private String gender;
	private String dob;
	private String mother;
	private String father;
	private String currentResidence;
	private String endType;
	private String otherId;
	private String otherNames;
	private String age;
	private String ageUnits;
	private String phoneNumber;
	private String otherPhoneNumber;
	private String pointOfContactName;
	private String pointOfContactPhoneNumber;
	private String languagePreference;
	private String memberStatus;
    private String nationality;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getMemberStatus() {
		return memberStatus;
	}

	public void setMemberStatus(String memberStatus) {
		this.memberStatus = memberStatus;
	}

	public String getPointOfContactName() {
		return pointOfContactName;
	}

	public void setPointOfContactName(String pointOfContactName) {
		this.pointOfContactName = pointOfContactName;
	}

	public String getPointOfContactPhoneNumber() {
		return pointOfContactPhoneNumber;
	}

	public void setPointOfContactPhoneNumber(String pointOfContactPhoneNumber) {
		this.pointOfContactPhoneNumber = pointOfContactPhoneNumber;
	}

	public String getExtId() {
		return extId;
	}

	public void setExtId(String extId) {
		this.extId = extId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getMother() {
		return mother;
	}

	public void setMother(String mother) {
		this.mother = mother;
	}

	public String getFather() {
		return father;
	}

	public void setFather(String father) {
		this.father = father;
	}

	public String getCurrentResidenceUuid() {
		return currentResidence;
	}

	public void setCurrentResidenceUuid(String currentResidence) {
		this.currentResidence = currentResidence;
	}

	public String getEndType() {
		return endType;
	}

	public void setEndType(String endType) {
		this.endType = endType;
	}

	public String getOtherId() {
		return otherId;
	}

	public void setOtherId(String otherId) {
		this.otherId = otherId;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getAgeUnits() {
		return ageUnits;
	}

	public void setAgeUnits(String ageUnits) {
		this.ageUnits = ageUnits;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getOtherPhoneNumber() {
		return otherPhoneNumber;
	}

	public void setOtherPhoneNumber(String otherPhoneNumber) {
		this.otherPhoneNumber = otherPhoneNumber;
	}

	public String getLanguagePreference() {
		return languagePreference;
	}

	public void setLanguagePreference(String languagePreference) {
		this.languagePreference = languagePreference;
	}

	public static String getFullName(Individual individual) {
		String fullname = individual.getFirstName() + " "
				+ individual.getLastName();
		return fullname;
	}

	public static String getAgeWithUnits(Individual individual) {

        String ageString = (null == individual.age || individual.age.equals("null")) ? "n/a" : individual.age;
        String unitsString = (null == individual.ageUnits || individual.ageUnits.equals("null")) ? "n/a" : individual.ageUnits;
		String ageWithUnits = ageString + " " + "(" + unitsString + ")";
		return ageWithUnits;
	}
}
