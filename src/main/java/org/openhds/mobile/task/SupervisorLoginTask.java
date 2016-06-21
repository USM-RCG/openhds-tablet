package org.openhds.mobile.task;

import android.os.AsyncTask;

import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.provider.DatabaseAdapter;

/**
 * The Login task will verify a local user exists in the database If no user
 * exist, the user must initially make a request to download forms with the
 * typed in username/password. If successfully
 */
public class SupervisorLoginTask extends AsyncTask<Boolean, Void, Supervisor> {

	private DatabaseAdapter storage;
	private String username;
	private String password;
	private Listener listener;

	public interface Listener {
		void onAuthenticated(Supervisor user);

		void onBadAuthentication();
	}

	public SupervisorLoginTask(DatabaseAdapter storage, String user, String password,
			Listener listener) {
		this.storage = storage;
		this.username = user;
		this.password = password;
		this.listener = listener;
	}

	@Override
	protected Supervisor doInBackground(Boolean... params) {
		Supervisor user = storage.findSupervisorByUsername(username);
		if (user != null && user.getPassword().equals(password)) {
			return user;
		} else {
			return null;
		}
	}

    @Override
    protected void onPostExecute(Supervisor user) {
        if (user == null) {
            listener.onBadAuthentication();
        } else {
            listener.onAuthenticated(user);
        }
    }

}
