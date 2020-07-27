package org.cimsbioko.search

import org.apache.lucene.analysis.util.CharTokenizer
import org.apache.lucene.util.Version
import java.io.Reader

/**
 * A custom lucene [Tokenizer] that is used to generate tokens from an input stream. It defines
 * tokens as being only Alphanumeric strings. All other input is considered as part of a delimiter.
 */
class AlphaNumericTokenizer(matchVersion: Version, `in`: Reader) : CharTokenizer(matchVersion, `in`) {
    override fun isTokenChar(c: Int) = Character.isLetterOrDigit(c)
}