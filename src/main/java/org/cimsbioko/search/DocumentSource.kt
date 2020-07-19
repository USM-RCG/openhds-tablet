package org.cimsbioko.search

import org.apache.lucene.document.Document
import java.io.Closeable

internal interface DocumentSource : Closeable {
    operator fun next(): Boolean
    fun size(): Int
    val document: Document
    override fun close()
}