package com.mgtriffid.games.cotta.core.serialization

import com.mgtriffid.games.cotta.core.registry.ComponentKey

interface EntitySnapshot {
    val id: Int
    val components: Map<ComponentKey, FullComponentData<*>>
}
