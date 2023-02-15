package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.CottaClient
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CottaClientImpl : CottaClient {
    override fun tick() {
        logger.info { "Running ${CottaClientImpl::class.simpleName}" }
    }
}
