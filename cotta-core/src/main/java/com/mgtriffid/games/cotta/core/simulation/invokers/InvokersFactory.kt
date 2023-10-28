package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import kotlin.reflect.KClass

interface InvokersFactory {
    fun <T: CottaSystem> createInvoker(systemClass: KClass<T>): Pair<SystemInvoker<*>, CottaSystem>

    companion object {
        fun getInstance(
            lagCompensatingEffectBus: LagCompensatingEffectBus,
            state: CottaState,
            playersSawTicks: PlayersSawTicks,
            sawTickHolder: InvokersFactoryImpl.SawTickHolder
        ): InvokersFactory = InvokersFactoryImpl(
            lagCompensatingEffectBus,
            state,
            playersSawTicks,
            sawTickHolder
        )
    }
}
