package org.cimsbioko.data

/**
 * Well known location for accessing type-specific gateway instances.
 */
object GatewayRegistry {

    val fieldWorkerGateway by lazy { FieldWorkerGateway() }

    val individualGateway by lazy { IndividualGateway() }

    val locationGateway by lazy { LocationGateway() }

    val locationHierarchyGateway by lazy { LocationHierarchyGateway() }
}