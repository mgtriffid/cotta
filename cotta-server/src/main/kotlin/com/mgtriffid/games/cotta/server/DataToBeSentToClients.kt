package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.CottaEffect

interface DataToBeSentToClients {
    val effects: Collection<CottaEffect>
}
