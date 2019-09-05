package org.cimsbioko.utilities;

import android.app.Activity;
import android.content.Intent;
import org.cimsbioko.activity.FieldWorkerLoginActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;

public class LoginUtils {

    private static final Map<Class, Login> logins = new HashMap<>();

    public static class Login<User extends Serializable> {

        User authenticatedUser;

        private Login() {
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
                launchLogin(ctx);
            }
        }

    }

    public static <T extends Serializable> Login<T> getLogin(Class<T> type) {
        if (!logins.containsKey(type)) {
            logins.put(type, new Login<T>());
        }
        return logins.get(type);
    }

    static void launchLogin(Activity ctx) {
        ctx.startActivity(new Intent(ctx, FieldWorkerLoginActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TASK));
    }
}
