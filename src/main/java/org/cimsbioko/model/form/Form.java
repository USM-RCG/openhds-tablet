package org.cimsbioko.model.form;

import org.cimsbioko.navconfig.forms.Binding;

import java.io.FileNotFoundException;
import java.io.Serializable;

import static org.cimsbioko.utilities.FormsHelper.getForm;

public class Form implements Serializable {

    private final Integer id;
    private final String formId;
    private final String formVersion;
    private final String fileName;
    private final String filePath;

    public Form(Integer id, String formId, String formVersion, String fileName, String filePath) {
        this.id = id;
        this.formId = formId;
        this.formVersion = formVersion;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public static Form lookup(Binding binding) throws FileNotFoundException {
        String formId = binding.getForm();
        Form template = getForm(formId);
        if (template == null) {
            throw new FileNotFoundException("form " + formId + " not found");
        }
        return template;
    }

    public Integer getId() {
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

    public String getFilePath() {
        return filePath;
    }
}
