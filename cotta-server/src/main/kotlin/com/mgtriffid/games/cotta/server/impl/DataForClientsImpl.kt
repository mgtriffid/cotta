package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.server.ComponentDeltasProvider
import com.mgtriffid.games.cotta.server.DataForClients

data class DataForClientsImpl(
    val effects: Collection<CottaEffect>,
    val inputs: Map<Int, Set<InputComponent<*>>>
) : DataForClients {
    override fun effects(tick: Long): Collection<CottaEffect> {
        return effects // TODO care about tick
    }

    override fun inputs(tick: Long): Map<Int, Set<InputComponent<*>>> {
        return inputs // TODO care about tick
    }
}
