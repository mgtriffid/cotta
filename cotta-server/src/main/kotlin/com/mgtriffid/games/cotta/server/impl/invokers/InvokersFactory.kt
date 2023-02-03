package com.mgtriffid.games.cotta.server.impl.invokers

import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.server.PlayerId
import kotlin.reflect.KClass

interface InvokersFactory {
    fun <T: CottaSystem> createInvoker(systemClass: KClass<T>): SystemInvoker

    companion object {
        fun getInstance(
            effectBus: EffectBus,
            state: CottaState,
            entityOwners: HashMap<Int, PlayerId>,
            playersSawTicks: HashMap<PlayerId, Long>,
            tickProvider: TickProvider
        ): InvokersFactory = InvokersFactoryImpl(
            effectBus,
            state,
            entityOwners,
            playersSawTicks,
            tickProvider
        )
    }
}
