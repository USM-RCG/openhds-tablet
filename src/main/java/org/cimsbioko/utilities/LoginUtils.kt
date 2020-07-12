package org.cimsbioko.utilities

import android.app.Activity
import android.content.Intent
import org.cimsbioko.activity.FieldWorkerLoginActivity
import org.cimsbioko.model.core.FieldWorker

object LoginUtils {

    @JvmStatic
    val login = Login<FieldWorker>()

    @JvmStatic
    fun launchLogin(ctx: Activity) {
        ctx.startActivity(Intent(ctx, FieldWorkerLoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    class Login<U> internal constructor() {

        var authenticatedUser: U? = null

        fun logout(ctx: Activity, launchLoginActivity: Boolean) {
            authenticatedUser = null
            if (launchLoginActivity) {
                launchLogin(ctx)
            }
        }
    }
}