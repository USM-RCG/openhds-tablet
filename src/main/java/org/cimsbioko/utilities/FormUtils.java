package org.cimsbioko.utilities;

import android.content.Intent;
import android.net.Uri;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.cimsbioko.model.form.FormInstance;
import org.cimsbioko.navconfig.forms.Binding;

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

import static org.cimsbioko.model.form.FormInstance.BINDING_ATTR;
import static org.cimsbioko.model.form.FormInstance.BINDING_MAP_KEY;
import static org.cimsbioko.utilities.FormsHelper.registerInstance;

/**
 * Dumping grounds for repeated jdom code. It might be possible to eliminate this code entirely, but for now it
 * consolidates the repetition and minimizes the surface area depending on jdom classes.
 */
public class FormUtils {

    private static final String TAG = FormUtils.class.getSimpleName();

    public static final String FILE_EXTENSION = ".xml";
    public static final String HEAD = "head";
    public static final String MODEL = "model";
    public static final String INSTANCE = "instance";

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
        return String.format("%s_%s", name, formatTime(time));
    }

    /**
     * Constructs the filename for a form.
     *
     * @param name the form instance id
     * @param time the form creation time
     * @return the formatted file name to use for the form
     */
    private static String formFilename(String name, Date time) {
        return String.format("%s%s", formBaseName(name, time), FILE_EXTENSION);
    }

    private static File getFormsDir() {
        return SyncUtils.getExternalDir();
    }

    public static File getInstancesDir() {
        return new File(getFormsDir(), "instances");
    }

    /**
     * Constructs the location to store form instances for the given form name, by convention. It generates locations
     * on external storage since CIMS Forms and this application communicate through the file system. Both applications
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
     * @param name the form instance id (or form name)
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

        Namespace xformsNs = Namespace.getNamespace("http://www.w3.org/2002/xforms"),
                xhtmlNs = Namespace.getNamespace("http://www.w3.org/1999/xhtml");

        Element instance = domFromFile(templateForm)
                .getRootElement()
                .getChild(HEAD, xhtmlNs)
                .getChild(MODEL, xformsNs)
                .getChild(INSTANCE, xformsNs)
                .getChildren()
                .get(0)
                .detach();

        // add the binding name as a root-level attribute, if present
        if (data.containsKey(BINDING_MAP_KEY)) {
            instance.setAttribute(BINDING_ATTR, data.get(BINDING_MAP_KEY));
        }

        // fill out the form instance elements with supplied values
        for (Element dataElement : descendants(instance)) {
            String elementName = dataElement.getName();
            if (data.containsKey(elementName) && data.get(elementName) != null) {
                dataElement.setText(data.get(elementName));
            }
        }

        return new Document(instance);
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
                Element root = formDoc.getRootElement();

                // add the instance binding name (an abnormal value)
                Attribute bindingAttr = root.getAttribute(BINDING_ATTR);
                if (bindingAttr != null) {
                    formValues.put(BINDING_MAP_KEY, bindingAttr.getValue());
                }

                // add the instance elements
                for (Element dataElement : descendants(root)) {
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
     * Generates and registers a new XML form with the specified values and registers it with CIMS Forms.
     *
     * @param values   name/value pairs specifying values to merge with the blank form
     * @param location the filesystem location to save the generated form to, it must be accessible to CIMS Forms
     * @return the content {@link Uri} of the form registered with CIMS Forms
     * @throws IOException
     */
    public static Uri generateForm(FormInstance template, Map<String, String> values, File location)
            throws IOException {
        String tName = template.getFormName(), tVersion = template.getFormVersion(), tPath = template.getFilePath();
        File tFile = new File(tPath);
        try {
            saveForm(genInstanceDoc(tFile, values), location);
            return registerInstance(location, location.getName(), tName, tVersion);
        } catch (JDOMException e) {
            throw new IOException("failed to fill out form", e);
        }
    }

    /**
     * Creates an {@link Intent} that can be used to edit a form using CIMS Forms.
     *
     * @param formUri the content {@link Uri} for the form instance to edit
     * @return an {@link Intent} useful for launching the editing activity in CIMS Forms
     */
    public static Intent editIntent(Uri formUri) {
        return new Intent(Intent.ACTION_EDIT, formUri);
    }
}
