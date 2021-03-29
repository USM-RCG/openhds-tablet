package org.cimsbioko.search

import android.database.Cursor
import org.apache.lucene.document.Field

interface SearchSource {
    val query: String
    val fields: List<SearchField>
    fun getValues(columnValues: Map<String, String?>): Map<String, String?>
}

interface SearchField {
    val name: String
    val isStored: Boolean
    val isIndexed: Boolean
    val isAnalyzed: Boolean
}

internal class CampaignDocumentSource(private val searchSource: SearchSource, c: Cursor) : CursorDocumentSource(c) {

    override val fields: Array<Field> = searchSource.fields.map { f ->
        Field(
            f.name,
            "",
            if (f.isStored) Field.Store.YES else Field.Store.NO,
            if (f.isIndexed && f.isAnalyzed) Field.Index.ANALYZED
            else if (f.isIndexed && !f.isAnalyzed) Field.Index.NOT_ANALYZED
            else Field.Index.NO
        )
    }.toTypedArray()

    private var values: Map<String, String?>? = null

    override fun next(): Boolean {
        return if (super.next()) {
            getColumnValues()
            values = searchSource.getValues(columnValues)
            true
        } else false
    }

    override fun getFieldValue(index: Int): String? = values?.let { it[fields[index].name()] }

    private val columnValues = mutableMapOf<String, String?>()

    private fun getColumnValues(): Map<String, String?> {
        columnValues.clear()
        for (col in cursor.columnNames) {
            val colIdx = cursor.getColumnIndex(col)
            columnValues[col] = if (cursor.isNull(colIdx)) null else cursor.getString(colIdx)
        }
        return columnValues
    }
}