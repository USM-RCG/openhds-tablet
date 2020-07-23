package org.cimsbioko.navconfig.db

import org.cimsbioko.data.DataWrapper

interface QueryHelper {
    fun getAll(level: String): List<DataWrapper>
    fun getChildren(qr: DataWrapper, childLevel: String): List<DataWrapper>
    operator fun get(level: String, uuid: String): DataWrapper?
    fun getParent(level: String, uuid: String): DataWrapper?
}