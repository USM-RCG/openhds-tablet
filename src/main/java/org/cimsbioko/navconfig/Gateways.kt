package org.cimsbioko.navconfig

import org.cimsbioko.data.*

/**
 * Used to provide scripts an interface into the application gateways
 */
object Gateways {

    @get:UsedByJSConfig
    val individuals: IndividualGateway
        get() = GatewayRegistry.getIndividualGateway()

    @get:UsedByJSConfig
    val locations: LocationGateway
        get() = GatewayRegistry.getLocationGateway()

    @get:UsedByJSConfig
    val hierarchy: LocationHierarchyGateway
        get() = GatewayRegistry.getLocationHierarchyGateway()

    @get:UsedByJSConfig
    val fieldworkers: FieldWorkerGateway
        get() = GatewayRegistry.getFieldWorkerGateway()

}