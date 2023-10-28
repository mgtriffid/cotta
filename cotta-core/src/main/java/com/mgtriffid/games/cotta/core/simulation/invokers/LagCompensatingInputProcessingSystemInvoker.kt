package com.mgtriffid.games.cotta.core.simulation.invokers

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.systems.CottaSystem
import com.mgtriffid.games.cotta.core.systems.InputProcessingSystem
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class LagCompensatingInputProcessingSystemInvoker(
    private val entities: Entities,
    private val entityOwnerSawTickProvider: EntityOwnerSawTickProvider,
    private val sawTickHolder: InvokersFactoryImpl.SawTickHolder
): SystemInvoker<InputProcessingSystem> {
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
