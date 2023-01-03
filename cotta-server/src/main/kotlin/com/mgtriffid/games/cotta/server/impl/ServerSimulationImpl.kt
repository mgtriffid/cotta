package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingCottaSystem
import com.mgtriffid.games.cotta.server.ServerSimulation

class ServerSimulationImpl: ServerSimulation {
    private val systems = ArrayList<CottaSystem>()

    private val effectBus = EffectBus.getInstance()

    private lateinit var state: CottaState

    override fun effectBus(): EffectBus {
        return effectBus
    }

    override fun setState(state: CottaState) {
        this.state = state
    }

    override fun registerSystem(system: CottaSystem) {
        systems.add(system)
    }

    override fun tick() {
        for (system in systems) {
            if (system is EntityProcessingCottaSystem) {
                for (entity in state.entities().all()) {
                    system.update(entity)
                }
            }
            if (system is EffectsConsumer) {
                effectBus.effects().forEach {
                    system.handleEffect(it)
                }
            }
        }
        state.advance()
        effectBus.clear()
    }
}
