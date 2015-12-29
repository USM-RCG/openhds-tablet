package org.openhds.mobile.tests;

import android.test.AndroidTestCase;
import org.openhds.mobile.task.http.HttpTask;
import org.openhds.mobile.task.http.HttpTaskRequest;
import org.openhds.mobile.task.http.HttpTaskResponse;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Make sure we can make a GET request and return some data.
 *
 * Use a handy http testing service called httpbin:
 *   http://httpbin.org/
 * This allows us to make a request, and it and returns a
 * JSON object describing our request.
 *
 * BSH
 */
public class HttpTaskTest  extends AndroidTestCase {

    private static final String TEST_GET_URL =  "http://httpbin.org/get";
    private static final String TEST_GET_PARAM =  "sauce";
    private static final String TEST_GET_VALUE =  "ketchupymustard";

    private static final String BAD_GET_URL =  "http://vedtrg.com/veiurgerg/serghuisrg/earg";

    private static final long TASK_TIMEOUT = 10;

    HttpTaskResponse httpTaskResponse;

    public void testGetWithParam() throws Exception {
        // start a task to GET from the httpbin service
        httpTaskResponse = null;
        final String testUrl = TEST_GET_URL + "?" + TEST_GET_PARAM + "=" + TEST_GET_VALUE;
        HttpTaskRequest httpTaskRequest = new HttpTaskRequest(0, testUrl, null, "", "");
        HttpTask httpTask = new HttpTask(new ResponseHandler());
        httpTask.execute(httpTaskRequest);

        // wait for the task to complete
        httpTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        // make sure we got a response
        assertNotNull(httpTaskResponse);
        assertTrue(httpTaskResponse.isSuccess());
        InputStream inputStream = httpTaskResponse.getInputStream();
        assertNotNull(inputStream);

        // scan the whole input stream into a string
        Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
        String response = scanner.next();
        inputStream.close();

        // make sure the response contains some expected content
        assertTrue(response.contains(TEST_GET_PARAM));
        assertTrue(response.contains(TEST_GET_VALUE));
    }

    public void testGetBadUrl() throws Exception {
        // start a task to GET from a bogus url
        httpTaskResponse = null;
        HttpTaskRequest httpTaskRequest = new HttpTaskRequest(0, BAD_GET_URL, null, "", "");
        HttpTask httpTask = new HttpTask(new ResponseHandler());
        httpTask.execute(httpTaskRequest);

        // wait for the task to complete
        httpTask.get(TASK_TIMEOUT, TimeUnit.SECONDS);
        Thread.sleep(100);

        // make sure we didn't get a response
        assertNotNull(httpTaskResponse);
        assertFalse(httpTaskResponse.isSuccess());
    }

    private class ResponseHandler implements HttpTask.HttpTaskResponseHandler {
        @Override
        public void handleHttpTaskResponse(HttpTaskResponse httpTaskResponse) {
            HttpTaskTest.this.httpTaskResponse = httpTaskResponse;
        }
    }
}
