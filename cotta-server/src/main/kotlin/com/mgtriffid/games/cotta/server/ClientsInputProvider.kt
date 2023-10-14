package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.server.impl.ClientsInput

interface ClientsInputProvider {
    fun getInput(): ClientsInput
}
