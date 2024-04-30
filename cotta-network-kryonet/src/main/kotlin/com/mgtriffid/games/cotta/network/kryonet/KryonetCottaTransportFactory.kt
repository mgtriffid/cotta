package com.mgtriffid.games.cotta.network.kryonet

import com.codahale.metrics.MetricRegistry
import com.mgtriffid.games.cotta.core.config.DebugConfig
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.Perfect
import com.mgtriffid.games.cotta.core.config.DebugConfig.EmulatedNetworkConditions.WithIssues
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.acking.AckingCottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.acking.AckingCottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.client.LaggingSaver
import com.mgtriffid.games.cotta.network.kryonet.client.LaggingSender
import com.mgtriffid.games.cotta.network.kryonet.client.SimpleSaver
import com.mgtriffid.games.cotta.network.kryonet.client.SimpleSender

class KryonetCottaTransportFactory(private val metricRegistry: MetricRegistry) {
    fun createClient(
        emulatedNetworkConditions: DebugConfig.EmulatedNetworkConditions,
        tcpPort: Int,
        udpPort: Int,
        serverHost: String
    ): CottaClientNetworkTransport {
        return when (emulatedNetworkConditions) {
            Perfect -> {
                AckingCottaClientNetworkTransport(
                    SimpleSender(),
                    SimpleSaver(),
                    tcpPort,
                    udpPort,
                    serverHost,
                    metricRegistry
                )
            }
            is WithIssues -> {
                AckingCottaClientNetworkTransport(
                    sender = LaggingSender(
                        issues = emulatedNetworkConditions.sending,
                        impl = SimpleSender()
                    ),
                    saver = LaggingSaver(emulatedNetworkConditions.receiving, SimpleSaver()),
                    tcpPort = tcpPort,
                    udpPort = udpPort,
                    serverHost = serverHost,
                    metricRegistry = metricRegistry
                )
            }
        }
    }
}
