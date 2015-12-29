package org.openhds.mobile.model.core;

import org.openhds.mobile.model.core.Individual;

import java.io.Serializable;

public class Relationship implements Serializable {

	private static final long serialVersionUID = -7405831168618814047L;

	private String individualA;
	private String individualB;
	private String startDate;
	private String type;
    private String uuid;

    public Relationship() {}

    public Relationship(Individual individualAUuid, Individual individualBUuid, String type, String startDate, String uuid) {
        this.individualA = individualAUuid.getUuid();
        this.individualB = individualBUuid.getUuid();
        this.type = type;
        this.startDate = startDate;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIndividualAUuid() {
		return individualA;
	}

	public void setIndividualAUuid(String individualA) {
		this.individualA = individualA;
	}

	public String getIndividualBUuid() {
		return individualB;
	}

	public void setIndividualBUuid(String individualB) {
		this.individualB = individualB;
	}

	// dates come in from the web service in dd-MM-yyyy format
	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
}
