package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.entities.PlayerId
import kotlin.reflect.KClass

interface InvokersFactory {
    fun <T: CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker

    companion object {
        fun getInstance(
            lagCompensatingEffectBus: LagCompensatingEffectBus,
            state: CottaState,
            playersSawTicks: HashMap<PlayerId, Long>,
            tickProvider: TickProvider,
            sawTickHolder: InvokersFactoryImpl.SawTickHolder
        ): InvokersFactory = InvokersFactoryImpl(
            lagCompensatingEffectBus,
            state,
            playersSawTicks,
            tickProvider,
            sawTickHolder
        )
    }
}
