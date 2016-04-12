package org.openhds.mobile.model.form;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.openhds.mobile.utilities.FormUtils.formFile;
import static org.openhds.mobile.utilities.FormUtils.generateODKForm;
import static org.openhds.mobile.utilities.FormUtils.loadInstance;
import static org.openhds.mobile.utilities.FormUtils.updateInstance;

public class FormHelper {

    private FormBehavior behavior;
    private ContentResolver resolver;
    private Context ctx;
    private Map<String, String> formData;
    private FormInstance instance;

    public FormHelper(Context ctx) {
        this.ctx = ctx;
        this.resolver = ctx.getContentResolver();
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

    public FormInstance getInstance(Uri instanceUri) {
        return instance = OdkCollectHelper.getInstance(resolver, instanceUri);
    }

    public void update() throws IOException {
        updateInstance(formData, instance.getFilePath());
    }

    public Map<String, String> fetch() throws IOException {
        return formData = loadInstance(instance.getFilePath());
    }

    public Uri newInstance() throws IOException {
        String formName = behavior.getFormName();
        return generateODKForm(resolver, formName, formData, formFile(formName, new Date()));
    }
}
