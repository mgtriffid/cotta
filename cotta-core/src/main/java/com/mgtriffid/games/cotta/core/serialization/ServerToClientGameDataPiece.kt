package com.mgtriffid.games.cotta.core.serialization

sealed class ServerToClientGameDataPiece {
    abstract val tick: Long
    // TODO no reason to have tick in DeltaPiece and Delta
    data class DeltaPiece(override val tick: Long, val delta: Delta): ServerToClientGameDataPiece()
    data class StatePiece(override val tick: Long, val stateSnapshot: StateSnapshot): ServerToClientGameDataPiece()
}
