package org.cimsbioko.utilities

import android.content.Intent
import android.net.Uri
import org.cimsbioko.App
import org.cimsbioko.navconfig.UsedByJSConfig
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import java.io.*

/**
 * Dumping grounds for repeated jdom code. It might be possible to eliminate this code entirely, but for now it
 * consolidates the repetition and minimizes the surface area depending on jdom classes.
 */
object FormUtils {

    private const val FILE_EXTENSION = ".xml"
    private const val HEAD = "head"
    private const val MODEL = "model"
    private const val INSTANCE = "instance"

    @UsedByJSConfig
    val XFORMS_NS: Namespace = Namespace.getNamespace("http://www.w3.org/2002/xforms")

    @UsedByJSConfig
    val XHTML_NS: Namespace = Namespace.getNamespace("http://www.w3.org/1999/xhtml")

    @UsedByJSConfig
    val JR_NS: Namespace = Namespace.getNamespace("http://openrosa.org/javarosa")

    /**
     * Loads the specified XML [java.io.InputStream] into a jdom2 [Document] object.
     *
     * @param stream the stream of the XML file to load
     * @return the jdom2 [Document] object, containing the contents of stream
     * @throws JDOMException
     * @throws IOException
     */
    @JvmStatic
    @Throws(JDOMException::class, IOException::class)
    fun domFromStream(stream: InputStream): Document = stream.use { s -> SAXBuilder().build(s) }

    /**
     * Saves the specified [Document] object to the specified file.
     *
     * @param doc  the jdom2 [Document] to save
     * @param dest the file specifying the target location to save to
     * @throws IOException
     */
    @Throws(IOException::class)
    fun domToFile(doc: Document?, dest: File?) {
        FileOutputStream(dest).use { o ->
            val xmlOutput = XMLOutputter()
            xmlOutput.format = Format.getCompactFormat().setOmitDeclaration(true)
            xmlOutput.output(doc, o)
        }
    }

    /**
     * Saves the specified [Document] object to the specified [java.io.OutputStream].
     *
     * @param doc  the jdom2 [Document] to save
     * @param stream the stream to save the document content to
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun domToStream(doc: Document, stream: OutputStream) {
        stream.use { s ->
            val xmlOutput = XMLOutputter()
            xmlOutput.format = Format.getCompactFormat().setOmitDeclaration(true)
            xmlOutput.output(doc, s)
        }
    }

    /**
     * Constructs a [File] object specifying the location to store a form.
     *
     * @return the filesystem location to save/load the form
     */
    @JvmStatic
    @Throws(IOException::class)
    fun formFile(): File {
        val genDir = File(App.instance.cacheDir, "generated_instances").also { it.mkdir() }
        return File.createTempFile("starter", FILE_EXTENSION, genDir)
    }

    /**
     * Makes directories necessary to store the specified file.
     *
     * @param file the [File] object representing the file to save
     */
    @Throws(IOException::class)
    private fun makeDirs(file: File) {
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            val created = parentDir.mkdirs()
            if (!created) {
                throw IOException("failed to make directories for $file")
            }
        }
    }

    /**
     * Saves the given [Document] as a form in the application's conventional repository format, creating any
     * intermediate directories as needed.
     *
     * @param form     the [Document] containing the form data
     * @param location the file system location to save the form at
     * @throws IOException
     */
    @JvmStatic
    @Throws(IOException::class)
    fun saveForm(form: Document, location: File) {
        makeDirs(location)
        domToFile(form, location)
    }

    /**
     * Generates a data document from a template form.
     * @return a [Document] object containing just a blank main data element from this
     */
    @JvmStatic
    fun Document.detachDataDoc(): Document = Document(getDataElement(this))

    private fun getDataElement(blank: Document): Element = blank
            .rootElement
            .getChild(HEAD, XHTML_NS)
            .getChild(MODEL, XFORMS_NS)
            .getChild(INSTANCE, XFORMS_NS)
            .children[0]
            .detach()

    /**
     * Creates an [Intent] that can be used to edit a form using CIMS Forms.
     *
     * @param formUri the content [Uri] for the form instance to edit
     * @return an [Intent] useful for launching the editing activity in CIMS Forms
     */
    @JvmStatic
    fun editIntent(formUri: Uri): Intent = Intent(Intent.ACTION_EDIT, formUri)
}