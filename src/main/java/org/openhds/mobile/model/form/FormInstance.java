package org.openhds.mobile.model.form;

import android.content.ContentResolver;
import android.net.Uri;

import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.provider.InstanceProviderAPI;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static org.openhds.mobile.utilities.FormUtils.formFile;
import static org.openhds.mobile.utilities.FormUtils.generateODKForm;
import static org.openhds.mobile.utilities.FormUtils.loadInstance;
import static org.openhds.mobile.utilities.FormUtils.updateInstance;
import static org.openhds.mobile.utilities.OdkCollectHelper.getBlankInstance;
import static org.openhds.mobile.utilities.OdkCollectHelper.getInstance;

public class FormInstance implements Serializable {

    public static final String BINDING_MAP_KEY = "@cims-binding"; // valid xml element names can't collide
    public static final String BINDING_ATTR = "cims-binding";

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

    /**
     * Updates the form instance on disk with the supplied values.
     *
     * @param data key/value pairs corresponding to element names to update and their values
     * @throws IOException
     */
    public void store(Map<String, String> data) throws IOException {
        updateInstance(data, filePath);
    }

    /**
     * Fetches the form instance values from disk.
     *
     * @return a map of key/value pairs corresponding to elements and their values
     * @throws IOException
     */
    public Map<String, String> load() throws IOException {
        return loadInstance(filePath);
    }

    /**
     * Gives the configured form binding as identified from the given instance data.
     *
     * @param data instance data, possibly containing binding information
     * @return the configured form binding, or null if none is available for the data
     */
    public static Binding getBinding(Map<String, String> data) {
        return isBound(data) ? NavigatorConfig.getInstance().getBinding(data.get(BINDING_MAP_KEY)) : null;
    }

    /**
     * Determines whether the given instance data contains binding information.
     *
     * @param data the instance data
     * @return true if binding metadata is present, false otherwise
     */
    public static boolean isBound(Map<String, String> data) {
        return data.containsKey(BINDING_MAP_KEY);
    }

    /**
     * Retrieves a {@link FormInstance} from a specified {@link Uri}.
     *
     * @param resolver the content resolved to use for the lookup
     * @param uri the uri of the instance to lookup
     * @return a {@link FormInstance} object if found, null otherwise
     */
    public static FormInstance lookup(ContentResolver resolver, Uri uri) {
        return getInstance(resolver, uri);
    }

    /**
     * Generates a new {@link FormInstance}.
     *
     * @param resolver content resolver to use for instance registration with ODK
     * @param binding the binding to use for instance generation
     * @param data the form data to populate the new instance with
     * @return the {@link Uri} to a new form instance, registered with ODK
     * @throws IOException
     */
    public static Uri generate(ContentResolver resolver, Binding binding, Map<String, String> data) throws IOException {
        FormInstance template = getBlankInstance(resolver, binding.getForm());
        return generateODKForm(resolver, binding, template, data, formFile(template.getFileName(), new Date()));
    }

}
