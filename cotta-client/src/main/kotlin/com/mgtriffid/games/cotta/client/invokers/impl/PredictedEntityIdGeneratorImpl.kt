package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.client.impl.PlayerIdHolder
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId
import jakarta.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

class PredictedEntityIdGeneratorImpl @Inject constructor(
    private val playerIdHolder: PlayerIdHolder
): PredictedEntityIdGenerator {
    private val nextId = AtomicInteger(0)
    override fun getId(): PredictedEntityId {
        return PredictedEntityId(playerIdHolder.playerId, nextId.incrementAndGet())
    }
}