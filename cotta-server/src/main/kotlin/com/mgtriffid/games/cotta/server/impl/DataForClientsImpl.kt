package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.server.DataForClients
import com.mgtriffid.games.cotta.server.PlayerId

data class DataForClientsImpl(
    val effectsHistory: EffectsHistory,
    val inputs: Map<EntityId, Set<InputComponent<*>>>,
    val state: CottaState,
    val metaEntities: Map<PlayerId, EntityId>
) : DataForClients {
    override fun effects(tick: Long): Collection<CottaEffect> {
        return effectsHistory.forTick(tick) // TODO care about tick
    }

    override fun inputs(tick: Long): Map<EntityId, Set<InputComponent<*>>> {
        return inputs // TODO care about tick
    }

    override fun entities(tick: Long): Entities {
        return state.entities(tick)
    }

    override fun metaEntities(): Map<PlayerId, EntityId> {
        return metaEntities
    }
}
