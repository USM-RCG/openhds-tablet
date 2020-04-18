package org.cimsbioko.utilities;

import android.content.Intent;
import android.net.Uri;
import org.cimsbioko.App;
import org.cimsbioko.navconfig.UsedByJSConfig;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;


/**
 * Dumping grounds for repeated jdom code. It might be possible to eliminate this code entirely, but for now it
 * consolidates the repetition and minimizes the surface area depending on jdom classes.
 */
public class FormUtils {

    @UsedByJSConfig
    public static final Namespace XFORMS_NS = Namespace.getNamespace("http://www.w3.org/2002/xforms"),
            XHTML_NS = Namespace.getNamespace("http://www.w3.org/1999/xhtml"),
            JR_NS = Namespace.getNamespace("http://openrosa.org/javarosa");

    public static final String FILE_EXTENSION = ".xml";
    public static final String HEAD = "head";
    public static final String MODEL = "model";
    public static final String INSTANCE = "instance";

    /**
     * Loads the specified XML {@link java.io.InputStream} into a jdom2 {@link Document} object.
     *
     * @param stream the stream of the XML file to load
     * @return the jdom2 {@link Document} object, containing the contents of stream
     * @throws JDOMException
     * @throws IOException
     */
    public static Document domFromStream(InputStream stream) throws JDOMException, IOException {
        try {
            return new SAXBuilder().build(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Saves the specified {@link Document} object to the specified file.
     *
     * @param doc  the jdom2 {@link Document} to save
     * @param dest the file specifying the target location to save to
     * @throws IOException
     */
    public static void domToFile(Document doc, File dest) throws IOException {
        FileOutputStream out = new FileOutputStream(dest);
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getCompactFormat().setOmitDeclaration(true));
            xmlOutput.output(doc, out);
        } finally {
            out.close();
        }
    }

    /**
     * Saves the specified {@link Document} object to the specified {@link java.io.OutputStream}.
     *
     * @param doc  the jdom2 {@link Document} to save
     * @param stream the stream to save the document content to
     * @throws IOException
     */
    public static void domToStream(Document doc, OutputStream stream) throws IOException {
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getCompactFormat().setOmitDeclaration(true));
            xmlOutput.output(doc, stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Constructs a {@link File} object specifying the location to store a form.
     *
     * @return the filesystem location to save/load the form
     */
    public static File formFile() throws IOException {
        File genDir = new File(App.getApp().getCacheDir(), "generated_instances");
        boolean created = genDir.mkdir();
        return File.createTempFile("starter", FILE_EXTENSION, genDir);
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
    public static void saveForm(Document form, File location) throws IOException {
        makeDirs(location);
        domToFile(form, location);
    }

    /**
     * Generates a data document from a template form.
     *
     * @param template dom containing full form to use as template
     * @return a {@link Document} object containing just a blank main data element from the template
     */
    public static Document detachedDataDoc(Document template) {
        return new Document(getDataElement(template));
    }

    private static Element getDataElement(Document blank) {
        return blank
                .getRootElement()
                .getChild(HEAD, XHTML_NS)
                .getChild(MODEL, XFORMS_NS)
                .getChild(INSTANCE, XFORMS_NS)
                .getChildren()
                .get(0)
                .detach();
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
