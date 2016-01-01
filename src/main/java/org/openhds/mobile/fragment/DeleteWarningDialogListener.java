package org.openhds.mobile.fragment;

import android.app.DialogFragment;

public interface DeleteWarningDialogListener {

    void onDialogPositiveClick(DialogFragment dialogFragment);
    void onDialogNegativeClick(DialogFragment dialogFragment);

}