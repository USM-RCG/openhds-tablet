package org.cimsbioko.utilities;

import org.cimsbioko.navconfig.UsedByJSConfig;

import java.util.UUID;

public class IdHelper {

    @UsedByJSConfig
    public static String generateEntityUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
