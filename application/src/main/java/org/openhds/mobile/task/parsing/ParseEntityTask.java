package org.openhds.mobile.task.parsing;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Carry out a ParseEntityTaskRequest.
 *
 * Read an input stream, parse it into entities, and
 * save entities to the database in batches.
 *
 * BSH
 */
public class ParseEntityTask extends AsyncTask<ParseEntityTaskRequest, Integer, Integer> {

    private static final String TAG = ParseEntityTask.class.getName();

    private static final int BATCH_SIZE = 100;

    private ContentResolver contentResolver;
    private ParseEntityTaskRequest parseEntityTaskRequest;
    private ProgressListener progressListener;

    private int entityCount;

    public ParseEntityTask(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    protected void onPreExecute () {
        entityCount = 0;
    }

    @Override
    protected Integer doInBackground(ParseEntityTaskRequest... parseEntityTaskRequests) {
        parseEntityTaskRequest = parseEntityTaskRequests[0];

        // set up a page parser
        XmlPageParser xmlPageParser = new XmlPageParser();
        xmlPageParser.setPageHandler(new EntityPageHandler());
        xmlPageParser.setPageErrorHandler(new EntityErrorHandler());

        // pass input stream to page parser
        InputStream input = parseEntityTaskRequest.getInputStream();
        try {
            xmlPageParser.parsePages(input);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            return -1;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                Log.d(TAG, "failed to close input", e);
            }
        }

        persistBatch();
        return entityCount;
    }

    @Override
    protected void onProgressUpdate (Integer... values) {
        int progress = values[0];
        if (null != progressListener) {
            progressListener.onProgressReport(progress);
        }
    }

    @Override
    protected void onPostExecute (Integer result) {
        if (null != progressListener) {
            progressListener.onComplete(entityCount);
        }
    }

    public interface ProgressListener {
        void onProgressReport(int progress);
        void onError(DataPage dataPage, Exception e);
        void onComplete(int progress);
    }

    private void persistBatch() {
        List<?> entities = parseEntityTaskRequest.getEntityParser().getEntities();
        parseEntityTaskRequest.getGateway().insertMany(contentResolver, entities);
        entities.clear();
    }

    private class EntityPageHandler implements XmlPageParser.PageHandler {
        @Override
        public boolean handlePage(DataPage dataPage) {
            // parse the new page into an entity
            parseEntityTaskRequest.getEntityParser().parsePage(dataPage);
            entityCount++;

            // persist entities in batches
            if (0 == entityCount % BATCH_SIZE) {
                persistBatch();
                publishProgress(entityCount);
            }

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }
    }

    private class EntityErrorHandler implements XmlPageParser.PageErrorHandler {
        @Override
        public boolean handlePageError(DataPage dataPage, Exception e) {
            progressListener.onError(dataPage, e);

            // stop parsing if the user cancelled the task
            return !isCancelled();
        }
    }
}
