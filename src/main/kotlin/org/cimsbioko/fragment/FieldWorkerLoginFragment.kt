package org.cimsbioko.fragment

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.cimsbioko.R
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.data.GatewayRegistry.fieldWorkerGateway
import org.cimsbioko.databinding.FieldworkerLoginFragmentBinding
import org.cimsbioko.model.FieldWorker
import org.cimsbioko.utilities.LoginUtils.login
import org.cimsbioko.utilities.MessageUtils.showLongToast
import org.mindrot.jbcrypt.BCrypt

class FieldWorkerLoginFragment : Fragment(), View.OnClickListener, View.OnKeyListener {

    companion object {
        private val UNKNOWN_FIELDWORKER = FieldWorker(
                uuid = "UnknownFieldWorker",
                extId = "UNK",
                firstName = "Unknown",
                lastName = "FieldWorker"
        )
        const val REVIEW_USERNAME = "PLAY"
        const val REVIEW_PASSWORD = "review"
    }

    private lateinit var job: Job

    private var usernameEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var loginButton: Button? = null

    override fun onResume() {
        job = Job()
        super.onResume()
    }

    override fun onPause() {
        job.cancel("fragment paused")
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FieldworkerLoginFragmentBinding.inflate(inflater, container, false).also { binding ->
            binding.titleTextView.setText(R.string.fieldworker_login)
            usernameEditText = binding.usernameEditText.also {
                it.setOnKeyListener(this@FieldWorkerLoginFragment)
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            }
            passwordEditText = binding.passwordEditText.also { it.setOnKeyListener(this) }
            loginButton = binding.loginButton.also { it.setOnClickListener(this) }
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        usernameEditText = null
        passwordEditText = null
        loginButton = null
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                lifecycleScope.launch { authenticateFieldWorker() }
                return true
            }
        }
        return false
    }

    override fun onClick(view: View) {
        lifecycleScope.launch {
            try {
                loginButton?.isEnabled = false
                authenticateFieldWorker()
            } finally {
                loginButton?.isEnabled = true
            }
        }
    }

    private fun getTextString(text: EditText): String {
        return text.text.toString()
    }

    private suspend fun authenticateFieldWorker() {
        val activity = requireActivity()
        val username = usernameEditText?.let { getTextString(it) }
        val password = passwordEditText?.let { getTextString(it) }
        if (username == REVIEW_USERNAME && password == REVIEW_PASSWORD) {
            authenticationSuccess(UNKNOWN_FIELDWORKER)
            return
        }
        val fieldWorkerGateway = fieldWorkerGateway
        val fieldWorker = withContext(Dispatchers.IO) { username?.let { fieldWorkerGateway.findByExtId(it).first } }
        if (fieldWorker != null) {
            if (withContext(Dispatchers.Default) { BCrypt.checkpw(password, fieldWorker.passwordHash) }) {
                authenticationSuccess(fieldWorker)
                return
            } else {
                showLongToast(activity, R.string.field_worker_bad_credentials)
            }
        } else {
            if (withContext(Dispatchers.IO) { fieldWorkerGateway.findAll().exists() }) {
                showLongToast(activity, R.string.field_worker_bad_credentials)
            } else {
                showLongToast(activity, R.string.field_worker_none_exist)
            }
        }
        login.logout(activity, false)
    }

    private fun authenticationSuccess(fieldWorker: FieldWorker) {
        requireActivity().run {
            login.authenticatedUser = fieldWorker
            if (isTaskRoot) launchPortalActivity()
            else finish()
        }
    }

    private fun launchPortalActivity() = startActivity(Intent(activity, FieldWorkerActivity::class.java))
}