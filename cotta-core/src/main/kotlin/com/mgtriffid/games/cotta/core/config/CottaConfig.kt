package com.mgtriffid.games.cotta.core.config


interface CottaConfig {
    val tickLength: Long
    val debugConfig: DebugConfig
        get() = object : DebugConfig { }
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
