package org.openhds.mobile.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

import org.openhds.mobile.R;

import java.net.MalformedURLException;
import java.net.URL;

import static org.openhds.mobile.utilities.ConfigUtils.getAppFullName;

public class PreferenceActivity extends android.preference.PreferenceActivity implements Preference.OnPreferenceChangeListener {

    EditTextPreference serverUrlPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(getAppFullName(this));

        addPreferencesFromResource(R.xml.preferences);

        serverUrlPref = (EditTextPreference) findPreference(getText(R.string.openhds_server_url_key));
        serverUrlPref.setOnPreferenceChangeListener(this);
        updateSummary(serverUrlPref, serverUrlPref.getText());
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
        }
    }
}
