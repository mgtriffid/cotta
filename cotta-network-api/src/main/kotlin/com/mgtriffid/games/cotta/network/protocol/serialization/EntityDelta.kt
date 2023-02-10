package com.mgtriffid.games.cotta.network.protocol.serialization

interface EntityDelta {
    /*
    val addedComponents: Map<ComponentKey, FullComponentData>
    val removedComponents: Set<ComponentKey>
    val changedComponents: Map<ComponentKey, ComponentDelta>
     */
    val data: Map<ComponentKey, ComponentDelta<*>>
}

interface ComponentKey
