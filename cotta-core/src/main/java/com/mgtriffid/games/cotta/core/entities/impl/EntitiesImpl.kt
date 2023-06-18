package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.exceptions.EntityNotExistsException
import java.util.concurrent.atomic.AtomicInteger

class EntitiesImpl : Entities {
    private var idGenerator = AtomicInteger()
    private val entities = HashMap<EntityId, Entity>()

    override fun createEntity(ownedBy: Entity.OwnedBy): Entity {
        return EntityImpl(AuthoritativeEntityId(idGenerator.incrementAndGet()), ownedBy).also { entities[it.id] = it }
    }

    override fun createEntity(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        return EntityImpl(id, ownedBy).also { entities[id] = it }
    }

    @Throws(EntityNotExistsException::class)
    override fun get(id: EntityId): Entity {
        return entities[id] ?: throw EntityNotExistsException("Entity $id does not exist")
    }

    override fun all(): Collection<Entity> {
        return entities.values
    }

    override fun remove(id: EntityId) {
        entities.remove(id)
    }

    fun deepCopy(): EntitiesImpl {
        val ret = EntitiesImpl()
        ret.idGenerator = idGenerator
        entities.forEach { (id: EntityId, entity: Entity) ->
            ret.entities[id] = (entity as EntityImpl).deepCopy()
        }
        return ret
    }
}
