package org.cimsbioko.data

/**
 * Well known location for accessing type-specific gateway instances.
 */
object GatewayRegistry {

    @JvmStatic
    val fieldWorkerGateway by lazy { FieldWorkerGateway() }

    @JvmStatic
    val individualGateway by lazy { IndividualGateway() }

    @JvmStatic
    val locationGateway by lazy { LocationGateway() }

    @JvmStatic
    val locationHierarchyGateway by lazy { LocationHierarchyGateway() }
}