package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.network.purgatory.EnterGameIntent
import com.mgtriffid.games.cotta.server.impl.ServerSimulationImpl
import kotlin.reflect.KClass

interface ServerSimulation {
    companion object {
        fun getInstance(
            tickProvider: TickProvider,
            historyLength: Int
        ): ServerSimulation = ServerSimulationImpl(
            tickProvider,
            historyLength
        )
    }

    // TODO use DI instead. Of some kind. Maybe.
    fun effectBus(): EffectBus

    /**
     * Should be called exactly once. TODO reconsider how this is configured.
     */
    fun setState(state: CottaState)

    /**
     * Registers a system for execution. Systems are invoked in the order of registration.
     */
    fun <T : CottaSystem> registerSystem(systemClass: KClass<T>)

    fun setMetaEntitiesInputComponents(components: Set<KClass<out InputComponent<*>>>)

    fun tick()
    fun setPlayerSawTick(playerId: PlayerId, tick: Long)
    fun enterGame(intent: EnterGameIntent): PlayerId
    fun getDataToBeSentToClients(): DataForClients
    fun setInputForUpcomingTick(input: IncomingInput)
}
