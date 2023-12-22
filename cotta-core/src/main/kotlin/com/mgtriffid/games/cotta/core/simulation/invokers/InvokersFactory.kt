package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

// TODO consider having different factories as different interfaces,
//      for they are solving different problems
interface InvokersFactory {
    fun <T: CottaSystem> createInvoker(systemClass: KClass<T>): Pair<SystemInvoker<*>, CottaSystem>
}
