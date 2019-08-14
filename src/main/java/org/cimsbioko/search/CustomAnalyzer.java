package org.cimsbioko.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.Reader;

import static org.apache.lucene.util.Version.LUCENE_40;

/**
 * A custom lucene {@link Analyzer} that processes input during indexing. It processes input into a
 * stream of lowercase alphanumeric tokens.
 */
public class CustomAnalyzer extends Analyzer {
    @Override
    protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader reader) {
        Tokenizer src = new AlphaNumericTokenizer(LUCENE_40, reader);
        TokenStream tok = new LowerCaseFilter(LUCENE_40, src);
        return new TokenStreamComponents(src, tok);
    }
}