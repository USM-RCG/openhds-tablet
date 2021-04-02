package org.cimsbioko.search

import org.apache.lucene.index.Term as LuceneTerm
import org.apache.lucene.search.BooleanClause as LuceneBooleanClause
import org.apache.lucene.search.BooleanQuery as LuceneBooleanQuery
import org.apache.lucene.search.FuzzyQuery as LuceneFuzzyQuery
import org.apache.lucene.search.Query as LuceneQuery
import org.apache.lucene.search.RegexpQuery as LuceneRegexpQuery
import org.apache.lucene.search.WildcardQuery as LuceneWildcardQuery

interface Query

interface BooleanClause {
    enum class Occurs {
        MUST, SHOULD, MUST_NOT
    }

    val query: Query
    val occurs: Occurs
}

interface BooleanQuery : Query {
    val clauses: List<BooleanClause>
}

interface TermQuery : Query {
    val field: String
    val text: String
}

interface FuzzyQuery : TermQuery {
    val maxEdits: Int
}

interface WildcardQuery : TermQuery

interface RegexpQuery : TermQuery

interface SearchQueryBuilder {
    fun build(query: String): Query
}

fun BooleanClause.Occurs.translate(): LuceneBooleanClause.Occur {
    return when (this) {
        BooleanClause.Occurs.MUST -> LuceneBooleanClause.Occur.MUST
        BooleanClause.Occurs.SHOULD -> LuceneBooleanClause.Occur.SHOULD
        BooleanClause.Occurs.MUST_NOT -> LuceneBooleanClause.Occur.MUST_NOT
    }
}

fun BooleanClause.translate(): LuceneBooleanClause {
    return LuceneBooleanClause(query.translate(), occurs.translate())
}

fun Query.translate(): LuceneQuery {
    return when (this) {
        is BooleanQuery -> LuceneBooleanQuery().also {
            clauses.forEach { clause ->
                it.add(clause.translate())
            }
        }
        is FuzzyQuery -> LuceneFuzzyQuery(LuceneTerm(field, text), maxEdits)
        is WildcardQuery -> LuceneWildcardQuery(LuceneTerm(field, text))
        is RegexpQuery -> LuceneRegexpQuery(LuceneTerm(field, text))
        else -> error("unexpected query type: $this")
    }
}