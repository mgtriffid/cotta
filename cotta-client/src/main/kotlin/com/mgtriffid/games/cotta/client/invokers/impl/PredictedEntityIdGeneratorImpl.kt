package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.core.entities.PredictedEntityId
import jakarta.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

class PredictedEntityIdGeneratorImpl @Inject constructor(
    private val localPlayer: LocalPlayer
): PredictedEntityIdGenerator {
    private val nextId = AtomicInteger(0)
    override fun getId(): PredictedEntityId {
        return PredictedEntityId(localPlayer.playerId, nextId.incrementAndGet())
    }
}