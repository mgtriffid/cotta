package com.mgtriffid.games.cotta.core.input

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.simulation.SimulationInput

interface InputProcessing {
    fun process(
        input: SimulationInput,
        entities: Entities,
        effectBus: EffectBus
    )
}
