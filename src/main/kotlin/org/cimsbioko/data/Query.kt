package org.cimsbioko.data

import android.database.Cursor
import android.net.Uri
import org.cimsbioko.App
import org.cimsbioko.data.CursorConvert.list
import org.cimsbioko.data.CursorConvert.one
import org.cimsbioko.data.WhereUtils.buildWhereStatement
import org.cimsbioko.navconfig.UsedByJSConfig

const val EQUALS = "="
const val LIKE = "LIKE"

private const val AND = "AND"
private const val WHERE_PLACEHOLDER = "?"
private const val WHERE_ALL = "1"

/**
 * Represent a database query to be performed.  Might be saved and performed in pieces by an Iterator.
 */
class Query<T : Any> internal constructor(
        private val gateway: Gateway<T>,
        private val tableUri: Uri,
        columnName: String?,
        columnValue: String?,
        private val columnOrderBy: String,
        private val operator: String = EQUALS) {

    private val columnNames: Array<String>? = columnName?.let { arrayOf(it) }
    private val columnValues: Array<String>? = columnValue?.let { arrayOf(it) }

    private fun select(): Cursor? {
        return App.instance
                .contentResolver
                .query(tableUri, null, buildWhereStatement(columnNames ?: emptyArray(), operator), columnValues, columnOrderBy)
    }

    val first: T?
        get() = one(select(), gateway.entityConverter)

    @get:UsedByJSConfig
    val list: List<T>
        get() = list(select(), gateway.entityConverter)

    // get the first result from a query as a QueryResult or null
    val firstWrapper: DataWrapper?
        get() = one(select(), gateway.wrapperConverter)

    // get all results from a query as a list of QueryResults
    val wrapperList: List<DataWrapper>
        get() = list(select(), gateway.wrapperConverter)

    fun exists(): Boolean {
        return select()?.moveToFirst() == true
    }
}

internal object WhereUtils {

    fun buildWhereStatement(columnNames: Array<String>, operator: String): String =
            if (columnNames.isNotEmpty()) {
                fun buildWhereClause(column: String, op: String) = "$column $op $WHERE_PLACEHOLDER"
                buildString {
                    columnNames.firstOrNull()?.let { first ->
                        append(buildWhereClause(first, operator))
                        columnNames.drop(1).forEach {
                            append(" $AND ")
                            append(buildWhereClause(it, operator))
                        }
                    }
                }
            } else WHERE_ALL

}