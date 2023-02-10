package com.mgtriffid.games.cotta.network.protocol.serialization

sealed class ServerToClientGameDataPacket {
    abstract val tick: Long
    data class DeltaPacket(val delta: Delta, override val tick: Long): ServerToClientGameDataPacket()
    data class StatePacket(val stateSnapshot: StateSnapshot, override val tick: Long): ServerToClientGameDataPacket()
}
