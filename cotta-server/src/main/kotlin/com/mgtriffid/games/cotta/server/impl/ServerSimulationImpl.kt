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
import kotlin.reflect.full.primaryConstructor

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

    override fun <T : CottaSystem> registerSystem(systemClass: KClass<T>) {
        systemInvokers.add(createInvoker(systemClass))
    }

    override fun tick() {
        state.advance()
        for (invoker in systemInvokers) {
            invoker()
        }
        effectBus.clear()
    }

    private fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker {
        val ctor = systemClass.primaryConstructor ?: throw IllegalArgumentException(
            "Class ${systemClass.qualifiedName} must have a primary constructor"
        )
        val parameters = ctor.parameters
        val parameterValues = parameters.map {
            (it.type.classifier as? KClass<*>)?.let {
                when (it) {
                    EffectBus::class -> effectBus
                    CottaState::class -> state
                    else -> null
                }
            }
        }
        return SimpleSystemInvoker(ctor.call(*parameterValues.toTypedArray()) as CottaSystem, state, effectBus)
    }
}
