package com.mgtriffid.games.cotta.core.simulation.impl

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import jakarta.inject.Inject

class EntityOwnerSawTickProviderImpl @Inject constructor(
    private val playersSawTicks: PlayersSawTicks
): EntityOwnerSawTickProvider {
    override fun getSawTickByEntity(entity: Entity): Long? {
        return (entity.ownedBy as? Entity.OwnedBy.Player)?.let { playersSawTicks[it.playerId] }
    }
}