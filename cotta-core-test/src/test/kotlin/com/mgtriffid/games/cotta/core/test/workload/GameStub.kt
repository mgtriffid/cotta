package com.mgtriffid.games.cotta.core.test.workload

import com.mgtriffid.games.cotta.Game
import com.mgtriffid.games.cotta.core.config.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import kotlin.reflect.KClass

@Game
object GameStub : CottaGame {
    override val serverSystems: List<KClass<*>> = emptyList()
    override val nonPlayerInputProvider: NonPlayerInputProvider = TODO()
    override fun initializeServerState(entities: Entities) {
    }

    override fun initializeStaticState(entities: Entities) {
    }

    override val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>> =
        emptySet()
    override val config: CottaConfig = TODO()
    override val playerInputKClass = TODO()

    override val inputProcessing = object : InputProcessing {
        override fun process(
            input: SimulationInput,
            entities: Entities,
            effectBus: EffectBus
        ) {
        }
    }
}
