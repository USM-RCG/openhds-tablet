package org.openhds.mobile.tests;

import android.test.ActivityInstrumentationTestCase2;
import org.openhds.mobile.activity.OpeningActivity;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.openhds.mobile.tests.OpeningActivityTest \
 * org.openhds.mobile.tests/android.test.InstrumentationTestRunner
 */
public class OpeningActivityTest extends ActivityInstrumentationTestCase2<OpeningActivity> {

    public OpeningActivityTest() {
        super("org.openhds.mobile", OpeningActivity.class);
    }

    public void testNothing() {
        assertEquals(false, false);
    }

}
