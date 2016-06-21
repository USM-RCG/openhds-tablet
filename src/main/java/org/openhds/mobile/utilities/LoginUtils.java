package org.openhds.mobile.utilities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

        public void logout() {
            authenticatedUser = null;
        }

    }

    public static <T extends Serializable> Login<T> getLogin(Class<T> type) {
        if (!logins.containsKey(type)) {
            logins.put(type, new Login<T>());
        }
        return logins.get(type);
    }
}
