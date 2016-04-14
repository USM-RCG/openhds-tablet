package org.openhds.mobile.tests;

import android.test.ActivityInstrumentationTestCase2;
import org.openhds.mobile.activity.LoginActivity;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.openhds.mobile.tests.LoginActivityTest \
 * org.openhds.mobile.tests/android.test.InstrumentationTestRunner
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity> {

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    public void testNothing() {
        assertEquals(false, false);
    }

}
