package org.cimsbioko.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import org.cimsbioko.R
import org.cimsbioko.activity.FieldWorkerActivity
import org.cimsbioko.data.GatewayRegistry.fieldWorkerGateway
import org.cimsbioko.utilities.LoginUtils.login
import org.cimsbioko.utilities.MessageUtils.showLongToast
import org.mindrot.jbcrypt.BCrypt
import kotlin.coroutines.CoroutineContext

class FieldWorkerLoginFragment : Fragment(), View.OnClickListener, View.OnKeyListener, CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onResume() {
        job = Job()
        super.onResume()
    }

    override fun onPause() {
        job.cancel("fragment paused")
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fieldworker_login_fragment, container, false).also { vg ->
            vg.findViewById<TextView>(R.id.titleTextView).apply {
                setText(R.string.fieldworker_login)
            }
            usernameEditText = vg.findViewById<EditText>(R.id.usernameEditText).also {
                it.setOnKeyListener(this)
                it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            }
            passwordEditText = vg.findViewById<EditText>(R.id.passwordEditText).also { it.setOnKeyListener(this) }
            loginButton = vg.findViewById<Button>(R.id.loginButton).also { it.setOnClickListener(this) }
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                launch { authenticateFieldWorker() }
                return true
            }
        }
        return false
    }

    override fun onClick(view: View) {
        launch {
            try {
                loginButton.isEnabled = false
                authenticateFieldWorker()
            } finally {
                loginButton.isEnabled = true
            }
        }
    }

    private fun getTextString(text: EditText): String {
        return text.text.toString()
    }

    private suspend fun authenticateFieldWorker() {
        val activity: Activity = requireActivity()
        val (username, password) = Pair(getTextString(usernameEditText), getTextString(passwordEditText))
        val login = login
        val fieldWorkerGateway = fieldWorkerGateway
        val fieldWorker = withContext(Dispatchers.IO) { fieldWorkerGateway.findByExtId(username).first }
        if (fieldWorker != null) {
            if (withContext(Dispatchers.Default) { BCrypt.checkpw(password, fieldWorker.passwordHash) }) {
                login.authenticatedUser = fieldWorker
                if (activity.isTaskRoot) {
                    launchPortalActivity()
                } else {
                    activity.finish()
                }
                return
            } else {
                showLongToast(getActivity(), R.string.field_worker_bad_credentials)
            }
        } else {
            if (withContext(Dispatchers.IO) { fieldWorkerGateway.findAll().exists() }) {
                showLongToast(getActivity(), R.string.field_worker_bad_credentials)
            } else {
                showLongToast(getActivity(), R.string.field_worker_none_exist)
            }
        }
        login.logout(activity, false)
    }

    private fun launchPortalActivity() {
        startActivity(Intent(activity, FieldWorkerActivity::class.java))
    }
}