package org.openhds.mobile.search;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.provider.OpenHDSProvider;

import java.io.File;
import java.io.IOException;

import static android.content.Context.NOTIFICATION_SERVICE;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.util.Version.LUCENE_36;
import static org.openhds.mobile.utilities.SyncUtils.close;

public class Indexer {

    private static final int NOTIFICATION_ID = 13;

    private static final String TAG = IndexingService.class.getSimpleName();

    private static Indexer instance;

    private Context ctx;

    private IndexWriterConfig config;
    private File indexFile;
    private IndexWriter writer;

    protected Indexer(Context ctx) {
        this.ctx = ctx;
        Analyzer analyzer = new CustomAnalyzer();
        config = new IndexWriterConfig(LUCENE_36, analyzer);
        config.setOpenMode(CREATE_OR_APPEND);
        indexFile = new File(ctx.getFilesDir(), "search-index");
    }

    public static Indexer getInstance(Context ctx) {
        if (instance == null) {
            instance = new Indexer(ctx.getApplicationContext());
        }
        return instance;
    }

    private IndexWriter getWriter(boolean reuse) throws IOException {
        if (writer != null && !reuse) {
            writer.close();
            writer = null;
        }
        if (writer == null) {
            Directory indexDir = FSDirectory.open(indexFile);
            writer = new IndexWriter(indexDir, config);
        }
        return writer;
    }

    public void bulkIndexAll() {
        try {
            SQLiteDatabase db = OpenHDSProvider.getDatabaseHelper(ctx).getReadableDatabase();
            IndexWriter indexWriter = getWriter(false);
            try {
                bulkIndexHierarchy(db, indexWriter);
                bulkIndexLocations(db, indexWriter);
                bulkIndexIndividuals(db, indexWriter);
            } finally {
                close(db, indexWriter);
            }
        } catch (IOException e) {
            Log.w(TAG, "io error, indexing failed: " + e.getMessage());
        }
    }

    private void bulkIndexHierarchy(SQLiteDatabase db, IndexWriter writer) throws IOException {
        String query = String.format("select %s, lower(%s) as level, %s, %s from %s", OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME, OpenHDS.HierarchyItems.TABLE_NAME);
        Cursor c = db.rawQuery(query, new String[]{});
        bulkIndex("hierarchy items", new SimpleCursorDocumentSource(c), writer);
    }

    private void bulkIndexLocations(SQLiteDatabase db, IndexWriter writer) throws IOException {
        String query = String.format("select %s, 'household' as level, %s, %s from %s", OpenHDS.Locations.COLUMN_LOCATION_UUID,
                OpenHDS.Locations.COLUMN_LOCATION_EXTID, OpenHDS.Locations.COLUMN_LOCATION_NAME,
                OpenHDS.Locations.TABLE_NAME);
        Cursor c = db.rawQuery(query, new String[]{});
        bulkIndex("locations", new SimpleCursorDocumentSource(c), writer);
    }

    private void bulkIndexIndividuals(SQLiteDatabase db, IndexWriter writer) throws IOException {
        String query = String.format("select %s, 'individual' as level, %s," +
                        " ifnull(%s,'') || ' ' || ifnull(%s,'') || ' ' || ifnull(%s,'') as name," +
                        " ifnull(%s,'') || ' ' || ifnull(%s,'') || ' ' || ifnull(%s,'') as phone" +
                        " from %s",
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER,
                OpenHDS.Individuals.TABLE_NAME);
        Cursor c = db.rawQuery(query, new String[]{});
        bulkIndex("individuals", new IndividualCursorDocumentSource(c, "name", "phone"), writer);
    }

    private void bulkIndex(String indexName, DocumentSource source, IndexWriter writer) throws IOException {

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);

        Notification.Builder notificationBuilder = new Notification.Builder(ctx)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Building search indices")
                .setContentText("Indexing in progress: " + indexName)
                .setOngoing(true);

        try {
            if (source.next()) {
                int totalCount = source.size(), processed = 0;
                do {
                    writer.addDocument(source.getDocument());
                    processed++;
                    if (processed % 1000 == 0) {
                        notificationBuilder.setProgress(totalCount, processed, false);
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());
                    }
                } while (source.next());
            }
        } finally {
            notificationManager.cancel(NOTIFICATION_ID);
            source.close();
        }
    }
}
