package org.cimsbioko.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.mindrot.jbcrypt.BCrypt;
import org.cimsbioko.R;
import org.cimsbioko.activity.FieldWorkerActivity;
import org.cimsbioko.model.core.FieldWorker;
import org.cimsbioko.repository.GatewayRegistry;
import org.cimsbioko.repository.FieldWorkerGateway;
import org.cimsbioko.utilities.LoginUtils;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
import static org.cimsbioko.utilities.LoginUtils.getLogin;
import static org.cimsbioko.utilities.MessageUtils.showLongToast;

public class FieldWorkerLoginFragment extends Fragment implements OnClickListener, OnKeyListener {

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fieldworker_login_fragment, container, false);
        TextView title = v.findViewById(R.id.titleTextView);
        title.setText(R.string.fieldworker_login);

        usernameEditText = v.findViewById(R.id.usernameEditText);
        passwordEditText = v.findViewById(R.id.passwordEditText);
        usernameEditText.setOnKeyListener(this);
        passwordEditText.setOnKeyListener(this);

        // Field worker user names are all-caps, this locks soft keyboard into caps mode
        usernameEditText.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_CHARACTERS);

        Button loginButton = v.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        return v;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    authenticateFieldWorker();
                    return true;
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

        FieldWorker fieldWorker = fieldWorkerGateway.findByExtId(username).getFirst();

        Activity activity = getActivity();
        LoginUtils.Login login = getLogin(FieldWorker.class);
        if (fieldWorker != null) {
            if (BCrypt.checkpw(password, fieldWorker.getPasswordHash())) {
                login.setAuthenticatedUser(fieldWorker);
                if (activity.isTaskRoot()) {
                    launchPortalActivity();
                } else {
                    activity.finish();
                }
                return;
            } else {
                showLongToast(getActivity(), R.string.field_worker_bad_credentials);
            }
        } else {
            if (fieldWorkerGateway.findAll().getFirst() != null) {
                showLongToast(getActivity(), R.string.field_worker_bad_credentials);
            } else {
                showLongToast(getActivity(), R.string.field_worker_none_exist);
            }
        }
        login.logout(getActivity(), false);
    }

    private void launchPortalActivity() {
        startActivity(new Intent(getActivity(), FieldWorkerActivity.class));
    }
}
