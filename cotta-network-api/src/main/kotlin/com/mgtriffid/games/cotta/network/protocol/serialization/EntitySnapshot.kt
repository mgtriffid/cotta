package com.mgtriffid.games.cotta.network.protocol.serialization

interface EntitySnapshot {
    val id: Int
    val components: Map<ComponentKey, FullComponentData<*>>
}
