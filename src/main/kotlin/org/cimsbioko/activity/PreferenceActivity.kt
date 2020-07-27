package org.cimsbioko.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.cimsbioko.R
import org.cimsbioko.utilities.ConfigUtils.getAppFullName

class PreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getAppFullName(this)
        setContentView(R.layout.preferences)
        findViewById<Toolbar>(R.id.preferences_toolbar).also { setSupportActionBar(it) }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
}