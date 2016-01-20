package org.openhds.mobile.task.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.github.batkinson.jrsync.Metadata;
import com.github.batkinson.jrsync.zsync.ProgressTracker;
import com.github.batkinson.jrsync.zsync.RangeRequest;
import com.github.batkinson.jrsync.zsync.RangeRequestFactory;

import org.openhds.mobile.R;
import org.openhds.mobile.task.TaskStatus;
import org.openhds.mobile.utilities.HttpUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static com.github.batkinson.jrsync.zsync.IOUtil.buffer;
import static com.github.batkinson.jrsync.zsync.IOUtil.close;
import static com.github.batkinson.jrsync.zsync.ZSync.sync;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openhds.mobile.utilities.HttpUtils.encodeBasicCreds;
import static org.openhds.mobile.utilities.StringUtils.join;
import static org.openhds.mobile.utilities.SyncUtils.streamToFile;


public class SyncTask extends AsyncTask<SyncRequest, TaskStatus, SyncResult> {

    public static final String UNSUPPORTED_RESPONSE = "Unsupported Response";

    private final String TAG = SyncTask.class.getName();

    public interface Listener {
        void handleResult(SyncResult result);
        void handleProgress(TaskStatus status);
    }

    private Listener listener;

    public SyncTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected SyncResult doInBackground(SyncRequest... params) {

        if (params != null && params.length > 0) {

            SyncRequest sync = params[0];

            String accept = sync.mimeType;

            boolean canSync = sync.basis.exists();

            // If we have local content to sync with, also accept metadata
            if (canSync) {
                accept = join(", ", sync.mimeType, Metadata.MIME_TYPE);
            }

            HttpURLConnection c = null;
            try {
                String auth = encodeBasicCreds(sync.user, sync.pass);
                RangeRequestFactory factory = new RangeRequestFactoryImpl(sync.endpoint, sync.mimeType, auth);
                c = HttpUtils.get(sync.endpoint, accept, auth, sync.eTag);

                int status = c.getResponseCode();
                String responseType = c.getContentType();
                InputStream responseBody = c.getInputStream();
                String eTag = c.getHeaderField("ETag");

                if (status == SC_OK) {
                    if (canSync && responseType.equals(Metadata.MIME_TYPE)) {
                        incrementalSync(responseBody, sync.basis, sync.target, factory);
                        return new SyncResult(SyncResult.Type.INCREMENTAL, eTag);
                    } else if (responseType.equals(sync.mimeType)) {
                        directDownload(responseBody, sync.target);
                        return new SyncResult(SyncResult.Type.FULL, eTag);
                    }
                } else if (status == SC_NOT_MODIFIED) {
                    return new SyncResult(SyncResult.Type.NO_UPDATE, sync.eTag);
                }

                return new SyncResult(SyncResult.Type.FAILURE, UNSUPPORTED_RESPONSE, null);

            } catch (Exception e) {
                Log.w(TAG, "sync failed", e);
                return new SyncResult(SyncResult.Type.FAILURE, e.getMessage(), null);
            } finally {
                if (c != null) {
                    c.disconnect();
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(SyncResult result) {
        if (result != null)
            listener.handleResult(result);
    }

    @Override
    protected void onProgressUpdate(TaskStatus... values) {
        if (values != null && values.length >= 1)
            listener.handleProgress(values[0]);
    }

    /**
     * Reads metadata from the specified stream and closes stream.
     */
    private Metadata readMetadata(InputStream in) throws IOException, NoSuchAlgorithmException {
        DataInputStream metaIn = new DataInputStream(buffer(in));
        try {
            return Metadata.read(metaIn);
        } finally {
            close(metaIn);
        }
    }

    /**
     * Performs an incremental sync based on a local existing file.
     */
    private void incrementalSync(InputStream responseBody, File basis, File target, RangeRequestFactory factory) throws NoSuchAlgorithmException, IOException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(basis, "r");
            publishProgress(new TaskStatus(R.string.sync_state_metadata));
            Metadata metadata = readMetadata(responseBody);
            publishProgress(new TaskStatus(R.string.sync_state_syncing));
            sync(metadata, file, target, factory, new SyncTracker());
            publishProgress(new TaskStatus(R.string.sync_state_synced));
        } finally {
            close(file);
        }
    }

    class SyncTracker implements ProgressTracker {

        private Stage stage;
        private int percent;

        @Override
        public void onProgress(Stage stage, int percent) {
            if (this.stage != stage || percent != this.percent) {

                // Setting these guarantees only publishing unique updates
                this.stage = stage;
                this.percent = percent;

                switch (stage) {
                    case SEARCH:
                        publishProgress(new TaskStatus(R.string.sync_state_compare, percent));
                        break;
                    case BUILD:
                        publishProgress(new TaskStatus(R.string.sync_state_build, percent));
                        break;
                }
            }
        }
    }

    /**
     * Directly streams the given stream to the target file.
     */
    private void directDownload(InputStream responseBody, File target) throws IOException {
        publishProgress(new TaskStatus(R.string.sync_state_download));
        streamToFile(buffer(responseBody), target);
        publishProgress(new TaskStatus(R.string.sync_state_synced));
    }

    /**
     * Used to create range requests for fetching remote file data.
     */
    private static class RangeRequestFactoryImpl implements RangeRequestFactory {

        private final URL endpoint;
        private final String mimeType;
        private final String auth;

        public RangeRequestFactoryImpl(URL endpoint, String mimeType, String auth) {
            this.endpoint = endpoint;
            this.mimeType = mimeType;
            this.auth = auth;
        }

        @Override
        public RangeRequest create() throws IOException {
            return new RangeRequestImpl(HttpUtils.get(endpoint, mimeType, auth, null));
        }
    }


    /**
     * A wrapper around android's {@link HttpURLConnection} to make them usable
     * with sync range requests.
     */
    private static class RangeRequestImpl implements RangeRequest {

        private final HttpURLConnection c;

        RangeRequestImpl(HttpURLConnection c) {
            this.c = c;
        }

        @Override
        public int getResponseCode() throws IOException {
            return c.getResponseCode();
        }

        @Override
        public String getContentType() {
            return c.getContentType();
        }

        @Override
        public String getHeader(String name) {
            return c.getHeaderField(name);
        }

        @Override
        public void setHeader(String name, String value) {
            c.setRequestProperty(name, value);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return c.getInputStream();
        }

        @Override
        public void close() throws IOException {
            c.disconnect();
        }
    }
}