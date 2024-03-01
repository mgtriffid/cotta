package com.mgtriffid.games.cotta.client.impl

import com.mgtriffid.games.cotta.client.network.NetworkClient
import jakarta.inject.Inject

class DeltasPresentImpl @Inject constructor(
    private val network: NetworkClient
) : DeltasPresent {
    override fun hasDelta(tick: Long): Boolean {
        return network.deltaAvailable(tick)
    }
}
