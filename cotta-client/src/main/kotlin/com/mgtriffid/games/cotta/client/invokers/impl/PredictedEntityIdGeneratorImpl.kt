package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import jakarta.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

class PredictedEntityIdGeneratorImpl @Inject constructor(
    private val localPlayer: LocalPlayer
): PredictedEntityIdGenerator {
    private val nextId = AtomicInteger(0)
    override fun getId(): EntityId {
        return AuthoritativeEntityId(nextId.decrementAndGet())
    }
}
