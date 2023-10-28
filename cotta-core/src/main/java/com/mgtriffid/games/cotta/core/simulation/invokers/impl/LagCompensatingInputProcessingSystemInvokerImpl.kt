package com.mgtriffid.games.cotta.core.simulation.invokers.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.simulation.invokers.InvokersFactoryImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.LagCompensatingInputProcessingSystemInvoker
import com.mgtriffid.games.cotta.core.simulation.invokers.SystemInvoker
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import jakarta.inject.Inject
import jakarta.inject.Named
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingInputProcessingSystemInvokerImpl @Inject constructor(
        @Named("latest") private val entities: Entities,
        private val entityOwnerSawTickProvider: EntityOwnerSawTickProvider,
        private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
): LagCompensatingInputProcessingSystemInvoker {
    override fun invoke(system: InputProcessingSystem) {
        logger.debug { "Invoked ${system::class.qualifiedName}" }
        entities.all().forEach { process(it, system) }
    }

    private fun process(entity: Entity, system: InputProcessingSystem) {
        logger.debug { "${system::class.simpleName} processing entity ${entity.id}" }
        sawTickHolder.tick = entityOwnerSawTickProvider.getSawTickByEntity(entity)
        system.process(entity)
        sawTickHolder.tick = null
    }

    interface EntityOwnerSawTickProvider {
        fun getSawTickByEntity(entity: Entity): Long?
    }
}