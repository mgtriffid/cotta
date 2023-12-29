package com.mgtriffid.games.cotta.core.simulation.invokers.context.impl

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.tracing.CottaTrace

class ServerCreatedEntitiesRegistry {
    var data: MutableList<Pair<CottaTrace, EntityId>> = ArrayList()
    operator fun get(trace: CottaTrace): EntityId {
        return data.indexOfFirst { it.first == trace }
            .takeIf { it >= 0 }
            ?.let { data.removeAt(it) }
            ?.second
            ?: run {
                throw IllegalStateException("No entity created for $trace")
            }
    }
}
