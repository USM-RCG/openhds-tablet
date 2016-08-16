package org.openhds.mobile.search;

import org.apache.lucene.document.Document;

interface DocumentSource {

    boolean next();

    int size();

    Document getDocument();

    void close();
}
