package org.cimsbioko.fragment;

import android.os.Bundle;
import androidx.preference.MultiSelectListPreference;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.cimsbioko.R;
import org.cimsbioko.navconfig.NavigatorConfig;
import org.cimsbioko.navconfig.NavigatorModule;
import org.cimsbioko.utilities.ConfigUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    EditTextPreference serverUrlPref;
    EditTextPreference syncHistoryPref;
    MultiSelectListPreference activeModulesPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.preferences);

        serverUrlPref = (EditTextPreference) findPreference(getText(R.string.server_url_key));
        serverUrlPref.setOnPreferenceChangeListener(this);
        updateSummary(serverUrlPref, serverUrlPref.getText());

        syncHistoryPref = (EditTextPreference) findPreference(getText(R.string.sync_history_retention_key));
        syncHistoryPref.setOnPreferenceChangeListener(this);
        updateSummary(syncHistoryPref, syncHistoryPref.getText());

        serverUrlPref = (EditTextPreference) findPreference(getText(R.string.server_url_key));
        serverUrlPref.setOnPreferenceChangeListener(this);
        updateSummary(serverUrlPref, serverUrlPref.getText());

        syncHistoryPref = (EditTextPreference) findPreference(getText(R.string.sync_history_retention_key));
        syncHistoryPref.setOnPreferenceChangeListener(this);
        updateSummary(syncHistoryPref, syncHistoryPref.getText());

        NavigatorConfig config = NavigatorConfig.getInstance();
        Collection<NavigatorModule> modules = config.getModules();
        Map<String, String> moduleLaunchLabels = new LinkedHashMap<>();
        for (NavigatorModule module : modules) {
            moduleLaunchLabels.put(module.getName(), module.getLaunchLabel());
        }

        activeModulesPref = (MultiSelectListPreference) findPreference(getText(R.string.active_modules_key));
        activeModulesPref.setOnPreferenceChangeListener(this);
        activeModulesPref.setEntries(moduleLaunchLabels.values().toArray(new String[]{}));
        activeModulesPref.setEntryValues(moduleLaunchLabels.keySet().toArray(new String[]{}));

        Set<String> activeModules = ConfigUtils.getMultiSelectPreference(
                getContext(), getString(R.string.active_modules_key), config.getModuleNames());
        activeModulesPref.setValues(activeModules);
        updateSummary(activeModulesPref, activeModules);
    }

    private boolean validate(Preference preference, Object newValue) {
        return preference != serverUrlPref || validateUrl((String) newValue);
    }

    private boolean validateUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.malformed_url)
                    .setMessage(R.string.malformed_url_msg)
                    .show();
            return false;
        }
        return true;
    }

    private void updateSummary(Preference preference, Object newValue) {
        if (preference instanceof EditTextPreference) {
            preference.setSummary((String) newValue);
        } else if (preference instanceof MultiSelectListPreference) {
            MultiSelectListPreference p = (MultiSelectListPreference) preference;
            if (newValue instanceof Set) {
                Set<CharSequence> selectedLabels = new LinkedHashSet<>();
                CharSequence[] valueLabels = p.getEntries();
                for (Object value : (Set<?>) newValue) {
                    int valueIndex = p.findIndexOfValue(value.toString());
                    if (valueIndex >= 0) {
                        selectedLabels.add(valueLabels[valueIndex]);
                    }
                }
                preference.setSummary(String.valueOf(selectedLabels));
            } else {
                preference.setSummary(String.valueOf(newValue));
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isValid = validate(preference, newValue);
        if (isValid) {
            updateSummary(preference, newValue);
            return true;
        }
        return false;
    }
}
