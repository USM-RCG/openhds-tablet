package org.cimsbioko.model.form;

import android.content.ContentUris;
import android.net.Uri;
import org.cimsbioko.App;
import org.cimsbioko.navconfig.forms.Binding;
import org.cimsbioko.provider.FormsProviderAPI;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import static org.cimsbioko.utilities.FormsHelper.getForm;

public class Form implements Serializable {

    private final Long id;
    private final String formId;
    private final String formVersion;
    private final String fileName;

    public Form(Long id, String formId, String formVersion, String fileName) {
        this.id = id;
        this.formId = formId;
        this.formVersion = formVersion;
        this.fileName = fileName;
    }

    public static Form lookup(Binding binding) throws FileNotFoundException {
        String formId = binding.getForm();
        Form template = getForm(formId);
        if (template == null) {
            throw new FileNotFoundException("form " + formId + " not found");
        }
        return template;
    }

    public Long getId() {
        return id;
    }

    public String getFormId() {
        return formId;
    }

    public String getFormVersion() {
        return formVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        Uri formUri = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, id);
        return App.getApp().getContentResolver().openInputStream(formUri);
    }
}
