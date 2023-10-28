package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId

/**
 * Plays a role of data source for server-to-client communication. Provides historical information about entities, their
 * states, ownership, players "who saw which tick" situations, effects, inputs for entities, etc.
 */
interface DataForClients {
    fun effects(tick: Long): Collection<CottaEffect>
    fun inputs(tick: Long): Map<EntityId, Collection<InputComponent<*>>>
    fun entities(tick: Long): Entities
    fun metaEntities(): MetaEntities
}
