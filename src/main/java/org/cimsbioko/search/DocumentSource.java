package org.cimsbioko.search;

import org.apache.lucene.document.Document;

interface DocumentSource {

    boolean next();

    int size();

    Document getDocument();

    void close();
}
