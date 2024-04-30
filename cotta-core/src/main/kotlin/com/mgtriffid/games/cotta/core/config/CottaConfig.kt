package com.mgtriffid.games.cotta.core.config


interface CottaConfig {
    val tickLength: Long
    val network: NetworkConfig
        get() = object : NetworkConfig {
            override val ports = object : NetworkConfig.Ports {
                override val tcp = 16001
                override val udp = 16002
            }
            override val serverHost = "127.0.0.1"
        }
    val debugConfig: DebugConfig
        get() = object : DebugConfig {}
}

interface NetworkConfig {
    val ports: Ports
    val serverHost: String

    interface Ports {
        val tcp: Int
        val udp: Int
    }
}

interface DebugConfig {
    val emulatedNetworkConditions: EmulatedNetworkConditions
        get() = EmulatedNetworkConditions.Perfect
    val debugInformation: DebugInformation
        get() = object : DebugInformation {
            override val monitorTicks: Boolean
                get() = false
            override val monitorNetwork: Boolean
                get() = false
            override val showFps: Boolean
                get() = false
        }

    interface DebugInformation {
        val monitorTicks: Boolean
        val monitorNetwork: Boolean
        val showFps: Boolean
    }

    sealed interface EmulatedNetworkConditions {
        data object Perfect : EmulatedNetworkConditions
        interface WithIssues : EmulatedNetworkConditions {
            val sending: Issues
            val receiving: Issues

            interface Latency {
                val min: Long
                val max: Long
            }

            interface Issues {
                val latency: Latency
                val packetLoss: Double
            }
        }
    }
}
