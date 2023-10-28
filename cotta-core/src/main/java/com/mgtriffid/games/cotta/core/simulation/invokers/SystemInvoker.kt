package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.systems.CottaSystem

interface SystemInvoker<in T: CottaSystem> {
    operator fun invoke(system: T)
}
