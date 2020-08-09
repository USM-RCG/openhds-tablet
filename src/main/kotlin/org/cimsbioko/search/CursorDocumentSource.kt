package org.cimsbioko.search

import android.database.Cursor
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import java.util.*

internal abstract class CursorDocumentSource(protected val cursor: Cursor) : DocumentSource {

    private val doc: Document = Document()

    private lateinit var flds: Array<Field>
    private lateinit var nullFields: MutableList<Field>

    abstract val fields: Array<Field>
    abstract fun getFieldValue(index: Int): String?

    override val document: Document
        get() {
            // add field back to document before we start next
            if (nullFields.isNotEmpty()) {
                for (nullField in nullFields) {
                    doc.add(nullField)
                }
                nullFields.clear()
            }

            // manipulate document to match next cursor record, remove fields for null values
            for ((i, fld) in flds.withIndex()) {
                val value = getFieldValue(i)
                if (value != null) {
                    fld.setStringValue(value)
                } else {
                    doc.removeField(fld.name())
                    nullFields.add(fld)
                }
            }
            return doc
        }

    override fun next(): Boolean {
        val isFirst = cursor.isBeforeFirst
        val hasNext = cursor.moveToNext()
        if (isFirst && hasNext) {
            flds = fields
            nullFields = ArrayList(flds.size)
            for (field in flds) {
                doc.add(field)
            }
        }
        return hasNext
    }

    override fun size(): Int {
        return cursor.count
    }

    override fun close() {
        cursor.close()
    }
}