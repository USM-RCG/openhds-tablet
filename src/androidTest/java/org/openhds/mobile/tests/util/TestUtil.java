package org.openhds.mobile.tests.util;

import android.content.Context;
import android.test.AndroidTestCase;

import java.lang.reflect.InvocationTargetException;

public class TestUtil {
    /**
     * Works around the fact the getTestContext method is hidden by using reflection at runtime.
     */
    public static Context getTestContext(AndroidTestCase testCase)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (Context) testCase.getClass().getMethod("getTestContext").invoke(testCase);
    }
}
