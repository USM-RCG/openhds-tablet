package org.cimsbioko.utilities;

import android.util.Log;
import org.cimsbioko.App;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.scripting.JsConfig;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.cimsbioko.App.getApp;
import static org.cimsbioko.utilities.FileUtils.getFingerprintFile;
import static org.cimsbioko.utilities.FileUtils.getTempFile;
import static org.cimsbioko.utilities.IOUtils.getExternalDir;
import static org.cimsbioko.utilities.IOUtils.loadFirstLine;
import static org.cimsbioko.utilities.SetupUtils.CAMPAIGN_FILENAME;
import static org.cimsbioko.utilities.UrlUtils.buildServerUrl;

public class CampaignUtils {

    private static final String TAG = CampaignUtils.class.getSimpleName();

    public static String getCampaignUrl() {
        return buildServerUrl(App.getApp(), "/api/rest/campaign");
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

    public static void updateCampaign() {
        File newFile = getCampaignTempFile(), newFingerprintFile = getCampaignTempFingerprintFile();
        if (newFile.canRead() && newFingerprintFile.canRead()) {
            boolean succeeded = newFile.renameTo(getDownloadedCampaignFile())
                    && newFingerprintFile.renameTo(getDownloadedCampaignFingerprintFile());
            if (succeeded) {
                NavigatorConfig.getInstance().reload();
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
}
