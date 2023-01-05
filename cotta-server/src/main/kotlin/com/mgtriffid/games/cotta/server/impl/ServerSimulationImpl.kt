package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.ServerSimulation
import com.mgtriffid.games.cotta.server.impl.invokers.InvokersFactory
import com.mgtriffid.games.cotta.server.impl.invokers.SystemInvoker
import kotlin.reflect.KClass

class ServerSimulationImpl: ServerSimulation {
    private val systemInvokers = ArrayList<SystemInvoker>()

    private val entityOwners = HashMap<Int, Int>()
    private val playersSawTicks = HashMap<Int, Long>()

    private val effectBus = EffectBus.getInstance()

    private lateinit var state: CottaState
    private lateinit var invokersFactory: InvokersFactory

    override fun effectBus(): EffectBus {
        return effectBus
    }

    override fun setState(state: CottaState) {
        this.state = state
        // TODO decouple
        this.invokersFactory = InvokersFactory.getInstance(
            effectBus,
            state,
            entityOwners,
            playersSawTicks
        )
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

    override fun setEntityOwner(damageDealerId: Int, playerId: Int) {
        entityOwners[damageDealerId] = playerId
    }

    override fun setPlayerSawTick(playerId: Int, tick: Long) {
        playersSawTicks[playerId] = tick
    }

    private fun <T : CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker {
        return invokersFactory.createInvoker(systemClass)
    }
}
