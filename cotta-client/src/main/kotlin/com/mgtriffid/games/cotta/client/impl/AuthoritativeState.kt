package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider

sealed interface AuthoritativeState {
    class Ready(val apply: (CottaState, TickProvider) -> Unit) : AuthoritativeState
    data object NotReady : AuthoritativeState
}
