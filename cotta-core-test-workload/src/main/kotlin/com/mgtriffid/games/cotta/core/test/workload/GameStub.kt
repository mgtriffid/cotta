package com.mgtriffid.games.cotta.core.test.workload

import com.mgtriffid.games.cotta.core.annotations.Game
import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.PlayersHandler
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.systems.CottaSystem

@Game
object GameStub : CottaGame {
    override val systems = emptyList<CottaSystem>()
    override fun initializeServerState(entities: Entities) {
    }

    override fun initializeStaticState(entities: Entities) {
    }

    override val config: CottaConfig
        get() = TODO()
    override val playerInputKClass
        get() = TODO()

    override val inputProcessing = object : InputProcessing {
        override fun process(
            input: SimulationInput,
            entities: Entities,
            effectBus: EffectBus
        ) {
        }

        override fun processPlayerInput(
            playerId: PlayerId,
            input: PlayerInput,
            entities: Entities,
            effectBus: EffectBus
        ) {
        }
    }

    override val playersHandler = object : PlayersHandler {
        override fun onLeaveGame(playerId: PlayerId, entities: Entities) {
        }
    }
}
