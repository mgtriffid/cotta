package com.mgtriffid.games.cotta.core.test.arrays

import com.esotericsoftware.kryo.Kryo
import com.mgtriffid.games.cotta.core.entities.arrays.ArraysBasedState
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.registry.impl.ComponentRegistryImpl
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.test.workload.GameStub
import com.mgtriffid.games.cotta.core.test.workload.components.AnotherComponent
import com.mgtriffid.games.cotta.core.test.workload.components.SimpleComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createAnotherComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createSimpleComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BasicQueryTests : ArraysEcsTest() {

    @Test
    fun `iteration should return N entities with a single component`() {
        val entity1 = state.createEntity()
        state.createEntity()
        val entity3 = state.createEntity()
        entity1.addComponent(createSimpleComponent(42))
        entity3.addComponent(createSimpleComponent(43))

        val processedEntities = mutableSetOf<EntityId>()
        state.queryAndExecute(SimpleComponent::class) { id, _ ->
            processedEntities.add(id)
        }

        assertEquals(setOf(entity1.id, entity3.id), processedEntities)
    }

    /**
     * id       | 1 | 2 | 3 | 4 |
     * simple   | x |   | x |   |
     * another  | x |   | x | x |
     */
    @Test
    fun `iteration over two components should return only those having both`() {
        val entity1 = state.createEntity()
        entity1.addComponent(createSimpleComponent(42))
        entity1.addComponent(createAnotherComponent(43.0))
        val entity2 = state.createEntity()
        entity2.addComponent(createAnotherComponent(44.0))
        val entity3 = state.createEntity()
        entity3.addComponent(createSimpleComponent(45))
        entity3.addComponent(createAnotherComponent(46.0))
        val entity4 = state.createEntity()
        entity4.addComponent(createSimpleComponent(47))

        val processedEntities = mutableSetOf<EntityId>()
        state.queryAndExecute(SimpleComponent::class, AnotherComponent::class) { id, _, _ ->
            processedEntities.add(id)
        }
        assertEquals(setOf(entity1.id, entity3.id), processedEntities)
    }
}
