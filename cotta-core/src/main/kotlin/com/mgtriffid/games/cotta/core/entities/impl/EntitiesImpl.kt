package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import java.util.concurrent.atomic.AtomicInteger

class EntitiesImpl(private val componentRegistry: ComponentRegistry) : EntitiesInternal {
    private var idGenerator = AtomicInteger()
    private val dynamic = HashMap<EntityId, Entity>()
    private var static = HashMap<EntityId, Entity>()

    override fun create(ownedBy: Entity.OwnedBy): Entity {
        return EntityImpl(componentRegistry, EntityId(idGenerator.incrementAndGet()), ownedBy).also { dynamic[it.id] = it }
    }

    override fun create(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        return EntityImpl(componentRegistry, id, ownedBy).also { dynamic[id] = it }
    }

    override fun currentId(): Int {
        return idGenerator.get()
    }

    override fun setIdGenerator(idSequence: Int) {
        idGenerator.set(idSequence)
    }

    override fun get(id: EntityId): Entity? {
        return dynamic[id] ?: static[id]
    }

    override fun all(): Collection<Entity> {
        return dynamic.values + static.values
    }

    override fun dynamic(): Collection<Entity> {
        return dynamic.values
    }

    override fun remove(id: EntityId) {
        dynamic.remove(id)
    }

    override fun deepCopy(): EntitiesImpl {
        val ret = EntitiesImpl(componentRegistry)
        ret.idGenerator = AtomicInteger(idGenerator.get())
        dynamic.forEach { (id: EntityId, entity: Entity) ->
            ret.dynamic[id] = (entity as EntityImpl).deepCopy()
        }
        ret.static = static
        return ret
    }
}
