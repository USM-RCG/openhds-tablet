package org.openhds.mobile.activity;

import static org.openhds.mobile.utilities.ConfigUtils.getPreferenceString;
import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericButton;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.openhds.mobile.InstanceProviderAPI;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.LoginPreferenceFragment;
import org.openhds.mobile.task.HttpTask.RequestContext;
import org.openhds.mobile.task.SyncEntitiesTask;
import org.openhds.mobile.task.SyncFieldworkersTask;
import org.openhds.mobile.utilities.EncryptionHelper;
import org.openhds.mobile.utilities.SyncDatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SupervisorMainActivity extends Activity implements OnClickListener {

	private FrameLayout prefContainer;
	private LinearLayout supervisorOptionsList;
	private SyncDatabaseHelper syncDatabaseHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.supervisor_main);

		prefContainer = (FrameLayout) findViewById(R.id.login_pref_container);
		supervisorOptionsList = (LinearLayout) findViewById(R.id.supervisor_activity_options);
		syncDatabaseHelper = new SyncDatabaseHelper(this);

		makeNewGenericButton(this,
				getResourceString(this, R.string.sync_database_description),
				getResourceString(this, R.string.sync_database_name),
				getResourceString(this, R.string.sync_database_name), this,
				supervisorOptionsList);

		makeNewGenericButton(
				this,
				getResourceString(this, R.string.sync_field_worker_description),
				getResourceString(this, R.string.sync_field_worker_name),
				getResourceString(this, R.string.sync_field_worker_name), this,
				supervisorOptionsList);

		makeNewGenericButton(
				this,
				getResourceString(this,
						R.string.send_finalized_forms_description),
				getResourceString(this, R.string.send_finalized_forms_name),
				getResourceString(this, R.string.send_finalized_forms_name),
				this, supervisorOptionsList);

		if (null != savedInstanceState) {
			return;
		}

		getFragmentManager().beginTransaction()
				.add(R.id.login_pref_container, new LoginPreferenceFragment())
				.commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.login_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Defining what happens when a main menu item is selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean isShowingPreferences = View.VISIBLE == prefContainer
				.getVisibility();
		if (isShowingPreferences) {
			prefContainer.setVisibility(View.GONE);
		} else {
			prefContainer.setVisibility(View.VISIBLE);
		}
		return true;
	}

	public void onClick(View v) {
		String tag = (String) v.getTag();
		if (tag.equals(getResourceString(this, R.string.sync_database_name))) {
			syncDatabase();
		} else if (tag.equals(getResourceString(this,
				R.string.sync_field_worker_name))) {
			syncFieldWorkers();
		} else if (tag.equals((getResourceString(this,
				R.string.send_finalized_forms_name)))) {



			String finalizedFormFilePath;

			Cursor cursor = getContentResolver()
					.query(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
							new String[] {
									InstanceProviderAPI.InstanceColumns.STATUS,
									InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH },
							InstanceProviderAPI.InstanceColumns.STATUS + "=?",
							new String[] { InstanceProviderAPI.STATUS_COMPLETE },
							null);

			while (cursor.moveToNext()) {
				finalizedFormFilePath = cursor
						.getString(cursor
								.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

				File file = new File(finalizedFormFilePath);

				try {
					EncryptionHelper eh = new EncryptionHelper(this);
					eh.decryptFile(file);
				} catch (NoSuchAlgorithmException | InvalidKeyException
						| IllegalBlockSizeException | BadPaddingException
						| NoSuchPaddingException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cursor.close();
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);

		}
	}

	private void syncDatabase() {

		String username = (String) getIntent().getExtras().get(
				OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get(
				OpeningActivity.PASSWORD_KEY);

		String openHdsBaseUrl = getPreferenceString(this,
				R.string.openhds_server_url_key, "");
		SyncEntitiesTask currentTask = new SyncEntitiesTask(openHdsBaseUrl,
				username, password, syncDatabaseHelper.getProgressDialog(),
				this, syncDatabaseHelper);
		syncDatabaseHelper.setCurrentTask(currentTask);

		syncDatabaseHelper.startSync();
	}

	private void syncFieldWorkers() {

		String username = (String) getIntent().getExtras().get(
				OpeningActivity.USERNAME_KEY);
		String password = (String) getIntent().getExtras().get(
				OpeningActivity.PASSWORD_KEY);
		String path = getResourceString(this, R.string.field_workers_sync_url);

		RequestContext requestContext = new RequestContext().user(username)
				.password(password).url(buildServerUrl(this, path));
		SyncFieldworkersTask currentTask = new SyncFieldworkersTask(
				requestContext, getContentResolver(),
				syncDatabaseHelper.getProgressDialog(), syncDatabaseHelper);
		syncDatabaseHelper.setCurrentTask(currentTask);

		syncDatabaseHelper.startSync();
	}

}
