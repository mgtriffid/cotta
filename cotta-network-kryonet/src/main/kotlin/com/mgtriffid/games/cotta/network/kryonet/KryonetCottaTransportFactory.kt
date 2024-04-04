package com.mgtriffid.games.cotta.network.kryonet

import com.mgtriffid.games.cotta.core.config.DebugConfig
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.Perfect
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.acking.AckingCottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.acking.AckingCottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.client.KryonetCottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.client.LaggingSaver
import com.mgtriffid.games.cotta.network.kryonet.client.LaggingSender
import com.mgtriffid.games.cotta.network.kryonet.client.SimpleSaver
import com.mgtriffid.games.cotta.network.kryonet.client.SimpleSender
import com.mgtriffid.games.cotta.network.kryonet.server.KryonetCottaServerNetworkTransport

class KryonetCottaTransportFactory {
    fun createClient(emulatedNetworkConditions: DebugConfig.EmulatedNetworkConditions): CottaClientNetworkTransport {
        return when (emulatedNetworkConditions) {
            Perfect -> {
                AckingCottaClientNetworkTransport(SimpleSender(), SimpleSaver())
            }
            is WithIssues -> {
                AckingCottaClientNetworkTransport(
                    LaggingSender(emulatedNetworkConditions.sending, SimpleSender()),
                    LaggingSaver(emulatedNetworkConditions.receiving, SimpleSaver())
                )
            }
        }
    }

    fun createServer() : CottaServerNetworkTransport {
        return AckingCottaServerNetworkTransport()
    }
}
