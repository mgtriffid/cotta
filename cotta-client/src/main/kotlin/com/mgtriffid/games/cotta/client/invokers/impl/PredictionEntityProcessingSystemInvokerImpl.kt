package com.mgtriffid.games.cotta.client.invokers.impl

import com.mgtriffid.games.cotta.client.impl.LocalPlayer
import com.mgtriffid.games.cotta.client.invokers.PredictionEntityProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.EntityProcessingSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PredictionEntityProcessingSystemInvokerImpl @Inject constructor(
    @Named("prediction") private val entities: Entities,
    private val localPlayer: LocalPlayer,
    private val context: PredictionEntityProcessingContext,
) : PredictionEntityProcessingSystemInvoker {
    override fun invoke(system: EntityProcessingSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        entities.all()
            .filter { it.shouldBePredicted() }
            .forEach { process(it, system) }
    }

    private fun Entity.shouldBePredicted() = ownedBy == Entity.OwnedBy.Player(localPlayer.playerId)

    private fun process(entity: Entity, system: EntityProcessingSystem) {
        logger.trace { "${system::class.simpleName} processing entity ${entity.id}" }
        system.process(entity, context)
    }
}
