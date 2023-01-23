package com.mgtriffid.games.cotta.server.impl

import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.server.ComponentDeltas
import com.mgtriffid.games.cotta.server.DataToBeSentToClients

data class DataToBeSentToClientsImpl(
    override val effects: Collection<CottaEffect>,
    override val inputs: Map<Int, Set<InputComponent<*>>>, // TODO better typing
    override val state: CottaState,
    override val deltas: ComponentDeltas
) : DataToBeSentToClients
