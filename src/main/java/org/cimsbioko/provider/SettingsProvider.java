package org.cimsbioko.provider;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.cimsbioko.utilities.SetupUtils.getCampaignId;
import static org.cimsbioko.utilities.UrlUtils.buildServerUrl;

public class SettingsProvider extends android.content.ContentProvider {

    private static final UriMatcher sUriMatcher;

    private static final String AUTHORITY = "org.cimsbioko.settings";

    private static final int ODK_API_URI = 2;
    private static final int CURRENT_CAMPAIGN = 3;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "odkApiUri", ODK_API_URI);
        sUriMatcher.addURI(AUTHORITY, "currentCampaign", CURRENT_CAMPAIGN);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (sUriMatcher.match(uri) == ODK_API_URI) {
            MatrixCursor c = new MatrixCursor(new String[]{"ODK_API_URI"});
            c.addRow(new Object[]{buildServerUrl(getContext(), "/api/odk")});
            return c;
        } else if (sUriMatcher.match(uri) == CURRENT_CAMPAIGN) {
            MatrixCursor c = new MatrixCursor(new String[]{"CURRENT_CAMPAIGN"});
            c.addRow(new Object[]{getCampaignId()});
            return c;
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
