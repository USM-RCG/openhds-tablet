package org.cimsbioko.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.cimsbioko.R
import org.cimsbioko.databinding.ManageFormsActivityBinding

class ManageFormsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.manage_forms)
        ManageFormsActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(manageFormsToolbar)
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
}