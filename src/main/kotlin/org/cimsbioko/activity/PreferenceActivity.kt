package org.cimsbioko.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.cimsbioko.databinding.PreferencesBinding
import org.cimsbioko.utilities.ConfigUtils.getAppFullName

class PreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getAppFullName(this)
        PreferencesBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(preferencesToolbar)
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
}