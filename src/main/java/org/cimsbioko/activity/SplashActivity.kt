package org.cimsbioko.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.cimsbioko.utilities.SetupUtils.createNotificationChannels
import org.cimsbioko.utilities.SetupUtils.setupRequirementsMet
import org.cimsbioko.utilities.SetupUtils.startApp

/**
 * This activity simply forwards to the actual opening activity for the application, showing an image by its style
 * instead of inflating a layout or setting any view. This ensures the image is available immediately, and the
 * actual opening activity is available as soon as it is ready.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels(this)
        if (setupRequirementsMet(this)) {
            startApp(this)
        } else {
            Intent(this, SetupChecklistActivity::class.java).also { startActivity(it) }
            finish()
        }
    }
}