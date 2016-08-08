package org.openhds.mobile.search;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.provider.OpenHDSProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.util.Version.LUCENE_36;

public class IndexingService extends IntentService {

    private static final int NOTIFICATION_ID = 13;

    private static final String TAG = IndexingService.class.getSimpleName();

    public IndexingService() {
        super("indexer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Context ctx = getApplicationContext();

        Analyzer analyzer = new CustomAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_36, analyzer);
        config.setRAMBufferSizeMB(5.0f);
        config.setOpenMode(CREATE_OR_APPEND);
        File indexFile = new File(ctx.getFilesDir(), "search-index");

        try {

            Directory indexDir = FSDirectory.open(indexFile);
            IndexWriter indexWriter = new IndexWriter(indexDir, config);
            SQLiteDatabase db = OpenHDSProvider.getDatabaseHelper(ctx).getReadableDatabase();

            try {
                buildHierarchyIndex(db, indexWriter);
                buildLocationIndex(db, indexWriter);
                buildIndividualIndex(db, indexWriter);
            } finally {
                indexWriter.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "io error, indexing failed: " + e.getMessage());
        }
    }

    private void buildHierarchyIndex(SQLiteDatabase db, IndexWriter writer) throws IOException {

        String query = String.format("select %s, lower(%s) as level, %s, %s from %s", OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID,
                OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME, OpenHDS.HierarchyItems.TABLE_NAME);
        Cursor c = db.rawQuery(query, new String[]{});
        try {
            buildIndex("hierarchy items", c, writer);
        } finally {
            c.close();
        }

    }

    private void buildLocationIndex(SQLiteDatabase db, IndexWriter writer) throws IOException {

        String query = String.format("select %s, 'household' as level, %s, %s from %s", OpenHDS.Locations.COLUMN_LOCATION_UUID,
                OpenHDS.Locations.COLUMN_LOCATION_EXTID, OpenHDS.Locations.COLUMN_LOCATION_NAME,
                OpenHDS.Locations.TABLE_NAME);
        Cursor c = db.rawQuery(query, new String[]{});
        try {
            buildIndex("locations", c, writer);
        } finally {
            c.close();
        }

    }

    private void buildIndividualIndex(SQLiteDatabase db, IndexWriter writer) throws IOException {

        // lowercase here converts from server level names
        String query = String.format("select %s, 'individual' as level, %s, %s, %s, %s from %s",
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME,
                OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES, OpenHDS.Individuals.TABLE_NAME);
        Cursor c = db.rawQuery(query, new String[]{});
        try {
            buildIndex("individuals", c, writer);
        } finally {
            c.close();
        }

    }

    private void buildIndex(String indexName, Cursor cursor, IndexWriter writer) throws IOException {

        Context ctx = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);

        try {
            if (cursor.moveToFirst()) {

                int totalCount = cursor.getCount(), processed = 0, columns = cursor.getColumnCount();

                // Performance: reuse a single document to reduce in-loop memory allocations
                Document doc = new Document();
                Field[] fields = new Field[columns];
                for (int c = 0; c < columns; c++) {
                    boolean dataCol = c <= 1;  // first two columns are assumed to be uuid and level
                    String columnName = cursor.getColumnName(c);
                    fields[c] = new Field(columnName, "",
                            dataCol ? Field.Store.YES : Field.Store.NO,
                            dataCol ? Field.Index.NO : Field.Index.ANALYZED);
                    doc.add(fields[c]);
                }

                List<Field> nullFields = new ArrayList<>(columns);

                do {
                    // Set field values or remove if null
                    for (int c = 0; c < columns; c++) {
                        Field field = fields[c];
                        String value = cursor.getString(c);
                        if (value != null) {
                            field.setValue(value);
                        } else {
                            doc.removeField(field.name());
                            nullFields.add(field);
                        }
                    }

                    // Add resulting document
                    writer.addDocument(doc);

                    // Re-add fields that were removed due to missing values
                    if (!nullFields.isEmpty()) {
                        for (Field f : nullFields) {
                            doc.add(f);
                        }
                        nullFields.clear();
                    }

                    // Update progress in status notification
                    processed++;
                    if (processed % 1000 == 0) {
                        notificationManager.notify(NOTIFICATION_ID, new Notification.Builder(ctx)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Building search indices")
                                .setContentText("Indexing in progress: " + indexName)
                                .setProgress(totalCount, processed, false)
                                .setOngoing(true)
                                .getNotification());
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}