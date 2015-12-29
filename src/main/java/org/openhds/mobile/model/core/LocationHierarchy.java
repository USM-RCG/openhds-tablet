package org.openhds.mobile.model.core;

import java.io.Serializable;

public class LocationHierarchy implements Serializable {

    private static final long serialVersionUID = -6370062790248563906L;

    private String extId;
    private String name;
    private String parentUuid;
    private String level;
    private String uuid;



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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

}
