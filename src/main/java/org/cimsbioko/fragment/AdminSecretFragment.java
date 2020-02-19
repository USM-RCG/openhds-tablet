package org.cimsbioko.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import org.cimsbioko.R;

public class AdminSecretFragment extends DialogFragment {

    private Listener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.admin_secret_fragment, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.admin_access)
                .setView(content)
                .setPositiveButton(android.R.string.ok,
                        (dlg, id) -> {
                            EditText passwordField = content.findViewById(R.id.adminSecretEditText);
                            String secret = passwordField.getText().toString();
                            listener.onAdminSecretDialogOk(secret);
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dlg, id) -> listener.onAdminSecretDialogCancel())
                .create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (Listener) context;
    }

    public interface Listener {
        void onAdminSecretDialogOk(String secret);

        void onAdminSecretDialogCancel();
    }
}