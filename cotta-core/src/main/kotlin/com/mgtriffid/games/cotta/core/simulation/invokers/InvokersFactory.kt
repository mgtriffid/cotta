package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.systems.CottaSystem
import kotlin.reflect.KClass

// TODO consider having different factories as different interfaces,
//      for they are solving different problems
interface InvokersFactory {
    fun createInvoker(system: CottaSystem): Pair<SystemInvoker<*>, CottaSystem>
}
