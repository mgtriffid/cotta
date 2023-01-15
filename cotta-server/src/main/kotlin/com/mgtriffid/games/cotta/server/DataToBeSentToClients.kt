package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.InputComponent

interface DataToBeSentToClients {
    val effects: Collection<CottaEffect>
    val inputs: Map<Int, Set<InputComponent<*>>>
}
