package org.cimsbioko.search

import org.apache.lucene.document.Document

internal interface DocumentSource {
    operator fun next(): Boolean
    fun size(): Int
    val document: Document
    fun close()
}