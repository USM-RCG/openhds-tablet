package org.cimsbioko.scripting

import org.cimsbioko.search.BooleanClause
import org.cimsbioko.search.BooleanQuery
import org.cimsbioko.search.Query
import org.cimsbioko.search.SearchQueryBuilder

class StubBooleanQuery : BooleanQuery {
    override val clauses: List<BooleanClause>
        get() = emptyList()
}

class StubQueryBuilder : SearchQueryBuilder {
    override fun build(query: String): Query = StubBooleanQuery()
}