package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.server.impl.ClientsInput
import com.mgtriffid.games.cotta.server.impl.ClientsPredictedEntities

interface ClientsInputProvider {
    fun getInput(): Pair<ClientsInput, ClientsPredictedEntities>
}
