package org.cimsbioko.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.cimsbioko.R

class AdminSecretFragment : DialogFragment() {

    private var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            requireActivity().layoutInflater.inflate(R.layout.admin_secret_fragment, null).let { content ->
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.admin_access)
                        .setView(content)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            listener!!.onAdminSecretDialogOk(content.findViewById<EditText>(R.id.adminSecretEditText).text.toString())
                        }
                        .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> listener!!.onAdminSecretDialogCancel() }
                        .create()
            }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as Listener
    }

    interface Listener {
        fun onAdminSecretDialogOk(secret: String)
        fun onAdminSecretDialogCancel()
    }
}