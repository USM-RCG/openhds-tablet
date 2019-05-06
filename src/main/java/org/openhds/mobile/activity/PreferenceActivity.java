package org.openhds.mobile.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.openhds.mobile.R;

import static org.openhds.mobile.utilities.ConfigUtils.getAppFullName;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getAppFullName(this));
        setContentView(R.layout.preferences);
    }
}
