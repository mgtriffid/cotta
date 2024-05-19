package com.mgtriffid.games.cotta.core.entities.arrays

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import java.util.concurrent.atomic.AtomicInteger

internal class ArraysEntities(
    private val entities: DynamicEntitiesStorage,
    private val componentsStorage: ComponentsStorage,
    private val componentRegistry: ComponentRegistry
) {
    private var idGenerator = AtomicInteger()

    fun createEntity(): ArraysEntity {
        val id = EntityId(idGenerator.incrementAndGet())
        entities.create(id.id)
        return ArraysEntity(
            componentRegistry,
            id,
            Entity.OwnedBy.System,
            entities,
            componentsStorage
        )
    }
}
