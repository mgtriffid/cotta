package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.server.ComponentDeltas
import com.mgtriffid.games.cotta.server.ComponentDeltasProvider

class ComponentDeltasProviderCalculatedOnTheFly(val state: CottaState) : ComponentDeltasProvider {
    override fun atTick(tick: Long): ComponentDeltas {
        TODO()
    }
}
