package org.openhds.mobile.model.core;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 230186771721044764L;

    private String uuid;
	private String extId;
	private String name;
	private String latitude;
	private String longitude;
	private String hierarchyUuid;
    private String hierarchyExtId;
    private String communityName;
    private String communityCode;
    private String localityName;
    private String mapAreaName;
    private String sectorName;
    private int buildingNumber;
    private int floorNumber;
    private String regionName;
    private String provinceName;
    private String subDistrictName;
    private String districtName;
    private String hasReceivedBedNets;
    private String description;
    private String locationEvaluationStatus;
    private String sprayingEvaluation;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getLocationEvaluationStatus() {
        return locationEvaluationStatus;
    }

    public void setLocationEvaluationStatus(String locationEvaluationStatus) {
        this.locationEvaluationStatus = locationEvaluationStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHasReceivedBedNets() {
        return hasReceivedBedNets;
    }

    public void setHasReceivedBedNets(String hasReceivedBedNets) {
        this.hasReceivedBedNets = hasReceivedBedNets;
    }

    public void setSprayingEvaluation(String sprayingEvaluation) {
        this.sprayingEvaluation = sprayingEvaluation;
    }

    public String getSprayingEvaluation() {
        return sprayingEvaluation;
    }

    public int getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(int buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getSubDistrictName() {
        return subDistrictName;
    }

    public void setSubDistrictName(String subDistrictName) {
        this.subDistrictName = subDistrictName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
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

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getHierarchyUuid() {
		return hierarchyUuid;
	}

	public void setHierarchyUuid(String hierarchyUuid) {
		this.hierarchyUuid = hierarchyUuid;
	}

    public String getHierarchyExtId() {
        return hierarchyExtId;
    }

    public void setHierarchyExtId(String hierarchyExtId) {
        this.hierarchyExtId = hierarchyExtId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getCommunityCode() {
        return communityCode;
    }

    public void setCommunityCode(String communityCode) {
        this.communityCode = communityCode;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

	public String getMapAreaName() {
		return mapAreaName;
	}

	public void setMapAreaName(String mapAreaName) {
		this.mapAreaName = mapAreaName;
	}
}
