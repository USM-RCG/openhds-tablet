package org.openhds.mobile.model.form;

import android.content.ContentResolver;
import android.net.Uri;

import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.provider.InstanceProviderAPI;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static org.openhds.mobile.utilities.FormUtils.formFile;
import static org.openhds.mobile.utilities.FormUtils.generateODKForm;
import static org.openhds.mobile.utilities.FormUtils.loadInstance;
import static org.openhds.mobile.utilities.FormUtils.updateInstance;
import static org.openhds.mobile.utilities.OdkCollectHelper.getInstance;

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

    public void put(Map<String, String> data) throws IOException {
        updateInstance(data, filePath);
    }

    public Map<String, String> get() throws IOException {
        return loadInstance(filePath);
    }

    public static FormInstance lookup(ContentResolver resolver, Uri uri) {
        return getInstance(resolver, uri);
    }

    public static Uri generate(ContentResolver resolver, Binding binding, Map<String, String> data) throws IOException {
        return generateODKForm(resolver, binding, data, formFile(binding.getForm(), new Date()));
    }

}
