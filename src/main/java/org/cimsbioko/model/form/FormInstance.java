package org.cimsbioko.model.form;

import android.net.Uri;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.navconfig.forms.LaunchContext;
import org.cimsbioko.provider.InstanceProviderAPI;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import static org.cimsbioko.utilities.FormUtils.*;
import static org.cimsbioko.utilities.FormsHelper.*;

public class FormInstance implements Serializable {

    public static final String BINDING_ATTR = "cims-binding";

    private static final long serialVersionUID = 1L;

    private String formName;
    private String filePath;
    private String fileName;
    private String uriString;
    private String formVersion;
    private String status;
    private boolean canEditWhenComplete;

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

    public boolean isCanEditWhenComplete() {
        return canEditWhenComplete;
    }

    public void setCanEditWhenComplete(boolean canEditWhenComplete) {
        this.canEditWhenComplete = canEditWhenComplete;
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
        domToFile(data, new File(filePath));
    }

    /**
     * Fetches the form instance values from disk.
     *
     * @return a map of key/value pairs corresponding to elements and their values
     * @throws IOException
     */
    public Document load() throws IOException, JDOMException {
        if (filePath == null) {
            throw new RuntimeException("failed to load form, path was null");
        }
        return domFromFile(new File(filePath));
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

        FormInstance template = getTemplate(binding);

        File sourceFile = new File(template.getFilePath()), targetFile = formFile(template.getFileName(), new Date());

        Document formData = newFormData(sourceFile);
        formData.getRootElement().setAttribute(BINDING_ATTR, binding.getName());
        binding.getBuilder().build(formData, ctx);

        saveForm(formData, targetFile);

        return registerInstance(targetFile, targetFile.getName(), template.getFormName(), template.getFormVersion());
    }

    private static Document newFormData(File templateFile) throws JDOMException, IOException {
        return clearDeclaredNs(detachedDataDoc(domFromFile(templateFile)));
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

    private static FormInstance getTemplate(Binding binding) throws FileNotFoundException {
        String formId = binding.getForm();
        FormInstance template = getBlankInstance(formId);
        if (template == null) {
            throw new FileNotFoundException("form " + formId + " not found");
        }
        return template;
    }
}
