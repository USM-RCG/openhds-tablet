package org.openhds.mobile.task.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.github.batkinson.jrsync.Metadata;
import com.github.batkinson.jrsync.zsync.RangeRequest;
import com.github.batkinson.jrsync.zsync.RangeRequestFactory;

import org.openhds.mobile.utilities.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import static com.github.batkinson.jrsync.zsync.IOUtil.buffer;
import static com.github.batkinson.jrsync.zsync.ZSync.readMetadata;
import static com.github.batkinson.jrsync.zsync.ZSync.sync;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.openhds.mobile.utilities.HttpUtils.encodeBasicCreds;
import static org.openhds.mobile.utilities.StringUtils.join;
import static org.openhds.mobile.utilities.SyncUtils.streamToFile;


public class SyncTask extends AsyncTask<Sync, Void, Result> {

    public static final String UNSUPPORTED_RESPONSE = "Unsupported Response";

    private final String TAG = SyncTask.class.getName();

    @Override
    protected Result doInBackground(Sync... params) {

        if (params != null && params.length > 0) {

            Sync sync = params[0];

            String accept = sync.contentType;

            boolean canSync = sync.basis.exists();

            // If we have local content to sync with, also accept metadata
            if (canSync) {
                accept = join(", ", sync.contentType, Metadata.MIME_TYPE);
            }

            HttpURLConnection c = null;
            try {
                String auth = encodeBasicCreds(sync.user, sync.pass);
                RangeRequestFactory factory = new RangeRequestFactoryImpl(sync.endpoint, sync.contentType, auth);
                c = HttpUtils.get(sync.endpoint, accept, auth, sync.eTag);

                int status = c.getResponseCode();
                String responseType = c.getContentType();
                InputStream responseBody = c.getInputStream();
                String eTag = c.getHeaderField("ETag");

                if (status == SC_OK) {
                    if (canSync && responseType == Metadata.MIME_TYPE) {
                        incrementalSync(responseBody, sync.basis, sync.target, factory);
                        return new Result(sync.name, Result.Type.INCREMENTAL, eTag);
                    } else if (responseType == sync.contentType) {
                        directDownload(responseBody, sync.target);
                        return new Result(sync.name, Result.Type.FULL, eTag);
                    }
                } else if (status == SC_NOT_MODIFIED) {
                    return new Result(sync.name, Result.Type.NO_UPDATE, sync.eTag);
                }

                return new Result(sync.name, Result.Type.FAILURE, UNSUPPORTED_RESPONSE, null);

            } catch (Exception e) {
                Log.w(TAG, "sync failed", e);
                return new Result(sync.name, Result.Type.FAILURE, e.getMessage(), null);
            } finally {
                if (c != null) {
                    c.disconnect();
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
    }

    /**
     * Performs an incremental sync based on a local existing file.
     */
    private void incrementalSync(InputStream responseBody, File basis, File target, RangeRequestFactory factory) throws NoSuchAlgorithmException, IOException {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(basis, "r");
            sync(readMetadata(responseBody), file, target, factory);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    Log.w(TAG, "failed to close basis file", e);
                }
            }
        }
    }

    /**
     * Directly streams the given stream to the target file.
     */
    private void directDownload(InputStream responseBody, File target) throws IOException {
        streamToFile(buffer(responseBody), target);
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
        public int getResposeCode() throws IOException {
            return c.getResponseCode();
        }

        @Override
        public String getContentType() {
            return c.getContentType();
        }

        @Override
        public String getHeader(String name) {
            return c.getRequestProperty(name);
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

class Sync {

    final String name;
    final URL endpoint;
    final String user;
    final String pass;
    final File basis;
    final File target;
    final String contentType;
    final String eTag;

    Sync(String name, URL endpoint, String user, String pass, File basis, File target, String mimeType, String eTag) {
        this.name = name;
        this.user = user;
        this.pass = pass;
        this.endpoint = endpoint;
        this.basis = basis;
        this.target = target;
        contentType = mimeType;
        this.eTag = eTag;
    }
}

class Result {

    enum Type {
        FULL, INCREMENTAL, NO_UPDATE, FAILURE
    }

    final String name;
    final Type type;
    String message;
    String eTag;

    Result(String name, Type type, String eTag) {
        this(name, type, null, eTag);
    }

    Result(String name, Type type, String message, String eTag) {
        this.name = name;
        this.type = type;
        this.message = message;
        this.eTag = eTag;
    }
}