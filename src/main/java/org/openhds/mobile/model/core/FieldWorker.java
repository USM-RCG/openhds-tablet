package org.openhds.mobile.model.core;

import java.io.Serializable;

public class FieldWorker implements Serializable {

	private static final long serialVersionUID = -8973040054481039466L;

    private String uuid;
	private String extId;
	private String firstName;
	private String lastName;
    private String passwordHash;
	private String idPrefix;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String collectedIdPrefix) {
		this.idPrefix = collectedIdPrefix;
	}
}
