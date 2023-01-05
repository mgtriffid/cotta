package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.impl.ServerSimulationImpl
import kotlin.reflect.KClass

interface ServerSimulation {
    companion object {
        fun getInstance(): ServerSimulation = ServerSimulationImpl()
    }

    // TODO use DI instead. Of some kind.
    fun effectBus(): EffectBus

    /**
     * Should be called exactly once.
     */
    fun setState(state: CottaState)

    /**
     * Registers a system for execution. Systems are invoked in the order of registration.
     */
    fun <T: CottaSystem> registerSystem(systemClass: KClass<T>)

    fun tick()
    fun setEntityOwner(damageDealerId: Int, playerId: Int)
    fun setPlayerSawTick(playerId: Int, tick: Long)
}
