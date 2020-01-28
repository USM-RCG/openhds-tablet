package org.cimsbioko.utilities;

import java.io.File;

public class CampaignDownloadResult {

    File downloadedFile;
    String campaign;
    String etag;
    String error;

    public CampaignDownloadResult(File downloadedFile, String campaign, String etag) {
        this.downloadedFile = downloadedFile;
        this.campaign = campaign;
        this.etag = etag;
    }

    public CampaignDownloadResult(String error) {
        this.error = error;
    }

    public boolean wasError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public File getDownloadedFile() {
        return downloadedFile;
    }

    public String getCampaign() {
        return campaign;
    }

    public String getEtag() {
        return etag;
    }
}
