package org.openhds.mobile.tests.parsing;

import android.test.AndroidTestCase;
import org.openhds.mobile.task.parsing.DataPage;
import org.openhds.mobile.task.parsing.XmlPageParser;

import java.io.InputStream;

import static java.util.Arrays.asList;

public class XmlPageParserTest extends AndroidTestCase {

    private void validateTestPage(DataPage dataPage) {
        assertEquals("rootElement", dataPage.getRootElementName());
        assertEquals("pageElement", dataPage.getPageElementName());

        assertEquals("page text", dataPage.getFirstString(asList("pageElement")));

        assertEquals("simple text", dataPage.getFirstString(asList("pageElement", "simple")));

        String spanningText = dataPage.getFirstString(asList("pageElement", "spanning"));
        assertTrue(spanningText.startsWith("line spanning"));
        assertTrue(spanningText.endsWith("text"));

        assertEquals(1, dataPage.getFirstInt(asList("pageElement", "repeated")));
        assertEquals(2, dataPage.getIntOccurrence(asList("pageElement", "repeated"), 1));
        assertEquals(3, dataPage.getIntOccurrence(asList("pageElement", "repeated"), 2));

        assertEquals("inner text", dataPage.getFirstString(asList("pageElement", "outer", "inner", "simple")));
    }

    public void testHappyParse() throws Exception {
        XmlPageParser pageParser = new XmlPageParser();

        HappyPageHandler happyPageHandler = new HappyPageHandler();
        pageParser.setPageHandler(happyPageHandler);

        // should parse all three xml data "pages"
        InputStream inputStream = getContext().getAssets().open("testXml/page-parser-test.xml");
        pageParser.parsePages(inputStream);

        assertEquals(3, happyPageHandler.getPageCount());
    }

    private class HappyPageHandler implements XmlPageParser.PageHandler {
        private int pageCount = 0;

        public int getPageCount() {
            return pageCount;
        }

        @Override
        public boolean handlePage(DataPage dataPage) {
            pageCount++;
            validateTestPage(dataPage);
            return true;
        }
    }

    public void testUnhappyParse() throws Exception {
        XmlPageParser pageParser = new XmlPageParser();

        UnhappyPageHandler unhappyPageHandler = new UnhappyPageHandler();
        pageParser.setPageHandler(unhappyPageHandler);

        UnhappyPageErrorHandler unhappyPageErrorHandler = new UnhappyPageErrorHandler();
        pageParser.setPageErrorHandler(unhappyPageErrorHandler);

        // should error on first page, parse the second page, and quit before the third page
        InputStream inputStream = getContext().getAssets().open("testXml/page-parser-test.xml");
        pageParser.parsePages(inputStream);

        assertEquals(2, unhappyPageHandler.getPageCount());
        assertEquals(1, unhappyPageErrorHandler.getErrorCount());
    }

    private class UnhappyPageHandler implements XmlPageParser.PageHandler {
        private int pageCount = 0;

        public int getPageCount() {
            return pageCount;
        }

        @Override
        public boolean handlePage(DataPage dataPage) {
            pageCount++;
            if (1 == pageCount) {
                throw new RuntimeException("deliberate page handler error!");
            }
            if (2 == pageCount) {
                validateTestPage(dataPage);

                // tell the parser to abort
                return false;
            }
            return true;
        }
    }

    private class UnhappyPageErrorHandler implements XmlPageParser.PageErrorHandler {
        private int errorCount = 0;

        public int getErrorCount() {
            return errorCount;
        }

        @Override
        public boolean handlePageError(DataPage dataPage, Exception e) {
            errorCount++;
            return true;
        }
    }
}
