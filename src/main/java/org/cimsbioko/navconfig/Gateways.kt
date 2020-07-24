package org.cimsbioko.navconfig

import org.cimsbioko.data.*

/**
 * Used to provide scripts an interface into the application gateways
 */
object Gateways {

    @get:UsedByJSConfig
    val individuals: IndividualGateway
        get() = GatewayRegistry.individualGateway

    @get:UsedByJSConfig
    val locations: LocationGateway
        get() = GatewayRegistry.locationGateway

    @get:UsedByJSConfig
    val hierarchy: LocationHierarchyGateway
        get() = GatewayRegistry.locationHierarchyGateway

    @get:UsedByJSConfig
    val fieldworkers: FieldWorkerGateway
        get() = GatewayRegistry.fieldWorkerGateway

}