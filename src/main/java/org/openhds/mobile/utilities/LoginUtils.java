package org.openhds.mobile.utilities;

import android.app.Activity;
import android.content.Intent;

import org.openhds.mobile.activity.LoginActivity;
import org.openhds.mobile.model.core.Supervisor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static org.openhds.mobile.activity.LoginActivity.FIELD_WORKER_IDX;
import static org.openhds.mobile.activity.LoginActivity.SELECTED_LOGIN_KEY;
import static org.openhds.mobile.activity.LoginActivity.SUPERVISOR_IDX;

public class LoginUtils {

    private static final Map<Class, Login> logins = new HashMap<>();

    public static class Login<User extends Serializable> {

        User authenticatedUser;

        private Login() {
        }

        public boolean hasAuthenticatedUser() {
            return authenticatedUser != null;
        }

        public void setAuthenticatedUser(User user) {
            authenticatedUser = user;
        }

        public User getAuthenticatedUser() {
            return authenticatedUser;
        }

        public void logout(Activity ctx, boolean launchLoginActivity) {
            authenticatedUser = null;
            if (launchLoginActivity) {
                launchLogin(ctx, authenticatedUser instanceof Supervisor);
            }
        }

    }

    public static <T extends Serializable> Login<T> getLogin(Class<T> type) {
        if (!logins.containsKey(type)) {
            logins.put(type, new Login<T>());
        }
        return logins.get(type);
    }

    public static void launchLogin(Activity ctx, boolean showSupervisor) {
        ctx.startActivity(new Intent(ctx, LoginActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(SELECTED_LOGIN_KEY, showSupervisor ? SUPERVISOR_IDX : FIELD_WORKER_IDX));
    }
}
