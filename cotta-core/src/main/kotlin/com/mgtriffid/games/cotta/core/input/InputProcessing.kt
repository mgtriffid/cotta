package com.mgtriffid.games.cotta.core.input

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Processes input for the simulation. By implementing this interface, you can
 * specify how data from [PlayerInput] and [NonPlayerInput] is passed to some
 * components of Entities.
 */
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
