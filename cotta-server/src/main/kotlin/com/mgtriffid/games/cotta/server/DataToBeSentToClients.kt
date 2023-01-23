package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.InputComponent

// TODO more specific requirements will arise during further development
interface DataToBeSentToClients {
    val effects: Collection<CottaEffect>
    val inputs: Map<Int, Set<InputComponent<*>>> // immutable
    val state: CottaState
    val deltas: ComponentDeltas
}
