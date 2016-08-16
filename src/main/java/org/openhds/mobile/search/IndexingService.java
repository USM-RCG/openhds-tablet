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
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.provider.OpenHDSProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        buildIndex("hierarchy items", new SimpleDocumentSource(c), writer);
    }

    private void buildLocationIndex(SQLiteDatabase db, IndexWriter writer) throws IOException {
        String query = String.format("select %s, 'household' as level, %s, %s from %s", OpenHDS.Locations.COLUMN_LOCATION_UUID,
                OpenHDS.Locations.COLUMN_LOCATION_EXTID, OpenHDS.Locations.COLUMN_LOCATION_NAME,
                OpenHDS.Locations.TABLE_NAME);
        Cursor c = db.rawQuery(query, new String[]{});
        buildIndex("locations", new SimpleDocumentSource(c), writer);
    }

    private void buildIndividualIndex(SQLiteDatabase db, IndexWriter writer) throws IOException {
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
        buildIndex("individuals", new IndividualDocumentSource(c, "name", "phone"), writer);
    }

    private void buildIndex(String indexName, DocumentSource source, IndexWriter writer) throws IOException {

        Context ctx = getApplicationContext();
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

interface DocumentSource {

    boolean next();

    int size();

    Document getDocument();

    void close();
}

abstract class CursorDocumentSource implements DocumentSource {

    protected final Document document;  // shared instance to reduce memory allocations during indexing
    protected final Cursor cursor;
    protected Field[] fields;
    private List<Field> nullFields;

    CursorDocumentSource(Cursor c) {
        document = new Document();
        cursor = c;
    }

    public abstract Field[] getFields();

    public abstract String getFieldValue(int index);

    @Override
    public Document getDocument() {

        // add field back to document before we start next
        if (!nullFields.isEmpty()) {
            for (Field nullField : nullFields) {
                document.add(nullField);
            }
            nullFields.clear();
        }

        // manipulate document to match next cursor record, remove fields for null values
        for (int f = 0; f < fields.length; f++) {
            Field field = fields[f];
            String value = getFieldValue(f);
            if (value != null) {
                field.setValue(value);
            } else {
                document.removeField(field.name());
                nullFields.add(field);
            }
        }

        return document;
    }

    @Override
    public boolean next() {
        boolean isFirst = cursor.isBeforeFirst(), hasNext = cursor.moveToNext();
        if (isFirst && hasNext) {
            fields = getFields();
            nullFields = new ArrayList<>(fields.length);
            for (Field field : fields) {
                document.add(field);
            }
        }
        return hasNext;
    }

    @Override
    public int size() {
        return cursor.getCount();
    }

    @Override
    public void close() {
        cursor.close();
    }
}

class SimpleDocumentSource extends CursorDocumentSource {

    SimpleDocumentSource(Cursor c) {
        super(c);
    }

    @Override
    public Field[] getFields() {
        int columns = cursor.getColumnCount();
        Field[] fields = new Field[columns];
        for (int c = 0; c < columns; c++) {
            boolean dataCol = c <= 1;  // first two columns are assumed to be uuid and level
            String columnName = cursor.getColumnName(c);
            fields[c] = new Field(columnName, "",
                    dataCol ? Field.Store.YES : Field.Store.NO,
                    dataCol ? Field.Index.NO : Field.Index.ANALYZED);
            document.add(fields[c]);
        }
        return fields;
    }

    @Override
    public String getFieldValue(int index) {
        return cursor.getString(index);
    }
}

class IndividualDocumentSource extends SimpleDocumentSource {

    private static final float MAX_SIMILARITY = 0.99f;
    private static final JaroWinklerDistance jwd = new JaroWinklerDistance();

    private String nameColumn, phoneColumn;

    IndividualDocumentSource(Cursor c, String nameCol, String phoneCol) {
        super(c);
        nameColumn = nameCol;
        phoneColumn = phoneCol;
    }

    @Override
    public String getFieldValue(int index) {
        String fieldName = fields[index].name();
        if (nameColumn.equals(fieldName)) {
            return getNameValue(index);
        } else if (phoneColumn.equals(fieldName)) {
            return getPhoneValue(index);
        } else {
            return super.getFieldValue(index);
        }
    }

    private String getPhoneValue(int index) {
        String rawValue = cursor.getString(index);
        if (rawValue != null) {
            return join(extractUniquePhones(rawValue), " ");
        } else {
            return rawValue;
        }
    }

    private Set<String> extractUniquePhones(String phoneValue) {
        Set<String> phones = new HashSet<>();
        for (String phone : phoneValue.trim().split("\\s+")) {
            phones.add(phone);
        }
        return phones;
    }

    private String getNameValue(int index) {
        String rawValue = cursor.getString(index);
        if (rawValue != null) {
            return join(extractDissimilarNames(rawValue), " ");
        } else {
            return rawValue;
        }
    }

    private Set<String> extractDissimilarNames(String nameValue) {
        Set<String> names = new HashSet<>();
        for (String name : nameValue.trim().toLowerCase().split("\\s+")) {
            name = name.replaceAll("\\W+", "");
            if (!containsSimilar(names, name)) {
                names.add(name);
            }
        }
        return names;
    }

    private String join(Set<String> values, String separator) {
        StringBuilder buf = new StringBuilder();
        for (String name : values) {
            if (buf.length() > 0) {
                buf.append(separator);
            }
            buf.append(name);
        }
        return buf.toString();
    }

    private boolean containsSimilar(Set<String> values, String value) {
        for (String v : values) {
            if (jwd.getDistance(v, value) > MAX_SIMILARITY)
                return true;
        }
        return false;
    }
}