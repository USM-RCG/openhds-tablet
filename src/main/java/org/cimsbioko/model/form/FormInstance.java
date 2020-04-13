package org.cimsbioko.model.form;

import android.content.ContentUris;
import android.net.Uri;
import org.cimsbioko.App;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.provider.InstanceProviderAPI;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;

import java.io.*;
import java.util.Date;

import static org.cimsbioko.utilities.FormUtils.*;
import static org.cimsbioko.utilities.FormsHelper.*;
import static org.cimsbioko.utilities.SetupUtils.getCampaignId;

public class FormInstance implements Serializable {

    public static final String BINDING_ATTR = "cims-binding";
    public static final String CAMPAIGN_ATTR = "cims-campaign";

    private static final long serialVersionUID = 1L;

    private Long id;
    private String formName;
    private String filePath;
    private String fileName;
    private String formVersion;
    private String status;
    private boolean canEditWhenComplete;

    public FormInstance(Long id, String formName, String filePath, String fileName, String formVersion, String status, boolean canEditWhenComplete) {
        this.id = id;
        this.formName = formName;
        this.filePath = filePath;
        this.fileName = fileName;
        this.formVersion = formVersion;
        this.status = status;
        this.canEditWhenComplete = canEditWhenComplete;
    }

    public Long getId() {
        return id;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFormName() {
        return formName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStatus() {
        return status;
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

    public boolean isCanEditWhenComplete() {
        return canEditWhenComplete;
    }

    public boolean canEdit() {
        return !(isSubmitted() || (isComplete() && !canEditWhenComplete));
    }

    /**
     * Updates the form instance on disk with the supplied values.
     *
     * @param data key/value pairs corresponding to element names to update and their values
     * @throws IOException
     */
    public void store(Document data) throws IOException {
        domToStream(data, getOutputStream());
    }

    /**
     * Fetches the form instance values from disk.
     *
     * @return a map of key/value pairs corresponding to elements and their values
     * @throws IOException
     */
    public Document load() throws IOException, JDOMException {
        return domFromStream(getInputStream());
    }

    private OutputStream getOutputStream() throws FileNotFoundException {
        return App.getApp().getContentResolver().openOutputStream(getUri());
    }

    private InputStream getInputStream() throws FileNotFoundException {
        return App.getApp().getContentResolver().openInputStream(getUri());
    }

    public Uri getUri() {
        return ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI, id);
    }

    /**
     * Gives the configured form binding as identified from the given instance data.
     *
     * @param data form data as in-memory dom object, possibly containing binding information
     * @return the configured form binding, or null if none is available for the data
     */
    public static Binding getBinding(Document data) {
        return isBound(data) ? NavigatorConfig.getInstance().getBinding(data.getRootElement().getAttribute(BINDING_ATTR).getValue()) : null;
    }

    /**
     * Determines whether the given instance data contains binding information.
     *
     * @param data the instance data
     * @return true if binding metadata is present, false otherwise
     */
    public static boolean isBound(Document data) {
        return data.getRootElement().getAttribute(BINDING_ATTR) != null;
    }

    /**
     * Retrieves a {@link FormInstance} from a specified {@link Uri}.
     *
     * @param uri the uri of the instance to lookup
     * @return a {@link FormInstance} object if found, null otherwise
     */
    public static FormInstance lookup(Uri uri) {
        return getInstance(uri);
    }

    /**
     * Generates a new {@link FormInstance} using the given binding and launch context.
     *
     * @param binding the binding to use for instance generation
     * @param ctx the launch context to use while generating the form
     * @return the {@link Uri} to a new form instance, registered with Forms
     * @throws IOException
     */
    public static Uri generate(Binding binding, LaunchContext ctx) throws IOException, JDOMException {

        Form template = Form.lookup(binding);
        Document formData = newFormData(template);
        Element root = formData.getRootElement();

        root.setAttribute(BINDING_ATTR, binding.getName());

        String campaignId = getCampaignId();
        if (campaignId != null) {
            root.setAttribute(CAMPAIGN_ATTR, campaignId);
        }

        binding.getBuilder().build(formData, ctx);

        File targetFile = formFile(template.getFileName(), new Date());
        saveForm(formData, targetFile);

        return registerInstance(targetFile, targetFile.getName(), template.getFormId(), template.getFormVersion());
    }

    private static Document newFormData(Form template) throws JDOMException, IOException {
        return clearDeclaredNs(detachedDataDoc(domFromStream(template.getInputStream())));
    }

    private static Document clearDeclaredNs(Document document) {
        Element root = document.getRootElement();
        Namespace rootDefaultNs = root.getNamespace("");
        if (!rootDefaultNs.equals(Namespace.NO_NAMESPACE)) {
            for (Element e : document.getDescendants(Filters.element(rootDefaultNs))) {
                e.setNamespace(Namespace.NO_NAMESPACE);
            }
        }
        return document;
    }
}
