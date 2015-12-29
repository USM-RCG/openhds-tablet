package org.openhds.mobile.provider;

/**
 * Get the database password that the ContentProvider should use.
 *
 * This is broken into a helper for modularity.  In particular, so
 * that we can inject a different password helper during integration
 * tests.
 *
 * BSH
 */
public interface PasswordHelper {
    public String getPassword();
}
