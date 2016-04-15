package org.openhds.mobile.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.provider.DatabaseAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Environment.getExternalStorageDirectory;
import static org.openhds.mobile.utilities.OdkCollectHelper.getAllUnsentFormInstances;
import static org.openhds.mobile.utilities.OdkCollectHelper.getBlankInstance;
import static org.openhds.mobile.utilities.OdkCollectHelper.moveInstance;
import static org.openhds.mobile.utilities.OdkCollectHelper.registerInstance;

/**
 * Dumping grounds for repeated jdom code. It might be possible to eliminate this code entirely, but for now it
 * consolidates the repetition and minimizes the surface area depending on jdom classes.
 */
public class FormUtils {

    private static final String TAG = FormUtils.class.getSimpleName();

    public static final String FILE_EXTENSION = ".xml";

    /**
     * Loads the specified XML file into a jdom2 {@link Document} object.
     *
     * @param file the object specifying the location of the XML file to load
     * @return the jdom2 {@link Document} object, containing the contents of file
     * @throws JDOMException
     * @throws IOException
     */
    private static Document domFromFile(File file) throws JDOMException, IOException {
        return new SAXBuilder().build(file);
    }

    /**
     * Saves the specified {@link Document} object to the specified file.
     *
     * @param doc  the jdom2 {@link Document} to save
     * @param dest the file specifying the target location to save to
     * @throws IOException
     */
    private static void domToFile(Document doc, File dest) throws IOException {
        FileOutputStream out = new FileOutputStream(dest);
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(doc, out);
        } finally {
            out.close();
        }
    }

    /**
     * Formats the specified {@link Date} object for use in form file names.
     *
     * @param time the time of form creation
     * @return a formatted {@link String} acceptable for use in a form file name
     */
    private static String formatTime(Date time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        return df.format(time);
    }

    /**
     * Constructs the base name for a form. This is used to construct file and directory names.
     *
     * @param name the form id of the form
     * @param time the creation time of the form
     * @return the common base name used in file/dir name construction
     */
    private static String formBaseName(String name, Date time) {
        return String.format("%s%s", name, formatTime(time));
    }

    /**
     * Constructs the filename for a form.
     *
     * @param name the ODK instance id
     * @param time the form creation time
     * @return the formatted file name to use for the form
     */
    private static String formFilename(String name, Date time) {
        return String.format("%s%s", formBaseName(name, time), FILE_EXTENSION);
    }

    private static File getODKDir() {
        return new File(getExternalStorageDirectory(), "odk");
    }

    public static File getInstancesDir() {
        return new File(getODKDir(), "cims-instances");
    }

    /**
     * Constructs the location to store form instances for the given form name, by convention. It generates locations
     * on external storage since ODK Collect and this application communicate through the file system. Both applications
     * must be able to read and write to the location.
     *
     * @param name the form name to use as a subdirectory
     * @return the filesystem location to store instances of the named form at
     */
    public static File formDir(String name, Date time) {
        return new File(getInstancesDir(), formBaseName(name, time));
    }

    /**
     * Constructs a {@link File} object specifying the location to store a form.
     *
     * @param name the odk instance id (or form name)
     * @param time the creation time of the form
     * @return the filesystem location to save/load the form
     */
    public static File formFile(String name, Date time) {
        return new File(formDir(name, time), formFilename(name, time));
    }

    /**
     * Makes directories necessary to store the specified file.
     *
     * @param file the {@link File} object representing the file to save
     */
    private static void makeDirs(File file) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new IOException("failed to make directories for " + file);
            }
        }
    }

    /**
     * Saves the given {@link Document} as a form in the application's conventional repository format, creating any
     * intermediate directories as needed.
     *
     * @param form     the {@link Document} containing the form data
     * @param location the file system location to save the form at
     * @throws IOException
     */
    private static void saveForm(Document form, File location) throws IOException {
        makeDirs(location);
        domToFile(form, location);
    }

    /**
     * Generates a list of elements descending from root and matching the specified match criteria.
     */
    private static List<Element> descendants(Element root, Filter<Element> filter) {
        List<Element> results = new ArrayList<>();
        Iterator<Element> itr = root.getDescendants(filter);
        while (itr.hasNext()) {
            results.add(itr.next());
        }
        return results;
    }

    /**
     * Generates list of all elements descending from root.
     */
    private static List<Element> descendants(Element root) {
        return descendants(root, new ElementFilter());
    }

    /**
     * Generates a filled-out form instance document by combining a template file with matching answers.
     *
     * @param templateForm file containing blank form
     * @param data         a dictionary of answers to use when generating the completed form
     * @return a {@link Document} object containing the completed form
     * @throws JDOMException
     * @throws IOException
     */
    private static Document genInstanceDoc(File templateForm, Map<String, String> data) throws IOException, JDOMException {
        Document template = domFromFile(templateForm), completed = new Document();
        Element templateRoot = template.getRootElement();
        for (Element templateData : descendants(templateRoot, new ElementFilter("data", templateRoot.getNamespace("")))) {
            completed.setRootElement(templateData.detach());
            for (Element dataElement : descendants(templateData)) {
                String elementName = dataElement.getName();
                if (data.containsKey(elementName) && data.get(elementName) != null) {
                    dataElement.setText(data.get(elementName));
                }
            }
        }
        return completed;
    }

    /**
     * Updates the XML form at the specified path with the specified values.
     *
     * @param values values to update, keys are element names to update, values are values.
     * @param path   the absolute file system path of the xml file to update.
     * @throws IOException
     */
    public static void updateInstance(Map<String, String> values, String path) throws IOException {
        try {
            Document orig = domFromFile(new File(path));
            for (Element element : descendants(orig.getRootElement())) {
                String elementName = element.getName();
                if (values.containsKey(elementName)) {
                    element.setText(values.get(elementName));
                }
            }
            domToFile(orig, new File(path));
        } catch (JDOMException e) {
            throw new IOException("failed to parse form " + path, e);
        }
    }

    /**
     * Retrieves values from the XML form at the specified path.
     *
     * @param path the filesystem path to the XML document to load
     * @return a map of values from the document, keys are element name, values are the associated values
     * @throws IOException
     */
    public static Map<String, String> loadInstance(String path) throws IOException {
        Map<String, String> formValues = new HashMap<>();
        if (path != null) {
            try {
                Document formDoc = domFromFile(new File(path));
                for (Element dataElement : descendants(formDoc.getRootElement())) {
                    List<Element> children = dataElement.getChildren();
                    if (children == null || children.isEmpty()) {
                        formValues.put(dataElement.getName(), dataElement.getText());
                    }
                }
            } catch (JDOMException e) {
                throw new IOException("failed to parse form " + path, e);
            }
        }
        return formValues;
    }

    /**
     * A convenience method: updates a single element in the XML file at the specified path.
     *
     * @param name  the name of the element to update
     * @param value the value of the element
     * @param path  the filesystem path of the xml document to update
     * @throws IOException
     */
    public static void updateFormElement(String name, String value, String path) throws IOException {
        Map<String, String> formData = loadInstance(path);
        formData.put(name, value);
        updateInstance(formData, path);
    }

    /**
     * A convenience method: retrieves a single value from the XML file at the specified path.
     *
     * @param name the element name to retrieve
     * @param path the path of the XML file to retrieve from
     * @return the value of the last element matching the name from the file at path
     * @throws IOException
     */
    public static String getFormElement(String name, String path) throws IOException {
        return loadInstance(path).get(name);
    }


    /**
     * A convenience method: determines whether the form at the specified path contains a special value denoting that
     * the form does not require approval by a supervisor. The sentinel value denoting this was apparently inverted when
     * it was first implemented: needsReview = 1 indicates the form *does not* need approval, while any other value (or
     * lack of a value) means the form *does* need approval.
     *
     * @param path filesystem path of the form to inspect
     * @return true if the form contains an element named 'needsReview' with text value of '1', false otherwise
     * @throws IOException
     */
    public static boolean isFormReviewed(String path) throws IOException {
        String needsReview = getFormElement(ProjectFormFields.General.NEEDS_REVIEW, path);
        return ProjectResources.General.FORM_NO_REVIEW_NEEDED.equalsIgnoreCase(needsReview);
    }

    /**
     * Generates and registers a new XML form with the specified values and registers it with ODK.
     *
     * @param resolver
     * @param name     the odk form instance id of the blank form to use as template
     * @param values   name/value pairs specifying values to merge with the blank form
     * @param location the filesystem location to save the generated form to, it must be accessible to ODK Collect
     * @return the content {@link Uri} of the form registered with ODK
     * @throws IOException
     */
    public static Uri generateODKForm(ContentResolver resolver, String name, Map<String, String> values, File location)
            throws IOException {
        FormInstance template = getBlankInstance(resolver, name);
        if (template != null) {
            String tName = template.getFormName(), tVersion = template.getFormVersion(), tPath = template.getFilePath();
            File tFile = new File(tPath);
            try {
                saveForm(genInstanceDoc(tFile, values), location);
                return registerInstance(resolver, location, location.getName(), tName, tVersion);
            } catch (JDOMException e) {
                throw new IOException("failed to fill out form", e);
            }
        } else {
            throw new FileNotFoundException("form " + name + " not found");
        }
    }

    /**
     * Migrates unsent form instances from the pre-2.3 instance storage format.
     * <p/>
     * Specifically, it performs the following:
     * <ul>
     * <li>Using the ODK content provider, lookup unsent form instances</li>
     * <li>For each form instance with a path under the old form directory:</li>
     * <ul>
     * <li>Using the existing file name, create an instance directory under the new form root</li>
     * <li>Move the form instance file from the existing path to the new directory</li>
     * <li>Update the form instance's path to the new location using the ODK content provider</li>
     * </ul>
     * </ul>
     * <p/>
     * The existing form paths look like:
     * <p/>
     * {Environment.getExternalStorageDirectory()}/Android/data/org.openhds.mobile/files/{formid}/{formid}YYYY-MM-DD_HH:MM:SS.xml
     * <p/>
     * Where {formid} is the id from the form such as 'duplicate_location' or 'location'.
     * <p/>
     * The new form paths look like:
     * <p/>
     * {Environment.getExternalStorageDirectory()}/odk/cims-instances/{formid}YYYY-MM-DD_HH-MM-SS/{formid}YYYY-MM-DD_HH-MM-SS.xml
     *
     * @param ctx used to perform lookup and update form instances in ODK and local databases
     */
    public static void migrateTo23Storage(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        File oldRoot = new File(getExternalStorageDirectory(), "Android/data/org.openhds.mobile/files");
        String oldRootPath = oldRoot.getAbsolutePath();
        if (oldRoot.exists() && oldRoot.isDirectory()) {
            Pattern oldSuffixPattern = Pattern.compile("(\\w+\\d{4}-\\d{2}-\\d{2}_\\d{2}:\\d{2}:\\d{2}).xml$");
            for (FormInstance instance : getAllUnsentFormInstances(resolver)) {
                String existingPath = instance.getFilePath();
                if (existingPath != null && existingPath.startsWith(oldRootPath)) {
                    Matcher oldSuffixMatcher = oldSuffixPattern.matcher(existingPath);
                    if (oldSuffixMatcher.find()) {
                        String newBaseName = oldSuffixMatcher.group(1).replace(':', '-');
                        File newDir = new File(getInstancesDir(), newBaseName);
                        try {
                            File newFile = new File(newDir, newBaseName + FILE_EXTENSION);
                            moveInstance(resolver, instance, newFile);
                            DatabaseAdapter.getInstance(ctx).updateAttachedPath(existingPath, newFile.getAbsolutePath());
                        } catch (IOException e) {
                            Log.w(TAG, "v2.3 migration error - " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates an {@link Intent} that can be used to edit a form using ODK Collect.
     *
     * @param formUri the content {@link Uri} for the form instance to edit
     * @return an {@link Intent} useful for launching the editing activity in ODK
     */
    public static Intent editIntent(Uri formUri) {
        return new Intent(Intent.ACTION_EDIT, formUri);
    }
}
