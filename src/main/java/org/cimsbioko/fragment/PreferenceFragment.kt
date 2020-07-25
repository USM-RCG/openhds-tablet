package org.cimsbioko.fragment

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.cimsbioko.R
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.utilities.ConfigUtils.getMultiSelectPreference
import java.net.MalformedURLException
import java.net.URL

class PreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private lateinit var serverUrlPref: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.preferences)

        serverUrlPref = findPreference<EditTextPreference>(getText(R.string.server_url_key))!!.apply {
            onPreferenceChangeListener = this@PreferenceFragment
            updateSummary(text)
        }

        findPreference<EditTextPreference>(getText(R.string.sync_history_retention_key))?.apply {
            onPreferenceChangeListener = this@PreferenceFragment
            updateSummary(text)
        }

        findPreference<MultiSelectListPreference>(getText(R.string.active_modules_key))?.apply {
            onPreferenceChangeListener = this@PreferenceFragment
            val config = NavigatorConfig.instance
            config.modules.associate { it.name to it.launchLabel }.also { launchLabels ->
                entries = launchLabels.values.toTypedArray()
                entryValues = launchLabels.keys.toTypedArray()
            }

            context?.also { ctx ->
                getMultiSelectPreference(ctx, getString(R.string.active_modules_key), config.moduleNames).also { activeModules ->
                    values = activeModules
                    updateSummary(activeModules)
                }
            }
        }
    }

    private fun EditTextPreference.updateSummary(newValue: Any) {
        summary = newValue as String
    }

    private fun MultiSelectListPreference.updateSummary(newValue: Any) {
        summary = when (newValue) {
            is Set<*> -> {
                entries?.let { valueLabels ->
                    newValue.mapNotNull { value ->
                        findIndexOfValue(value.toString()).let { index ->
                            if (index in valueLabels.indices) valueLabels[index] else null
                        }
                    }.toSet().toString()
                }
            }
            else -> newValue.toString()
        }
    }

    private fun Preference.updateSummary(newValue: Any) {
        when (this) {
            is EditTextPreference -> updateSummary(newValue)
            is MultiSelectListPreference -> updateSummary(newValue)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean =
            (preference !== serverUrlPref || try {
                URL(newValue as String)
                true
            } catch (e: MalformedURLException) {
                context?.also {
                    AlertDialog.Builder(it)
                            .setTitle(R.string.malformed_url)
                            .setMessage(R.string.malformed_url_msg)
                            .show()
                }
                false
            }).also { preference.updateSummary(newValue) }
}