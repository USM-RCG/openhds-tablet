package org.cimsbioko.utilities

import android.util.Log
import org.cimsbioko.App
import org.cimsbioko.campaign.CampaignUpdateService
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.scripting.JsConfig
import org.cimsbioko.utilities.UrlUtils.buildServerUrl
import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.net.*

object CampaignUtils {

    private val TAG = CampaignUtils::class.java.simpleName

    val campaignUrl: String?
        get() = buildServerUrl(App.instance, "/api/rest/campaign")

    fun getCampaignUrl(uuid: String): String? {
        return buildServerUrl(App.instance, "/api/rest/campaign/$uuid")
    }

    private val myCampaignsUrl: String?
        get() = buildServerUrl(App.instance, "/api/rest/mycampaigns")

    private val externalCampaignFile: File
        get() = File(IOUtils.externalDir, SetupUtils.CAMPAIGN_FILENAME)

    val downloadedCampaignFile: File
        get() = App.instance.getFileStreamPath(SetupUtils.CAMPAIGN_FILENAME)

    val campaignTempFingerprintFile: File
        get() = FileUtils.getFingerprintFile(campaignTempFile)

    val campaignTempFile: File
        get() = FileUtils.getTempFile(downloadedCampaignFile)

    val campaignHash: String?
        get() = IOUtils.loadFirstLine(downloadedCampaignFingerprintFile)

    private val downloadedCampaignFingerprintFile: File
        get() = FileUtils.getFingerprintFile(downloadedCampaignFile)

    fun updateCampaign(campaignId: String?, clearActiveModules: Boolean) {
        val newFile = campaignTempFile
        val newFingerprintFile = campaignTempFingerprintFile
        if (newFile.canRead() && newFingerprintFile.canRead()) {
            val succeeded = (newFile.renameTo(downloadedCampaignFile)
                    && newFingerprintFile.renameTo(downloadedCampaignFingerprintFile))
            if (succeeded) {
                if (clearActiveModules) {
                    ConfigUtils.clearActiveModules()
                }
                SetupUtils.campaignId = campaignId
                NavigatorConfig.instance.reload()
                SyncUtils.checkForUpdate()
            } else {
                Log.w(TAG, "failed to install campaign update: move failed")
            }
        } else {
            Log.w(TAG, "campaign update failed: new campaign files don't exist or can't be read")
        }
    }

    /**
     * Chooses the [ClassLoader] that the application should use to load its configuration from. It selects the
     * first existing, readable configuration in order: external storage, downloaded (app files), apk (compiled-in).
     *
     * @return the loader to use to load campaign configuration
     * @throws MalformedURLException
     */
    @get:Throws(MalformedURLException::class)
    private val loader: ClassLoader
        get() {
            val possible = arrayOf(externalCampaignFile, downloadedCampaignFile)
            for (file in possible) if (file.canRead()) {
                Log.i(TAG, "loading campaign from $file")
                val base = "jar:file:" + file.path + "!/"
                val urls = arrayOf(URL(base + "mobile/"), URL(base + "shared/"))
                return URLClassLoader.newInstance(urls)
            }
            Log.i(TAG, "loading internal campaign")
            return CampaignUtils::class.java.classLoader!!
        }

    fun downloadedCampaignExists() = downloadedCampaignFile.canRead()

    @Throws(MalformedURLException::class, URISyntaxException::class)
    fun loadCampaign(): JsConfig = JsConfig(loader).load()

    /**
     * Calls server web api to retrieve the device's currently assigned campaigns.
     *
     * @param token the token to use when authenticating
     * @return a [JSONObject] containing the response data.
     * @throws IOException   when url is bad, or io fails
     * @throws JSONException when construction of the response object fails
     */
    @Throws(IOException::class, JSONException::class)
    fun getCampaigns(token: String): JSONArray {
        val urlConn = URL(myCampaignsUrl).openConnection() as HttpURLConnection
        urlConn.requestMethod = "GET"
        urlConn.setRequestProperty("Content-Type", "application/json")
        urlConn.setRequestProperty("Authorization", HttpUtils.encodeBearerCreds(token))
        var `in`: BufferedInputStream? = null
        return try {
            if (urlConn.responseCode != HttpURLConnection.HTTP_OK || !urlConn.contentType.startsWith("application/json")) {
                throw IOException(String.format(
                        "unexpected response: status %s, mime type = %s",
                        urlConn.responseCode, urlConn.contentType))
            } else {
                val bout = ByteArrayOutputStream()
                `in` = BufferedInputStream(urlConn.inputStream)
                IOUtils.copy(`in`, bout)
                JSONArray(bout.toString())
            }
        } finally {
            IOUtils.close(`in`)
            urlConn.disconnect()
        }
    }

    /**
     * Calls server web api to retrieve the campaign file for the specified id.
     *
     * @param token the token to use when authenticating
     * @param uuid  the uuid of the campaign to download
     * @param targetFile the file to download the campaign to
     * @return a [File] to the downloaded campaign file.
     * @throws IOException when url is bad, or io fails
     */
    @Throws(IOException::class)
    fun downloadCampaignFile(token: String, uuid: String, targetFile: File): CampaignDownloadResult {
        val urlConn = HttpUtils.get(URL(getCampaignUrl(uuid)), null, HttpUtils.encodeBearerCreds(token), null)
        var `in`: InputStream? = null
        var out: OutputStream? = null
        return try {
            if (urlConn.responseCode != HttpURLConnection.HTTP_OK || !urlConn.contentType.startsWith("application/zip")) {
                throw RuntimeException(String.format(
                        "unexpected response: status %s, mime type = %s",
                        urlConn.responseCode, urlConn.contentType))
            } else {
                val etag = urlConn.getHeaderField("ETag")
                val campaignId = urlConn.getHeaderField(CampaignUpdateService.CIMS_CAMPAIGN_ID)
                out = FileOutputStream(targetFile)
                `in` = BufferedInputStream(urlConn.inputStream)
                IOUtils.copy(`in`, out)
                CampaignDownloadResult.Success(targetFile, campaignId, etag)
            }
        } finally {
            IOUtils.close(`in`, out)
            urlConn.disconnect()
        }
    }
}

sealed class CampaignDownloadResult {
    class Success(val downloadedFile: File, val campaign: String, val etag: String) : CampaignDownloadResult()
    class Failure(val error: String) : CampaignDownloadResult()
}