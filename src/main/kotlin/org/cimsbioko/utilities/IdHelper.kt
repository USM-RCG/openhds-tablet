package org.cimsbioko.utilities

import org.cimsbioko.navconfig.UsedByJSConfig
import java.util.*

object IdHelper {

    @UsedByJSConfig
    fun generateEntityUuid(): String = UUID.randomUUID().toString().replace("-", "")

}