package org.cimsbioko.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.cimsbioko.R
import org.cimsbioko.databinding.AdminSecretFragmentBinding

class AdminSecretFragment : DialogFragment() {

    private var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AdminSecretFragmentBinding.inflate(requireActivity().layoutInflater).let { content ->
                AlertDialog.Builder(requireContext())
                        .setTitle(R.string.admin_access)
                        .setView(content.root)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            listener?.onAdminSecretDialogOk(content.adminSecretEditText.text.toString())
                        }
                        .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int -> listener?.onAdminSecretDialogCancel() }
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