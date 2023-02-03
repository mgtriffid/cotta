package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.PlayerId
import com.mgtriffid.games.cotta.server.impl.EffectsHistory
import kotlin.reflect.KClass

interface InvokersFactory {
    fun <T: CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker

    companion object {
        fun getInstance(
            lagCompensatingEffectBus: LagCompensatingEffectBus,
            state: CottaState,
            entityOwners: HashMap<Int, PlayerId>,
            playersSawTicks: HashMap<PlayerId, Long>,
            tickProvider: TickProvider,
            sawTickHolder: InvokersFactoryImpl.SawTickHolder
        ): InvokersFactory = InvokersFactoryImpl(
            lagCompensatingEffectBus,
            state,
            entityOwners,
            playersSawTicks,
            tickProvider,
            sawTickHolder
        )
    }
}
