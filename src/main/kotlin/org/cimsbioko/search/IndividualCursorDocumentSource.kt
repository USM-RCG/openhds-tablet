package org.cimsbioko.search

import android.database.Cursor
import org.cimsbioko.search.Utils.extractDissimilarNames
import org.cimsbioko.search.Utils.extractUniquePhones
import org.cimsbioko.search.Utils.join

internal class IndividualCursorDocumentSource(
        c: Cursor,
        private val nameColumn: String,
        private val phoneColumn: String
) : SimpleCursorDocumentSource(c) {
    override fun getFieldValue(index: Int) = when (getFieldName(index)) {
        nameColumn -> cursor.getString(index)?.let { join(extractDissimilarNames(it), " ") }
        phoneColumn -> cursor.getString(index)?.let { join(extractUniquePhones(it), " ") }
        else -> super.getFieldValue(index)
    }
}