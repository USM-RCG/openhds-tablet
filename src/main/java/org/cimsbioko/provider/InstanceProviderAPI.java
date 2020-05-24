/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cimsbioko.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Originally from ODK Collect v1.16.3, this provides a typed reference to the content provider for
 * form instances. It has been updated to use the CIMS Forms content providers instead of ODK Collect's.
 */
public final class InstanceProviderAPI {

    public static final String AUTHORITY = "org.cimsbioko.forms.provider.odk.instances";

    // This class cannot be instantiated
    private InstanceProviderAPI() {
    }

    // status for instances
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_SUBMITTED = "submitted";

    public static final class InstanceColumns implements BaseColumns {

        // This class cannot be instantiated
        private InstanceColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/instances");

        // These are the only things needed for an insert
        public static final String DISPLAY_NAME = "displayName";
        public static final String INSTANCE_FILE_PATH = "instanceFilePath";
        public static final String JR_FORM_ID = "jrFormId";
        public static final String JR_VERSION = "jrVersion";

        // these are generated for you (but you can insert something else if you want)
        public static final String STATUS = "status";
        public static final String CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete";
    }
}