package org.openhds.mobile.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;

import org.openhds.mobile.R;
import org.openhds.mobile.navconfig.NavigatorConfig;
import org.openhds.mobile.utilities.ConfigUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static org.openhds.mobile.utilities.ConfigUtils.getAppFullName;

public class PreferenceActivity extends android.preference.PreferenceActivity implements Preference.OnPreferenceChangeListener {

    EditTextPreference serverUrlPref;
    EditTextPreference syncHistoryPref;
    MultiSelectListPreference activeModulesPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(getAppFullName(this));

        addPreferencesFromResource(R.xml.preferences);

        serverUrlPref = (EditTextPreference) findPreference(getText(R.string.openhds_server_url_key));
        serverUrlPref.setOnPreferenceChangeListener(this);
        updateSummary(serverUrlPref, serverUrlPref.getText());

        syncHistoryPref = (EditTextPreference) findPreference(getText(R.string.sync_history_retention_key));
        syncHistoryPref.setOnPreferenceChangeListener(this);
        updateSummary(syncHistoryPref, syncHistoryPref.getText());

        NavigatorConfig config = NavigatorConfig.getInstance();
        Set<String> moduleNames = config.getModuleNames();
        Set<String> activeModules = ConfigUtils.getMultiSelectPreference(
                this, getString(R.string.active_modules_key), moduleNames);
        String[] moduleNameArray = moduleNames.toArray(new String[]{});

        activeModulesPref = (MultiSelectListPreference) findPreference(getText(R.string.active_modules_key));
        activeModulesPref.setOnPreferenceChangeListener(this);
        activeModulesPref.setEntries(moduleNameArray);
        activeModulesPref.setEntryValues(moduleNameArray);
        activeModulesPref.setValues(activeModules);
        updateSummary(activeModulesPref, activeModulesPref.getValues());
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

    private boolean validate(Preference preference, Object newValue) {
        if (preference == serverUrlPref) {
            return validateUrl((String)newValue);
        }
        return true;
    }

    private boolean validateUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            new AlertDialog.Builder(PreferenceActivity.this)
                    .setTitle(R.string.malformed_url)
                    .setMessage(R.string.malformed_url_msg)
                    .show();
            return false;
        }
        return true;
    }

    private void updateSummary(Preference preference, Object newValue) {
        if (preference instanceof EditTextPreference) {
            preference.setSummary((String)newValue);
        } else if (preference instanceof MultiSelectListPreference) {
            preference.setSummary(String.valueOf(newValue));
        }
    }
}
