package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import jakarta.inject.Inject

interface Deltas {
    fun get(tick: Long): Delta
}

class DeltasImpl @Inject constructor(
    private val networkClient: NetworkClient
): Deltas {
    override fun get(tick: Long): Delta {
        return networkClient.tryGetDelta(tick) ?: TODO("Not yet implemented")
    }
}
