package org.cimsbioko.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.cimsbioko.R

class ManageFormsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.manage_forms)
        setContentView(R.layout.manage_forms_activity)
        findViewById<Toolbar>(R.id.manage_forms_toolbar).also { setSupportActionBar(it) }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
}