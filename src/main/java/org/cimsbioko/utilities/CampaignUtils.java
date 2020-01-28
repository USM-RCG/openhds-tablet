package org.cimsbioko.utilities;

import android.util.Log;
import org.cimsbioko.App;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.scripting.JsConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;

import static org.cimsbioko.App.getApp;
import static org.cimsbioko.campaign.CampaignUpdateService.CIMS_CAMPAIGN_ID;
import static org.cimsbioko.utilities.ConfigUtils.clearActiveModules;
import static org.cimsbioko.utilities.FileUtils.getFingerprintFile;
import static org.cimsbioko.utilities.FileUtils.getTempFile;
import static org.cimsbioko.utilities.HttpUtils.encodeBearerCreds;
import static org.cimsbioko.utilities.IOUtils.*;
import static org.cimsbioko.utilities.IOUtils.close;
import static org.cimsbioko.utilities.SetupUtils.CAMPAIGN_FILENAME;
import static org.cimsbioko.utilities.SetupUtils.setCampaignId;
import static org.cimsbioko.utilities.UrlUtils.buildServerUrl;

public class CampaignUtils {

    private static final String TAG = CampaignUtils.class.getSimpleName();

    public static String getCampaignUrl() {
        return buildServerUrl(App.getApp(), "/api/rest/campaign");
    }

    public static String getCampaignUrl(String uuid) {
        return buildServerUrl(App.getApp(), "/api/rest/campaign/" + uuid);
    }

    public static String getMyCampaignsUrl() {
        return buildServerUrl(App.getApp(), "/api/rest/mycampaigns");
    }

    public static File getExternalCampaignFile() {
        return new File(getExternalDir(), CAMPAIGN_FILENAME);
    }

    public static File getDownloadedCampaignFile() {
        return getApp().getFileStreamPath(CAMPAIGN_FILENAME);
    }

    public static File getCampaignTempFingerprintFile() {
        return getFingerprintFile(getCampaignTempFile());
    }

    public static File getCampaignTempFile() {
        return getTempFile(getDownloadedCampaignFile());
    }

    public static String getCampaignHash() {
        return loadFirstLine(getDownloadedCampaignFingerprintFile());
    }

    private static File getDownloadedCampaignFingerprintFile() {
        return getFingerprintFile(getDownloadedCampaignFile());
    }

    public static void updateCampaign(String campaignId, boolean clearActiveModules) {
        File newFile = getCampaignTempFile(), newFingerprintFile = getCampaignTempFingerprintFile();
        if (newFile.canRead() && newFingerprintFile.canRead()) {
            boolean succeeded = newFile.renameTo(getDownloadedCampaignFile())
                    && newFingerprintFile.renameTo(getDownloadedCampaignFingerprintFile());
            if (succeeded) {
                if (clearActiveModules) {
                    clearActiveModules();
                }
                setCampaignId(campaignId);
                NavigatorConfig.getInstance().reload();
                SyncUtils.checkForUpdate();
            } else {
                Log.w(TAG, "failed to install campaign update: move failed");
            }
        } else {
            Log.w(TAG, "campaign update failed: new campaign files don't exist or can't be read");
        }
    }

    /**
     * Chooses the {@link ClassLoader} that the application should use to load its configuration from. It selects the
     * first existing, readable configuration in order: external storage, downloaded (app files), apk (compiled-in).
     *
     * @return the loader to use to load campaign configuration
     * @throws MalformedURLException
     */
    private static ClassLoader getLoader() throws MalformedURLException {
        File[] possible = {getExternalCampaignFile(), getDownloadedCampaignFile()};
        for (File file : possible)
            if (file.canRead()) {
                Log.i(TAG, "loading campaign from " + file);
                String base = "jar:file:" + file.getPath() + "!/";
                URL[] urls = {new URL(base + "mobile/"), new URL(base + "shared/")};
                return URLClassLoader.newInstance(urls);
            }
        Log.i(TAG, "loading internal campaign");
        return CampaignUtils.class.getClassLoader();
    }

    public static boolean downloadedCampaignExists() {
        return getDownloadedCampaignFile().canRead();
    }

    public static JsConfig loadCampaign() throws MalformedURLException, URISyntaxException {
        return new JsConfig(getLoader()).load();
    }

    /**
     * Calls server web api to retrieve the device's currently assigned campaigns.
     *
     * @param token the token to use when authenticating
     * @return a {@link JSONObject} containing the response data.
     * @throws IOException   when url is bad, or io fails
     * @throws JSONException when construction of the response object fails
     */
    public static JSONArray getCampaigns(String token) throws IOException, JSONException {
        HttpURLConnection urlConn = (HttpURLConnection) new URL(getMyCampaignsUrl()).openConnection();
        urlConn.setRequestMethod("GET");
        urlConn.setRequestProperty("Content-Type", "application/json");
        urlConn.setRequestProperty("Authorization", encodeBearerCreds(token));
        BufferedInputStream in = null;
        try {
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK || !urlConn.getContentType().startsWith("application/json")) {
                throw new IOException(String.format(
                        "unexpected response: status %s, mime type = %s",
                        urlConn.getResponseCode(), urlConn.getContentType()));
            } else {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                in = new BufferedInputStream(urlConn.getInputStream());
                copy(in, bout);
                return new JSONArray(bout.toString());
            }
        } finally {
            close(in);
            urlConn.disconnect();
        }
    }

    /**
     * Calls server web api to retrieve the campaign file for the specified id.
     *
     * @param token the token to use when authenticating
     * @param uuid  the uuid of the campaign to download
     * @param targetFile the file to download the campaign to
     * @return a {@link File} to the downloaded campaign file.
     * @throws IOException when url is bad, or io fails
     */
    public static CampaignDownloadResult downloadCampaignFile(String token, String uuid, File targetFile) throws IOException {
        HttpURLConnection urlConn = HttpUtils.get(new URL(getCampaignUrl(uuid)), null, encodeBearerCreds(token), null);
        InputStream in = null;
        OutputStream out = null;
        try {
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK || !urlConn.getContentType().startsWith("application/zip")) {
                throw new RuntimeException(String.format(
                        "unexpected response: status %s, mime type = %s",
                        urlConn.getResponseCode(), urlConn.getContentType()));
            } else {
                String etag = urlConn.getHeaderField("ETag"), campaignId = urlConn.getHeaderField(CIMS_CAMPAIGN_ID);
                out = new FileOutputStream(targetFile);
                in = new BufferedInputStream(urlConn.getInputStream());
                copy(in, out);
                return new CampaignDownloadResult(targetFile, campaignId, etag);
            }
        } finally {
            close(in, out);
            urlConn.disconnect();
        }
    }
}
