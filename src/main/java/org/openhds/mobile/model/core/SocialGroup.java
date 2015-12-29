package org.openhds.mobile.model.core;

import java.io.Serializable;

public class SocialGroup implements Serializable {

	private static final long serialVersionUID = 571090333555561853L;

    private String locationUuid;
	private String groupName;
	private String groupHeadUuid;
    private String uuid;

    public SocialGroup() {}

    public SocialGroup(String locationUuid, String name, Individual head, String uuid) {
        this.locationUuid = locationUuid;
        this.groupName = name;
        this.groupHeadUuid = head.getUuid();
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getGroupHeadUuid() {
		return groupHeadUuid;
	}
	
	public void setGroupHeadUuid(String groupHeadUUid) {
		this.groupHeadUuid = groupHeadUUid;
	}
}
