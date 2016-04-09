package org.openhds.mobile.model.form;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import org.openhds.mobile.utilities.FormUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.openhds.mobile.utilities.FormUtils.generateODKForm;
import static org.openhds.mobile.utilities.FormUtils.loadInstance;
import static org.openhds.mobile.utilities.FormUtils.updateInstance;
import static org.openhds.mobile.utilities.OdkCollectHelper.completedFormPath;

public class FormHelper {

    private FormBehavior behavior;
    private Uri instanceUri;
    private ContentResolver resolver;
    private Map<String, String> formData;
    private String completedFormPath;

    public FormHelper(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public String getCompletedFormPath() {
        return completedFormPath;
    }

    public FormBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(FormBehavior behavior) {
        this.behavior = behavior;
    }

    public Map<String, String> getData() {
        return formData;
    }

    public void setData(Map<String, String> formData) {
        this.formData = formData;
    }

    public Intent editIntent() {
        return FormUtils.editIntent(instanceUri);
    }

    public boolean loadCompletedForm() {
        try {
            completedFormPath = completedFormPath(resolver, instanceUri);
        } catch (FileNotFoundException e) {
            completedFormPath = null;
        }
        return completedFormPath != null;
    }

    public void update() throws IOException {
        updateInstance(formData, completedFormPath);
    }

    public Map<String, String> fetch() throws IOException {
        return formData = loadInstance(completedFormPath);
    }

    public void newInstance() throws IOException {
        this.completedFormPath = null;
        instanceUri = generateODKForm(resolver, behavior.getFormName(), formData);
    }
}
