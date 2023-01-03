package com.mgtriffid.games.cotta.core.entities.impl

import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.exceptions.EntityNotExistsException
import java.util.concurrent.atomic.AtomicInteger

class EntitiesImpl : Entities {
    private var idGenerator = AtomicInteger()
    private val entities = HashMap<Int, Entity>()

    override fun createEntity(): Entity {
        return EntityImpl(idGenerator.incrementAndGet()).also { entities[it.id] = it }
    }

    @Throws(EntityNotExistsException::class)
    override fun get(id: Int): Entity {
        return entities[id] ?: throw EntityNotExistsException("Entity $id does not exist")
    }

    fun deepCopy(): EntitiesImpl {
        val ret = EntitiesImpl()
        ret.idGenerator = idGenerator
        entities.forEach { (id: Int, entity: Entity) ->
            ret.entities[id] = (entity as EntityImpl).deepCopy()
        }
        return ret
    }
}
