package org.openhds.mobile.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.SupervisorActivity;
import org.openhds.mobile.model.core.Supervisor;
import org.openhds.mobile.provider.DatabaseAdapter;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;

import static org.openhds.mobile.utilities.ConfigUtils.getResourceString;
import static org.openhds.mobile.utilities.LoginUtils.getLogin;
import static org.openhds.mobile.utilities.MessageUtils.showLongToast;
import static org.openhds.mobile.utilities.UrlUtils.buildServerUrl;

public class SupervisorLoginFragment extends Fragment implements OnClickListener, OnKeyListener {

    private static String TAG = SupervisorLoginFragment.class.getSimpleName();

    private EditText usernameText;
    private EditText passwordText;
    private DatabaseAdapter databaseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.generic_login_fragment, container, false);
        TextView title = (TextView) v.findViewById(R.id.titleTextView);
        title.setText(R.string.supervisor_login);

        usernameText = (EditText) v.findViewById(R.id.usernameEditText);
        passwordText = (EditText) v.findViewById(R.id.passwordEditText);
        usernameText.setOnKeyListener(this);
        passwordText.setOnKeyListener(this);
        Button loginButton = (Button) v.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        databaseAdapter = DatabaseAdapter.getInstance(getActivity());

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

    private String getUsername() {
        return usernameText.getText().toString();
    }

    private String getPassword() {
        return passwordText.getText().toString();
    }

    private void authenticateSupervisor() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            authConnected();
        } else {
            authDisconnected();
        }
    }

    private void authConnected() {
        Log.i(TAG, "attempting connected auth: " + getUrl() + " user=" + getUsername());
        HttpTaskRequest httpTaskRequest = new HttpTaskRequest(getUrl(), null, getUsername(), getPassword());
        HttpTask httpTask = new HttpTask(new AuthenticateListener());
        httpTask.execute(httpTaskRequest);
    }

    private void authDisconnected() {
        Log.i(TAG, "attempting disconnected auth: user=" + getUsername());
        Supervisor user = databaseAdapter.findSupervisorByUsername(getUsername());
        if (user != null && user.getPassword().equals(getPassword())) {
            launchOnSuccess(user);
        } else {
            onDisconnectedFailure();
        }
    }

    private void launchOnSuccess(Supervisor user) {
        setAuthenticatedUser(user);
        launchSupervisorActivity();
    }

    private String getUrl() {
        String path = getResourceString(getActivity(), R.string.supervisor_login_url);
        return buildServerUrl(getActivity(), path);
    }

    private void logoutAuthenticatedUser() {
        getLogin(Supervisor.class).logout(getActivity(), false);
    }

    private void launchSupervisorActivity() {
        startActivity(new Intent(getActivity(), SupervisorActivity.class));
    }

    private void deleteSupervisor(String username) {
        Supervisor user = new Supervisor();
        user.setName(username);
        databaseAdapter.deleteSupervisor(user);
    }

    private void addSupervisor(Supervisor user) {
        databaseAdapter.addSupervisor(user);
    }

    private void setAuthenticatedUser(Supervisor user) {
        getLogin(Supervisor.class).setAuthenticatedUser(user);
    }

    private void onConnectedFailure(HttpTask.Result result) {
        Log.i(TAG, "connected auth failed for " + getUsername());
        deleteSupervisor(getUsername());
        onFailure(getErrorMessage(result));
    }

    private int getErrorMessage(HttpTask.Result result) {
        switch (result) {
            case AUTH_ERROR:
                return R.string.supervisor_bad_credentials;
            case CONNECT_FAILURE:
                return R.string.server_connect_error;
            case CLIENT_ERROR:
                return R.string.http_client_error;
            case SERVER_ERROR:
                return R.string.http_server_error;
            default:
                return R.string.unknown_error;
        }
    }

    private void onDisconnectedFailure() {
        Log.i(TAG, "disconnected auth failed for " + getUsername());
        onFailure(R.string.supervisor_bad_credentials);
    }

    private void onFailure(int msgId) {
        logoutAuthenticatedUser();
        showLongToast(getActivity(), msgId);
    }


    private class AuthenticateListener implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            if (httpTaskResponse.isSuccess()) {
                onSuccess();
            } else {
                onConnectedFailure(httpTaskResponse.getResult());
            }
        }

        private void onSuccess() {
            deleteSupervisor(getUsername());
            Supervisor user = new Supervisor();
            user.setName(getUsername());
            user.setPassword(getPassword());
            addSupervisor(user);
            launchOnSuccess(user);
        }
    }
}
