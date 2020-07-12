package org.cimsbioko.utilities;

import android.app.Activity;
import android.content.Intent;
import org.cimsbioko.activity.FieldWorkerLoginActivity;
import org.cimsbioko.model.core.FieldWorker;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

public class LoginUtils {

    private static final Login<FieldWorker> login = new Login<>();

    public static class Login<U> {

        U authenticatedUser;

        private Login() {
        }

        public void setAuthenticatedUser(U user) {
            authenticatedUser = user;
        }

        public U getAuthenticatedUser() {
            return authenticatedUser;
        }

        public void logout(Activity ctx, boolean launchLoginActivity) {
            authenticatedUser = null;
            if (launchLoginActivity) {
                launchLogin(ctx);
            }
        }

    }

    public static Login<FieldWorker> getLogin() {
        return login;
    }

    static void launchLogin(Activity ctx) {
        ctx.startActivity(new Intent(ctx, FieldWorkerLoginActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK));
    }
}
