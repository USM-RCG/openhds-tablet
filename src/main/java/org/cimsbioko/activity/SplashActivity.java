package org.cimsbioko.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import static org.cimsbioko.utilities.SetupUtils.setupRequirementsMet;
import static org.cimsbioko.utilities.SetupUtils.startApp;

/**
 * This activity simply forwards to the actual opening activity for the application, showing an image by its style
 * instead of inflating a layout or setting any view. This ensures the image is available immediately, and the
 * actual opening activity is available as soon as it is ready.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (setupRequirementsMet(this)) {
            startApp(this);
        } else {
            Intent intent = new Intent(this, SetupChecklistActivity.class);
            startActivity(intent);
            finish();
        }
    }

}