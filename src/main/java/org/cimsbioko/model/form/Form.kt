package org.cimsbioko.model.form

import android.content.ContentUris
import android.net.Uri
import org.cimsbioko.App
import org.cimsbioko.navconfig.forms.Binding
import org.cimsbioko.provider.FormsProviderAPI
import org.cimsbioko.utilities.FormsHelper.getForm
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.Serializable

data class Form(val id: Long, val formId: String, val formVersion: String, val fileName: String) : Serializable {

    @get:Throws(FileNotFoundException::class)
    val inputStream: InputStream?
        get() = App.instance.contentResolver.openInputStream(uri)

    val uri: Uri
        get() = ContentUris.withAppendedId(FormsProviderAPI.FormsColumns.CONTENT_URI, id)

    companion object {
        @JvmStatic
        @Throws(FileNotFoundException::class)
        fun lookup(binding: Binding): Form {
            val formId = binding.form
            return getForm(formId) ?: throw FileNotFoundException("form $formId not found")
        }
    }

}