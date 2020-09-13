package org.cimsbioko.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.cimsbioko.R
import org.cimsbioko.databinding.SyncDbActivityBinding

class SyncDbActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.sync_db_title)
        SyncDbActivityBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(syncDbToolbar)
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }
}