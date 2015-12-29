package org.openhds.mobile.model.form;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.provider.FormsProviderAPI;
import org.openhds.mobile.provider.InstanceProviderAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.openhds.mobile.repository.RepositoryUtils.LIKE;
import static org.openhds.mobile.repository.RepositoryUtils.LIKE_WILD_CARD;

public class FormHelper {

    private FormBehaviour formBehaviour;
    private Uri contentUri;
    private ContentResolver contentResolver;
    private Map<String, String> formFieldData;
    private String finalizedFormFilePath;

    public FormHelper(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public String getFinalizedFormFilePath() {
        return finalizedFormFilePath;
    }

    public FormBehaviour getFormBehaviour() {
        return formBehaviour;
    }

    public void setFormBehaviour(FormBehaviour formBehaviour) {
        this.formBehaviour = formBehaviour;
    }

    public Map<String, String> getFormFieldData() {
        return formFieldData;
    }

    public void setFormFieldData(Map<String, String> formFieldData) {
        this.formFieldData = formFieldData;
    }

    public Intent buildEditFormInstanceIntent() {
        return new Intent(Intent.ACTION_EDIT, contentUri);
    }

    // Pull out to ODKCollectHelper
    public boolean checkFormInstanceStatus() {
        final String[] columnNames = new String[]{
                InstanceProviderAPI.InstanceColumns.STATUS,
                InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH};
        Cursor cursor = contentResolver.query(contentUri, columnNames,
                InstanceProviderAPI.InstanceColumns.STATUS + "=?",
                new String[]{InstanceProviderAPI.STATUS_COMPLETE}, null);

        if (cursor.moveToNext()) {
            finalizedFormFilePath =
                    cursor.getString(cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));
            cursor.close();
            return true;
        } else {
            finalizedFormFilePath = null;
            cursor.close();
            return false;
        }
    }

    public static String getFormTagValue(String tag, String formFilePath) {
        Map<String, String> formData = FormHelper.fetchFormInstanceData(formFilePath);
        if (null == formData) {
            return null;
        } else {
            return formData.get(tag);
        }
    }

    public static boolean isFormReviewed(String formFilePath) {
        String needsReview = FormHelper.getFormTagValue(ProjectFormFields.General.NEEDS_REVIEW, formFilePath);
        return needsReview != null && needsReview.equalsIgnoreCase(ProjectResources.General.FORM_NO_REVIEW_NEEDED);
    }

    public static boolean setFormTagValue(String tag, String value, String formFilePath) {
        Map<String, String> formFieldMap = FormHelper.fetchFormInstanceData(formFilePath);
        formFieldMap.put(tag, value);
        return FormHelper.updateExistingFormInstance(formFieldMap, formFilePath);
    }

    private static boolean updateExistingFormInstance(Map<String, String> formFieldMap, String formFilePath) {

        try {
            SAXBuilder builder = new SAXBuilder();

            Document oldForm = builder.build(new File(formFilePath));

            Element root = oldForm.getRootElement();

            Iterator<Element> itr = root.getDescendants(new ElementFilter());

            Map<Element,String> updatez = new HashMap<>();


            while (itr.hasNext()) {

                Element child = itr.next();

                String childName = child.getName();

                if(formFieldMap.containsKey(childName)){
                    updatez.put(child, formFieldMap.get(childName));
                }

            }

            for(Map.Entry<Element,String> entry : updatez.entrySet()){

                entry.getKey().setText(entry.getValue());

            }

            FileOutputStream fos = new FileOutputStream(formFilePath);
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(oldForm, fos);
            fos.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // this method assumes/relies on finalizedFormFilePath is NOT null.
    public boolean updateExistingFormInstance() {
        return updateExistingFormInstance(formFieldData,finalizedFormFilePath);
    }


    public static Map<String, String> fetchFormInstanceData(String formFilePath) {
        Map<String, String> formFields = new HashMap<>();
        if (null == formFilePath) {
            return null;
        }

        SAXBuilder builder = new SAXBuilder();
        try {
            Document finalizedDoc = builder.build(new File(formFilePath));
            Element root = finalizedDoc.getRootElement();
            Iterator<Element> itr = root.getDescendants(new ElementFilter());

            while (itr.hasNext()) {
                Element child = itr.next();
                List<Element> childsChildren = child.getChildren();
                if(null == childsChildren || childsChildren.isEmpty()){
                    formFields.put(child.getName(), child.getText());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return formFields;
    }

    public Map<String, String> fetchFormInstanceData() {
        formFieldData = fetchFormInstanceData(finalizedFormFilePath);
        return formFieldData;
    }


    public FormInstance newFormInstance() throws JDOMException, IOException {
        this.finalizedFormFilePath = null;
        FormInstance formInstance = new FormInstance();

        // find a form definition with the name of the current form behaviour
        final String[] columnNames = new String[]{
                FormsProviderAPI.FormsColumns.JR_FORM_ID,
                FormsProviderAPI.FormsColumns.FORM_FILE_PATH,
                FormsProviderAPI.FormsColumns.JR_VERSION};
        Cursor cursor = contentResolver.query(
                FormsProviderAPI.FormsColumns.CONTENT_URI, columnNames,
                FormsProviderAPI.FormsColumns.JR_FORM_ID + " " + LIKE + " ?",
                new String[]{formBehaviour.getFormName() + LIKE_WILD_CARD}, null);

        // read the path and type for the new form instance
        if (cursor.moveToFirst()) {
            formInstance.setFormName(cursor.getString(0));
            formInstance.setFilePath(cursor.getString(1));
            formInstance.setFormVersion(cursor.getString(2));
            cursor.close();
        } else {
            cursor.close();
            return null;
        }


        // populate the fields of the new form instance
        SAXBuilder builder = new SAXBuilder();

        // get reference to unfilled form
        Document blankDoc = builder.build(new File(formInstance.getFilePath()));
        Element root = blankDoc.getRootElement();
        ElementFilter filter = new ElementFilter("data");
        Document filledForm = new Document();
        Iterator<Element> itr = root.getDescendants(filter);

        if (itr.hasNext()) {

            Element filledFormRoot = itr.next();
            filledFormRoot.detach();
            filledForm.setRootElement(filledFormRoot);
            Iterator<Element> dataDescendantsItr = filledFormRoot.getDescendants(new ElementFilter());

            Map<Element, String> toModify = new HashMap<>();

            while (dataDescendantsItr.hasNext()) {

                Element child = dataDescendantsItr.next();
                String name = child.getName();

                boolean isNestedElement = child.getParentElement().hasAttributes();

                if (child.getParentElement() != filledFormRoot && isNestedElement) {
                    filledFormRoot.removeChild(name);
                }

                if (formFieldData.containsKey(name) && null != formFieldData.get(name)) {
                    toModify.put(child, formFieldData.get(name));
                }

            }

            for (Element child : toModify.keySet()) {
                child.setText(toModify.get(child));
            }
        }

        // write out the filled-in form instance
        File editableFormFile = getExternalStorageXmlFile(formInstance.getFormName(), formBehaviour.getFormName(), ".xml");
        FileOutputStream fileOutputStream = new FileOutputStream(editableFormFile);
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(filledForm, fileOutputStream);
        fileOutputStream.close();

        contentUri = shareOdkFormInstance(editableFormFile, editableFormFile.getName(), formInstance.getFormName(), formInstance.getFormVersion());

        return formInstance;
    }

    // ODKCollectHelper
    private Uri shareOdkFormInstance(File targetFile, String displayName, String formId, String versionNumber) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, targetFile.getAbsolutePath());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, displayName);
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, formId);
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, versionNumber);
        return contentResolver.insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
    }

    private File getExternalStorageXmlFile(String subDir, String baseName, String extension) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(new Date());
        String externalFileName = baseName + date + extension;

        File root = Environment.getExternalStorageDirectory();
        String destinationPath = root.getAbsolutePath() + File.separator
                + "Android" + File.separator + "data" + File.separator
                + "org.openhds.mobile" + File.separator + "files"
                + File.separator + subDir;
        File parentDir = new File(destinationPath);
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                return null;
            }
        }

        destinationPath += File.separator + externalFileName;
        return new File(destinationPath);
    }
}
