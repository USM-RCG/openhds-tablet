package org.openhds.mobile.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import org.openhds.mobile.R;

public class DeleteWarningDialogFragment extends DialogFragment {


    DeleteWarningDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_forms_dialog_warning);
        builder.setTitle(R.string.delete_dialog_warning_title);
        builder.setPositiveButton(R.string.delete_form_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onDialogPositiveClick(DeleteWarningDialogFragment.this);
            }
        });
        builder.setNegativeButton(R.string.delete_warning_dialog_cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onDialogNegativeClick(DeleteWarningDialogFragment.this);
            }
        });

        return builder.create();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (DeleteWarningDialogListener) activity;
    }
}