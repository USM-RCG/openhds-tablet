package org.openhds.mobile.task.parsing;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Rebuilds an entity's table from XML. It first wipes the table, then parses
 * and inserts records in batches until the entire XML file is processed.
 */
public class ParseTask extends AsyncTask<ParseRequest, String, Integer> {

    private static final int BATCH_SIZE = 100;

    public interface Listener {
        void onProgress(String progress);
        void onError(DataPage dataPage, Exception e);
        void onComplete(int progress);
    }

    private ContentResolver contentResolver;
    private ParseRequest parseRequest;
    private Listener listener;
    private int entityCount;


    public ParseTask(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(ParseRequest... parseRequests) {

        parseRequest = parseRequests[0];

        publishProgress("Wiping");
        parseRequest.getGateway().deleteAll(contentResolver);

        XmlPageParser xmlPageParser = new XmlPageParser();
        ParseHandler handler = new ParseHandler();
        xmlPageParser.setPageHandler(handler);
        xmlPageParser.setPageErrorHandler(handler);

        publishProgress("Parsing");
        InputStream input = parseRequest.getInputStream();
        try {
            xmlPageParser.parsePages(input);
        } catch (Exception e) {
            Log.e(getTagName(), e.getMessage(), e);
            return -1;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                Log.d(getTagName(), "failed to close input", e);
            }
        }

        persistBatch();

        return entityCount;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (listener != null && values != null && values.length >= 1) {
            listener.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (null != listener) {
            listener.onComplete(entityCount);
        }
    }

    @SuppressWarnings("unchecked")
    private void persistBatch() {
        List<?> entities = parseRequest.getEntityParser().getEntities();
        parseRequest.getGateway().insertMany(contentResolver, entities);
        entities.clear();
    }

    private String getTagName() {
        return getClass().getName();
    }

    private class ParseHandler implements XmlPageParser.PageHandler, XmlPageParser.PageErrorHandler {

        @Override
        public boolean handlePage(DataPage dataPage) {
            // parse the new page into an entity
            parseRequest.getEntityParser().parsePage(dataPage);
            entityCount++;

            // persist entities in batches
            if (0 == entityCount % BATCH_SIZE) {
                persistBatch();
                publishProgress(Integer.toString(entityCount));
            }

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }

        @Override
        public boolean handlePageError(DataPage dataPage, Exception e) {
            listener.onError(dataPage, e);

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }
    }
}
