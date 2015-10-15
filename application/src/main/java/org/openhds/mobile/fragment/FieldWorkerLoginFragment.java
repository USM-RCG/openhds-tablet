package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
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
import org.openhds.mobile.activity.PortalActivity;
import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.mindrot.jbcrypt.BCrypt;

import static org.openhds.mobile.utilities.MessageUtils.showLongToast;

public class FieldWorkerLoginFragment extends Fragment implements
        OnClickListener, OnKeyListener {

    public static final String FIELD_WORKER_EXTRA = "FieldWorker";

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.generic_login_fragment, container,
                false);
        TextView title = (TextView) v.findViewById(R.id.titleTextView);
        title.setText(R.string.fieldworker_login);

        usernameEditText = (EditText) v.findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) v.findViewById(R.id.passwordEditText);
        usernameEditText.setOnKeyListener(this);
        passwordEditText.setOnKeyListener(this);
        loginButton = (Button) v.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        return v;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    authenticateFieldWorker();
            }
        }
        return false;
    }

    public void onClick(View view) {
        authenticateFieldWorker();
    }

    private String getTextString(EditText text) {
        return text.getText().toString();
    }

    private void authenticateFieldWorker() {

        // current implementation does not require password
        String password = getTextString(passwordEditText);
        String username = getTextString(usernameEditText);

        FieldWorkerGateway fieldWorkerGateway = GatewayRegistry.getFieldWorkerGateway();
        ContentResolver contentResolver = getActivity().getContentResolver();
        FieldWorker fieldWorker = fieldWorkerGateway.getFirst(contentResolver, fieldWorkerGateway.findByExtId(username));

        if (null == fieldWorker || !BCrypt.checkpw(password,fieldWorker.getPasswordHash())) {
            showLongToast(getActivity(), R.string.field_worker_bad_credentials);
        } else {
            launchPortalActivity(fieldWorker);
        }
    }

    private void launchPortalActivity(FieldWorker fieldWorker) {
        Intent intent = new Intent(getActivity(), PortalActivity.class);
        intent.putExtra(FIELD_WORKER_EXTRA, fieldWorker);
        startActivity(intent);
    }
}
