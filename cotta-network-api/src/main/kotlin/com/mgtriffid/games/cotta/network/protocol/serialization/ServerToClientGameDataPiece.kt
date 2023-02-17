package com.mgtriffid.games.cotta.network.protocol.serialization

sealed class ServerToClientGameDataPiece {
    abstract val tick: Long
    data class DeltaPiece(val delta: Delta, override val tick: Long): ServerToClientGameDataPiece()
    data class StatePiece(val stateSnapshot: StateSnapshot, override val tick: Long): ServerToClientGameDataPiece()
}
