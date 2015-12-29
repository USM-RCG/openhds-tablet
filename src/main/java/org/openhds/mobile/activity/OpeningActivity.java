package org.openhds.mobile.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.FieldWorkerLoginFragment;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.fragment.SupervisorLoginFragment;
import org.openhds.mobile.model.form.FormInstance;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.OdkCollectHelper;

import static org.openhds.mobile.utilities.ConfigUtils.getAppFullName;

import java.util.List;

public class OpeningActivity extends Activity {

    public static final String USERNAME_KEY = "usernameKey";
    public static final String PASSWORD_KEY = "passwordKey";

    private FrameLayout loginPrefContainer;

    @Override
    protected void onResume() {
        super.onResume();
        List<FormInstance> allFormInstances = OdkCollectHelper.getAllFormInstances(getContentResolver());
        if (null != allFormInstances) {
            EncryptionHelper.encryptFiles(FormInstance.toListOfFiles(allFormInstances), this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getAppFullName(this));

        setContentView(R.layout.opening_activity);

        loginPrefContainer = (FrameLayout) findViewById(R.id.login_pref_container);


        if (null != savedInstanceState) {
            return;
        }

        getFragmentManager()
                .beginTransaction()
                .add(R.id.login_pref_container, new LoginPreferenceFragment())
                .add(R.id.field_worker_login_container, new FieldWorkerLoginFragment())
                .add(R.id.supervisor_login_container, new SupervisorLoginFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isShowingPreferences = View.VISIBLE == loginPrefContainer
                .getVisibility();
        if (isShowingPreferences) {
            loginPrefContainer.setVisibility(View.GONE);
        } else {
            loginPrefContainer.setVisibility(View.VISIBLE);
        }
        return true;
    }
}
