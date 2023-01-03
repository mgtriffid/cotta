package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.network.CottaNetwork
import com.mgtriffid.games.cotta.network.CottaServerNetwork

/**
 * Temporary class that hosts code which is yet to be settled somewhere. Let's call this "nowhere" and
 * put some code here, and then we'll sort it out later where to put it. For now we don't know what belongs
 * to server, what belongs to game class, network manager, etc etc., what belongs to Cotta framework, what
 * belongs to Panna game.
 */
class ServerCodePurgatory(
    private val network: CottaNetwork
) {
    lateinit var state: Any
    lateinit var serverNetwork: CottaServerNetwork

    fun getNonPlayerInput(): Any {
        TODO()
    }

    fun getPlayerInputs(): Any {
        return emptyMap<Any, Any>()
    }

    fun initializeNetwork() {
        serverNetwork = network.createServerNetwork()
    }

    fun sendDataToClients() {
        val data = getData()
    }

    private fun getData(): String { // TODO not String but some special thing
        return "keke hello data from server"
    }
}
