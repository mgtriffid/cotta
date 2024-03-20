package com.mgtriffid.games.cotta.server.workload

import com.mgtriffid.games.cotta.Game
import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import kotlin.reflect.KClass

@Game
class GameStub : CottaGame {
    override val serverSystems: List<KClass<*>> = emptyList()
    override val nonPlayerInputProvider = object : NonPlayerInputProvider {
        override fun input(entities: Entities) = object : NonPlayerInput {}
    }
    override val inputProcessing = object : InputProcessing {
        override fun process(
            input: SimulationInput,
            entities: Entities,
            effectBus: EffectBus
        ) {}
    }

    override fun initializeServerState(entities: Entities) {}
    override fun initializeStaticState(entities: Entities) {}

    override val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>> = emptySet()
    override val config: CottaConfig = object : CottaConfig {
        override val tickLength: Long = 20
    }
    override val playerInputKClass = PlayerInputStub::class
}

data class PlayerInputStub(val input: String) : PlayerInput
