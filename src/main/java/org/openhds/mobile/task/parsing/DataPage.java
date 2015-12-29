package org.openhds.mobile.task.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flexible, in-memory container for data like XML elements or JSON objects.
 *
 * Maintains a data structure for text data, grouped by "paths" to the data.
 * For XML, the path to some text would be all the element names between
 * the text element and the document root element.  Similarly for JSON
 * objects.  Multiple text data can be stored under the same path.
 *
 * Provides utility methods for getting text out of the data structure.
 *
 * The idea is to bridge the gap between long data streams, which are highly
 * serial and too big to put in memory, and entity objects, which must fit
 * in memory to give random access.
 *
 * So a stream reader like XMLPageParser can dump streaming data into one
 * DataPage at a time, without worrying about element order or meaning.  Then
 * a converter like IndividualPageConverter can read from one DataPage at a
 * time with random access and without worrying about the whole data stream.
 *
 */
public class DataPage {
    private final String pageDescription;
    private final String rootElementName;
    private final String pageElementName;
    private final Map<List<String>, List<String>> pageText;

    public DataPage(String rootElementName, String pageElementName, String pageDescription) {
        this.rootElementName = rootElementName;
        this.pageElementName = pageElementName;
        this.pageDescription = pageDescription;

        pageText = new HashMap<>();
    }

    public String getRootElementName() {
        return rootElementName;
    }

    public String getPageDescription() {
        return pageDescription;
    }

    public String getPageElementName() {
        return pageElementName;
    }

    public void addText(List<String> elementPath, String text) {
        if (!pageText.containsKey(elementPath)) {
            // copy the path to avoid modifications to map keys
            pageText.put(new ArrayList<String>(elementPath), new ArrayList<String>());
        }
        pageText.get(elementPath).add(text);
    }

    public String getStringOccurrence(List<String> elementPath, int occurrence) {
        if (!pageText.containsKey(elementPath)) {
            return null;
        }

        List<String> occurrences = pageText.get(elementPath);
        if (occurrence >= occurrences.size()) {
            return null;
        }

        return occurrences.get(occurrence);
    }

    public int getIntOccurrence(List<String> elementPath, int occurrence) {
        String asString = getStringOccurrence(elementPath, occurrence);
        if (null == asString) {
            return 0;
        }
        return Integer.parseInt(asString);
    }

    public String getFirstString(List<String> elementPath) {
        return getStringOccurrence(elementPath, 0);
    }

    public int getFirstInt(List<String> elementPath) {
        return getIntOccurrence(elementPath, 0);
    }
}
