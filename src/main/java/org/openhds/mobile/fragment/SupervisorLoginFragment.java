package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.http.HttpStatus;
import org.openhds.mobile.R;
import org.openhds.mobile.activity.OpeningActivity;
import org.openhds.mobile.activity.SupervisorMainActivity;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.task.SupervisorLoginTask;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

public class SupervisorLoginFragment extends Fragment implements
        OnClickListener, OnKeyListener {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private DatabaseAdapter databaseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.generic_login_fragment, container, false);
        TextView title = (TextView) v.findViewById(R.id.titleTextView);
        title.setText(R.string.supervisor_login);

        usernameEditText = (EditText) v.findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
        usernameEditText.setOnKeyListener(this);
        passwordEditText.setOnKeyListener(this);
        Button loginButton = (Button) v.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        databaseAdapter = new DatabaseAdapter(getActivity());

        return v;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    authenticateSupervisor();
                    return true;
            }
        }
        return false;
    }

    public void onClick(View view) {
        authenticateSupervisor();
    }

    private String getUsernameFromEditText() {
        return usernameEditText.getText().toString();
    }

    private String getPasswordFromEditText() {
        return passwordEditText.getText().toString();
    }

    private String getUrl() {
        // supervisor_login_url needs to be a secured resource on the sever
        // for example openhds/api/rest/socialgroups
        String path = getResourceString(getActivity(), R.string.supervisor_login_url);

        return buildServerUrl(getActivity(), path);
    }

    private void authenticateSupervisor() {
        HttpTaskRequest httpTaskRequest = new HttpTaskRequest(
                R.string.login_btn, getUrl(), null, getUsernameFromEditText(), getPasswordFromEditText());

        HttpTask httpTask = new HttpTask(new AuthenticateListener());
        httpTask.execute(httpTaskRequest);
    }

    private void onConnectedAndAuthenticated() {
        // valid credentials were cached in tablet database by AuthenticateTask
        // delete any stale credentials from local then add authenticated
        // credentials to match server
        deleteSupervisor();
        addSupervisor();
        launchSupervisorMainActivity();
    }

    private void onConnectedButNotAuthenticated() {
        // delete unauthorized user from tablet database
        // to prevent login when not connected to network
        showLongToast(getActivity(), R.string.supervisor_bad_credentials);
        deleteSupervisor();
    }

    private void deleteSupervisor() {
        Supervisor user = new Supervisor();
        user.setName(getUsernameFromEditText());
        databaseAdapter.deleteSupervisor(user);
    }

    private void addSupervisor() {
        Supervisor user = new Supervisor();
        user.setName(getUsernameFromEditText());
        user.setPassword(getPasswordFromEditText());
        databaseAdapter.addSupervisor(user);
    }

    private void onNotConnected() {
        // attempt to log in using cached credentials in tablet database
        SupervisorLoginTask loginTask = new SupervisorLoginTask(
                databaseAdapter, getUsernameFromEditText(),
                getPasswordFromEditText(), new LoginListener());
        loginTask.execute();
    }

    private void launchSupervisorMainActivity() {
        Intent intent = new Intent(getActivity(), SupervisorMainActivity.class);
        intent.putExtra(OpeningActivity.USERNAME_KEY, getUsernameFromEditText());
        intent.putExtra(OpeningActivity.PASSWORD_KEY, getPasswordFromEditText());
        startActivity(intent);
    }

    private class AuthenticateListener implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (httpTaskResponse.isSuccess()) {
                onConnectedAndAuthenticated();
                return;
            }

            if (HttpStatus.SC_FORBIDDEN == httpTaskResponse.getHttpStatus()) {
                onConnectedButNotAuthenticated();
            }

            onNotConnected();
        }
    }

    private class LoginListener implements SupervisorLoginTask.Listener {
        public void onAuthenticated() {
            launchSupervisorMainActivity();
        }

        public void onBadAuthentication() {
            showLongToast(getActivity(), R.string.supervisor_bad_credentials);
        }
    }
}
