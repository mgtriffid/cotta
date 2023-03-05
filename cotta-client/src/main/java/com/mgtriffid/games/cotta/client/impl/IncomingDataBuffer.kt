package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.core.serialization.Delta
import com.mgtriffid.games.cotta.core.serialization.ServerToClientGameDataPiece
import com.mgtriffid.games.cotta.core.serialization.StateSnapshot
import mu.KotlinLogging
import java.util.*
import kotlin.math.min

private val logger = KotlinLogging.logger {}

class IncomingDataBuffer {
    private val states = TreeMap<Long, StateSnapshot>()
    private val deltas = TreeMap<Long, Delta>()
    fun store(serverToClientGameDataPiece: ServerToClientGameDataPiece) {
        when (serverToClientGameDataPiece) {
            is ServerToClientGameDataPiece.StatePiece -> storeStatePiece(serverToClientGameDataPiece)
            is ServerToClientGameDataPiece.DeltaPiece -> storeDeltaPiece(serverToClientGameDataPiece)
        }
    }

    private fun storeDeltaPiece(serverToClientGameDataPiece: ServerToClientGameDataPiece.DeltaPiece) {
        deltas[serverToClientGameDataPiece.tick] = serverToClientGameDataPiece.delta
        cleanUpOldDeltas(serverToClientGameDataPiece.tick)
    }

    private fun cleanUpOldDeltas(tick: Long) {
        logger.debug { "Cleaning deltas" }
        cleanUp(deltas, tick)
        logger.debug { "Cleaning deltas completed" }
    }

    private fun storeStatePiece(serverToClientGameDataPiece: ServerToClientGameDataPiece.StatePiece) {
        states[serverToClientGameDataPiece.tick] = serverToClientGameDataPiece.stateSnapshot
        cleanUpOldStates(serverToClientGameDataPiece.tick)
    }

    private fun cleanUpOldStates(tick: Long) {
        logger.debug { "Cleaning states" }
        cleanUp(states, tick)
        logger.debug { "Cleaning states completed" }
    }

    private fun cleanUp(data: TreeMap<Long, *>, tick: Long) {
        data.navigableKeySet().subSet(min(data.firstKey(), tick - 128), tick - 128).toList().forEach {
            logger.debug { "Removing data for tick $it" }
            data.remove(it)
        }
    }
}
