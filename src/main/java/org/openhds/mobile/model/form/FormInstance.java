package org.openhds.mobile.model.form;

import org.openhds.mobile.provider.InstanceProviderAPI;

import java.io.Serializable;

public class FormInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private String formName;
    private String filePath;
    private String fileName;
    private String uriString;
    private String formVersion;
    private String status;

    public String getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(String formVersion) {
        this.formVersion = formVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isComplete() {
        return InstanceProviderAPI.STATUS_COMPLETE.equals(status);
    }

    public boolean isSubmitted() {
        return InstanceProviderAPI.STATUS_SUBMITTED.equals(status);
    }

    public boolean isIncomplete() {
        return InstanceProviderAPI.STATUS_INCOMPLETE.equals(status);
    }

}
