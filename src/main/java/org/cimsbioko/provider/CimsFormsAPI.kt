package org.cimsbioko.provider

import android.net.Uri
import android.provider.BaseColumns


/**
 * This class was originally taken from the OpenDataKit project version 1.3.0. It has been updated to use the CIMS Form
 * app's content provider.
 */
object FormsProviderAPI {

    const val AUTHORITY = "org.cimsbioko.forms.provider.odk.forms"

    object FormsColumns : BaseColumns {
        @JvmField
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/forms")
        const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form"
        const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.form"

        // These are the only things needed for an insert
        const val DISPLAY_NAME = "displayName"
        const val DESCRIPTION = "description" // can be null
        const val JR_FORM_ID = "jrFormId"
        const val JR_VERSION = "jrVersion" // can be null
        const val FORM_FILE_PATH = "formFilePath"
        const val SUBMISSION_URI = "submissionUri" // can be null
        const val BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey" // can be null

        // these are generated for you (but you can insert something else if you want)
        const val DISPLAY_SUBTEXT = "displaySubtext"
        const val MD5_HASH = "md5Hash"
        const val DATE = "date"
        const val JRCACHE_FILE_PATH = "jrcacheFilePath"
        const val FORM_MEDIA_PATH = "formMediaPath"

        // this is null on fromForm, and can only be set on an update.
        const val LANGUAGE = "language"
    }
}

/**
 * Originally from ODK Collect v1.16.3, this provides a typed reference to the content provider for
 * form instances. It has been updated to use the CIMS Forms content providers instead of ODK Collect's.
 */
object InstanceProviderAPI {

    const val AUTHORITY = "org.cimsbioko.forms.provider.odk.instances"
    const val STATUS_INCOMPLETE = "incomplete"
    const val STATUS_COMPLETE = "complete"
    const val STATUS_SUBMITTED = "submitted"

    object InstanceColumns {
        @JvmField
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/instances")

        // These are the only things needed for an insert
        const val DISPLAY_NAME = "displayName"
        const val INSTANCE_FILE_PATH = "instanceFilePath"
        const val JR_FORM_ID = "jrFormId"
        const val JR_VERSION = "jrVersion"

        // these are generated for you (but you can insert something else if you want)
        const val STATUS = "status"
        const val CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete"
    }
}