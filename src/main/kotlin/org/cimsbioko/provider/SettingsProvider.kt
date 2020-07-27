package org.cimsbioko.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import org.cimsbioko.utilities.SetupUtils.campaignId
import org.cimsbioko.utilities.UrlUtils.buildServerUrl

class SettingsProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "org.cimsbioko.settings"
        private const val ODK_API_URI = 2
        private const val CURRENT_CAMPAIGN = 3
        private val sUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "odkApiUri", ODK_API_URI)
            addURI(AUTHORITY, "currentCampaign", CURRENT_CAMPAIGN)
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? =
            when (sUriMatcher.match(uri)) {
                ODK_API_URI -> MatrixCursor(arrayOf("ODK_API_URI")).apply {
                    addRow(arrayOf<Any?>(buildServerUrl(context!!, "/api/odk")))
                }
                CURRENT_CAMPAIGN -> MatrixCursor(arrayOf("CURRENT_CAMPAIGN")).apply {
                    addRow(arrayOf<Any>(campaignId!!))
                }
                else -> throw IllegalArgumentException("Unknown URI $uri")
            }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}