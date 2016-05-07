package org.openhds.mobile.model.form;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import org.openhds.mobile.navconfig.forms.Binding;

import java.io.IOException;
import java.util.Map;

import static org.openhds.mobile.model.form.FormInstance.generate;
import static org.openhds.mobile.model.form.FormInstance.lookup;


public class FormHelper {

    private Binding binding;
    private ContentResolver resolver;
    private Map<String, String> data;
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
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public FormInstance getInstance(Uri uri) {
        return instance = lookup(resolver, uri);
    }

    public void update() throws IOException {
        instance.put(data);
    }

    public Map<String, String> fetch() throws IOException {
        return data = instance.get();
    }

    public Uri newInstance() throws IOException {
        return generate(resolver, binding, data);
    }
}
