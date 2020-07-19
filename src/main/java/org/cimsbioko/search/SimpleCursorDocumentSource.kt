package org.cimsbioko.search

import android.database.Cursor
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.Index
import org.apache.lucene.document.Field.Store

internal open class SimpleCursorDocumentSource(c: Cursor) : CursorDocumentSource(c) {

    override val fields: Array<Field>
        get() = with(mutableListOf<Field>()) {
            fun getFieldStore(index: Int) = if (index == UUID_COL_IDX || index == LEVEL_COL_IDX) Store.YES else Store.NO
            fun getFieldIndex(index: Int) = when (index) {
                UUID_COL_IDX -> Index.NO
                LEVEL_COL_IDX -> Index.NOT_ANALYZED
                else -> Index.ANALYZED
            }
            for (c in 0 until cursor.columnCount) {
                add(Field(getFieldName(c), "", getFieldStore(c), getFieldIndex(c)))
            }
            toTypedArray()
        }

    fun getFieldName(index: Int): String {
        return cursor.getColumnName(index)
    }

    override fun getFieldValue(index: Int): String? {
        return cursor.getString(index)
    }

    companion object {
        private const val UUID_COL_IDX = 0
        private const val LEVEL_COL_IDX = 1
    }
}