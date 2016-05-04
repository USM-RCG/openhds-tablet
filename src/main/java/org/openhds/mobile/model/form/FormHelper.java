package org.openhds.mobile.model.form;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import org.openhds.mobile.navconfig.forms.Binding;
import org.openhds.mobile.utilities.OdkCollectHelper;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.openhds.mobile.utilities.FormUtils.formFile;
import static org.openhds.mobile.utilities.FormUtils.generateODKForm;
import static org.openhds.mobile.utilities.FormUtils.loadInstance;
import static org.openhds.mobile.utilities.FormUtils.updateInstance;

public class FormHelper {

    private Binding binding;
    private ContentResolver resolver;
    private Map<String, String> formData;
    private FormInstance instance;

    public FormHelper(Context ctx) {
        this.resolver = ctx.getContentResolver();
    }

    public Binding getBinding() {
        return binding;
    }

    public void setForm(Binding binding) {
        this.binding = binding;
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
        return generateODKForm(resolver, binding, formData, formFile(binding.getForm(), new Date()));
    }
}
