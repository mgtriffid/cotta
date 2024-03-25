package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.PredictedEntityId
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks

/**
 * Plays a role of data source for server-to-client communication. Provides historical information about entities, their
 * states, ownership, players "who saw which tick" situations, effects, inputs for entities, etc.
 */
interface DataForClients {
    fun effects(tick: Long): Collection<CottaEffect>
    fun entities(tick: Long): Entities
    fun confirmedEntities(tick: Long): List<Pair<PredictedEntityId, AuthoritativeEntityId>>
    fun players(): Players
    fun playersSawTicks(): PlayersSawTicks
    fun playerInputs(): Map<PlayerId, PlayerInput>
}
