package org.cimsbioko.model.core;

import org.cimsbioko.navconfig.UsedByJSConfig;

import java.io.Serializable;

public class Individual implements Serializable {

	private static final long serialVersionUID = 4865035836250357347L;

    private String uuid;
	private String extId;
	private String firstName;
	private String lastName;
	private String gender;
	private String dob;
	private String currentResidence;
	private String relationshipToHead;
	private String otherId;
	private String otherNames;
	private String phoneNumber;
	private String otherPhoneNumber;
	private String pointOfContactName;
	private String pointOfContactPhoneNumber;
	private String languagePreference;
	private String status;
    private String nationality;
    private String attrs;

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

    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getCurrentResidenceUuid() {
		return currentResidence;
	}

	public void setCurrentResidenceUuid(String currentResidence) {
		this.currentResidence = currentResidence;
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

	@UsedByJSConfig
	public static String getFullName(Individual individual) {
		return individual.getFirstName() + " " + individual.getLastName();
	}

	public String getRelationshipToHead() {
		return relationshipToHead;
	}

	public void setRelationshipToHead(String relationshipToHead) {
		this.relationshipToHead = relationshipToHead;
	}

	public String getAttrs() {
		return attrs;
	}

	public void setAttrs(String attrs) {
		this.attrs = attrs;
	}
}
