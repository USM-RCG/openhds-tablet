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
            for (f in flds.indices) {
                val field = flds[f]
                val value = getFieldValue(f)
                if (value != null) {
                    field.setStringValue(value)
                } else {
                    doc.removeField(field.name())
                    nullFields.add(field)
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