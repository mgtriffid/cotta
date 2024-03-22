package com.mgtriffid.games.panna.shared.game.systems

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.invokers.context.NewInputProcessingSystemContext
import com.mgtriffid.games.cotta.core.systems.NewInputProcessingSystem
import com.mgtriffid.games.panna.shared.game.components.SteamManPlayerComponent

class PannaNewInputProcessingSystem : NewInputProcessingSystem {
    fun process(ctx: NewInputProcessingSystemContext, input: SimulationInput) {
        ctx.entities().all().filter {
            it.hasComponent(SteamManPlayerComponent::class)
        }.forEach { entity ->
            val player = (entity.ownedBy as Entity.OwnedBy.Player).playerId
            val playerInput = input.inputForPlayers()[player]
            if (playerInput != null) {

            }


        }


        // get inputs from... inputs
        // get entities associated with players
        //      is that a singleton global state thing?
        // put data into those entities
    }
}
