package org.openhds.mobile.task.parsing;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;

import org.openhds.mobile.R;
import org.openhds.mobile.task.TaskStatus;

import java.util.List;

import static com.github.batkinson.jrsync.zsync.IOUtil.close;

/**
 * Rebuilds an entity's table from XML. It first wipes the table, then parses
 * and inserts records in batches until the entire XML file is processed.
 */
public class ParseTask extends AsyncTask<ParseRequest, TaskStatus, Integer> {

    private static final int BATCH_SIZE = 100;

    public interface Listener {
        void onProgress(TaskStatus progress);
        void onError(DataPage dataPage, Exception e);
        void onComplete(int progress);
    }

    private ContentResolver contentResolver;
    private ParseRequest parseRequest;
    private Listener listener;
    private ParserInputStream input;
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

        publishProgress(new TaskStatus(R.string.sync_state_wiping));
        parseRequest.getGateway().deleteAll(contentResolver);

        XmlPageParser xmlPageParser = new XmlPageParser();
        ParseHandler handler = new ParseHandler();
        xmlPageParser.setPageHandler(handler);
        xmlPageParser.setPageErrorHandler(handler);

        try {
            input = parseRequest.getInputStream();
            publishProgress(new TaskStatus(R.string.sync_state_parse, 0));
            xmlPageParser.parsePages(input);
        } catch (Exception e) {
            Log.e(getTagName(), e.getMessage(), e);
            return -1;
        } finally {
            close(input);
        }

        persistBatch();

        return entityCount;
    }

    @Override
    protected void onProgressUpdate(TaskStatus... values) {
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
        try {
            parseRequest.getGateway().insertMany(contentResolver, entities);
        } finally {
            entities.clear();
        }
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
                publishProgress(new TaskStatus(R.string.sync_state_parse, input.getPercentRead()));
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
