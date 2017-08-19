package org.openhds.mobile.search;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;

import java.io.File;
import java.io.IOException;

import static android.content.Context.NOTIFICATION_SERVICE;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.util.Version.LUCENE_47;
import static org.openhds.mobile.provider.OpenHDSProvider.getDatabaseHelper;
import static org.openhds.mobile.utilities.SyncUtils.close;

public class Indexer {

    public static final String INDIVIDUAL_INDEX_QUERY = String.format("select %s, 'individual' as level, %s," +
                    " ifnull(%s,'') || ' ' || ifnull(%s,'') || ' ' || ifnull(%s,'') as name," +
                    " ifnull(%s,'') || ' ' || ifnull(%s,'') || ' ' || ifnull(%s,'') as phone" +
                    " from %s", OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID, OpenHDS.Individuals.COLUMN_INDIVIDUAL_EXTID,
            OpenHDS.Individuals.COLUMN_INDIVIDUAL_FIRST_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_NAMES,
            OpenHDS.Individuals.COLUMN_INDIVIDUAL_LAST_NAME, OpenHDS.Individuals.COLUMN_INDIVIDUAL_PHONE_NUMBER,
            OpenHDS.Individuals.COLUMN_INDIVIDUAL_OTHER_PHONE_NUMBER,
            OpenHDS.Individuals.COLUMN_INDIVIDUAL_POINT_OF_CONTACT_PHONE_NUMBER,
            OpenHDS.Individuals.TABLE_NAME);

    public static final String INDIVIDUAL_UPDATE_QUERY = String.format(INDIVIDUAL_INDEX_QUERY + " where %s = ?",
            OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID);

    public static final String LOCATION_INDEX_QUERY = String.format("select %s, 'household' as level, %s, %s from %s",
            OpenHDS.Locations.COLUMN_LOCATION_UUID, OpenHDS.Locations.COLUMN_LOCATION_EXTID,
            OpenHDS.Locations.COLUMN_LOCATION_NAME, OpenHDS.Locations.TABLE_NAME);

    public static final String LOCATION_UPDATE_QUERY = String.format(LOCATION_INDEX_QUERY + " where %s = ?",
            OpenHDS.Locations.COLUMN_LOCATION_UUID);

    public static final String HIERARCHY_INDEX_QUERY = String.format("select %s, %s as level, %s, %s from %s",
            OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL,
            OpenHDS.HierarchyItems.COLUMN_HIERARCHY_EXTID, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_NAME,
            OpenHDS.HierarchyItems.TABLE_NAME);

    public static final String HIERARCHY_UPDATE_QUERY = String.format(HIERARCHY_INDEX_QUERY + " where %s = ?",
            OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID);

    private static final int NOTIFICATION_ID = 13;

    private static final String TAG = IndexingService.class.getSimpleName();

    private static Indexer instance;

    private Context ctx;

    private File indexFile;
    private IndexWriter writer;

    protected Indexer(Context ctx) {
        this.ctx = ctx;
        indexFile = new File(ctx.getFilesDir(), "search-index");
    }

    private SQLiteDatabase getDatabase() {
        return getDatabaseHelper(ctx).getReadableDatabase();
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
            Analyzer analyzer = new CustomAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(LUCENE_47, analyzer);
            config.setOpenMode(CREATE_OR_APPEND);
            writer = new IndexWriter(indexDir, config);
        }
        return writer;
    }

    public void reindexAll() {
        try {
            IndexWriter indexWriter = getWriter(false);
            try {
                indexWriter.deleteAll();
                bulkIndexHierarchy(indexWriter);
                bulkIndexLocations(indexWriter);
                bulkIndexIndividuals(indexWriter);
            } finally {
                close(indexWriter);
            }
        } catch (IOException e) {
            Log.w(TAG, "io error, indexing failed: " + e.getMessage());
        }
    }

    private void bulkIndexHierarchy(IndexWriter writer) throws IOException {
        Cursor c = getDatabase().rawQuery(HIERARCHY_INDEX_QUERY, new String[]{});
        bulkIndex(R.string.indexing_hierarchy_items,
                new HierarchyCursorDocumentSource(c, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL), writer);
    }

    public void reindexHierarchy(String uuid) throws IOException {
        IndexWriter writer = getWriter(false);
        try {
            Cursor c = getDatabase().rawQuery(HIERARCHY_UPDATE_QUERY, new String[]{uuid});
            updateIndex(new HierarchyCursorDocumentSource(c, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_LEVEL),
                    writer, OpenHDS.HierarchyItems.COLUMN_HIERARCHY_UUID);
        } finally {
            writer.commit();
        }
    }

    private void bulkIndexLocations(IndexWriter writer) throws IOException {
        Cursor c = getDatabase().rawQuery(LOCATION_INDEX_QUERY, new String[]{});
        bulkIndex(R.string.indexing_locations, new SimpleCursorDocumentSource(c), writer);
    }

    public void reindexLocation(String uuid) throws IOException {
        IndexWriter writer = getWriter(false);
        try {
            Cursor c = getDatabase().rawQuery(LOCATION_UPDATE_QUERY, new String[]{uuid});
            updateIndex(new SimpleCursorDocumentSource(c), writer, OpenHDS.Locations.COLUMN_LOCATION_UUID);
        } finally {
            writer.commit();
        }
    }

    private void bulkIndexIndividuals(IndexWriter writer) throws IOException {
        Cursor c = getDatabase().rawQuery(INDIVIDUAL_INDEX_QUERY, new String[]{});
        bulkIndex(R.string.indexing_individuals, new IndividualCursorDocumentSource(c, "name", "phone"), writer);
    }

    public void reindexIndividual(String uuid) throws IOException {
        IndexWriter writer = getWriter(false);
        try {
            Cursor c = getDatabase().rawQuery(INDIVIDUAL_UPDATE_QUERY, new String[]{uuid});
            updateIndex(new SimpleCursorDocumentSource(c), writer, OpenHDS.Individuals.COLUMN_INDIVIDUAL_UUID);
        } finally {
            writer.commit();
        }
    }

    private void bulkIndex(int label, DocumentSource source, IndexWriter writer) throws IOException {

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(ctx)
                .setSmallIcon(R.drawable.ic_progress)
                .setContentTitle(ctx.getString(R.string.updating_index))
                .setContentText(ctx.getString(label))
                .setOngoing(true);

        try {
            if (source.next()) {
                int totalCount = source.size(), processed = 0, lastNotified = -1;
                do {
                    writer.addDocument(source.getDocument());
                    processed++;
                    int percentFinished = (int) ((processed / (float) totalCount) * 100);
                    if (lastNotified != percentFinished) {
                        notificationBuilder.setProgress(totalCount, processed, false);
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());
                        lastNotified = percentFinished;
                    }
                } while (source.next());
            }
        } finally {
            notificationManager.cancel(NOTIFICATION_ID);
            source.close();
        }
    }

    private void updateIndex(DocumentSource source, IndexWriter writer, String idField) throws IOException {
        try {
            if (source.next()) {
                do {
                    Document doc = source.getDocument();
                    Term idTerm = new Term(idField, doc.get(idField));
                    writer.updateDocument(idTerm, doc);
                } while (source.next());
            }
        } finally {
            source.close();
        }
    }
}
