package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.EffectsConsumer
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.EntityProcessingCottaSystem
import com.mgtriffid.games.cotta.server.ServerSimulation
import com.mgtriffid.games.cotta.server.impl.invokers.SimpleSystemInvoker
import com.mgtriffid.games.cotta.server.impl.invokers.SystemInvoker
import kotlin.reflect.KClass

class ServerSimulationImpl: ServerSimulation {
    private val systemInvokers = ArrayList<SystemInvoker>()
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
        systemInvokers.add(SimpleSystemInvoker(system, state, effectBus))
    }

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {

    }

    override fun tick() {
        state.advance()
        for (invoker in systemInvokers) {
            invoker()
        }
        effectBus.clear()
    }
}
