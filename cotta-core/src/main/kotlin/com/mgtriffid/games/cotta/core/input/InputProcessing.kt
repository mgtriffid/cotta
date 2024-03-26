package com.mgtriffid.games.cotta.core.input

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

interface InputProcessing {
    fun process(
        input: SimulationInput,
        entities: Entities,
        effectBus: EffectBus
    ) {
        input.inputForPlayers().forEach { (playerId, playerInput) ->
            processPlayerInput(playerId, playerInput, entities, effectBus)
        }
        processNonPlayerInput(input.nonPlayerInput(), entities, effectBus)
    }

    fun processPlayerInput(
        playerId: PlayerId,
        input: PlayerInput,
        entities: Entities,
        effectBus: EffectBus
    )

    fun processNonPlayerInput(
        input: NonPlayerInput,
        entities: Entities,
        effectBus: EffectBus
    ) = Unit
}
