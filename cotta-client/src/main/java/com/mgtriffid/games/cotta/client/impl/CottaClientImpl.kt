package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClient
import com.mgtriffid.games.cotta.network.CottaClientNetwork
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CottaClientImpl(val network: CottaClientNetwork) : CottaClient {
    var connected = false

    override fun tick() {
        logger.info { "Running ${CottaClientImpl::class.simpleName}" }
        if (!connected) connect()
    }

    private fun connect() {
        network.initialize()
        network.sendEnterGameIntent()
        connected = true
    }
}
