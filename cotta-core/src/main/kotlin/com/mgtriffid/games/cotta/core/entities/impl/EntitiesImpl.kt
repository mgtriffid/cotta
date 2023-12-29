package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.exceptions.EntityNotExistsException
import java.util.concurrent.atomic.AtomicInteger

class EntitiesImpl : Entities {
    private var idGenerator = AtomicInteger()
    private val dynamic = HashMap<EntityId, Entity>()
    private var static = HashMap<EntityId, Entity>()

    override fun create(ownedBy: Entity.OwnedBy): Entity {
        return EntityImpl(AuthoritativeEntityId(idGenerator.incrementAndGet()), ownedBy).also { dynamic[it.id] = it }
    }

    override fun create(id: EntityId, ownedBy: Entity.OwnedBy): Entity {
        return EntityImpl(id, ownedBy).also { dynamic[id] = it }
    }

    override fun createStatic(id: EntityId): Entity {
        return EntityImpl(id, Entity.OwnedBy.System).also { static[id] = it }
    }

    @Throws(EntityNotExistsException::class)
    override fun get(id: EntityId): Entity {
        return dynamic[id] ?: throw EntityNotExistsException("Entity $id does not exist")
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

    fun deepCopy(): EntitiesImpl {
        val ret = EntitiesImpl()
        ret.idGenerator = idGenerator
        dynamic.forEach { (id: EntityId, entity: Entity) ->
            ret.dynamic[id] = (entity as EntityImpl).deepCopy()
        }
        ret.static = static
        return ret
    }
}
