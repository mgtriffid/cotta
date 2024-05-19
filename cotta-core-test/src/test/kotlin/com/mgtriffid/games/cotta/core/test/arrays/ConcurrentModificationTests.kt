package com.mgtriffid.games.cotta.core.test.arrays

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.test.workload.components.SimpleComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createAnotherComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createSimpleComponent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConcurrentModificationTests : ArraysEcsTest() {

    /**
     * id       | 1 | 2 | 3 | 4 | 5 |
     * simple   | x | x | x | x |   |
     * another  | x | x | x | x | x |
     *
     * We iterate over Simple, remove entity 3 when processing 1.
     * And we expect 1, 2, 3, 4 to be processed. Exactly those.
     */
    @Test
    fun `should be possible to remove Entity whie iterating`() {
        val entity1 = state.createEntity()
        entity1.addComponent(createSimpleComponent(42))
        entity1.addComponent(createAnotherComponent(42.0))
        val entity2 = state.createEntity()
        entity2.addComponent(createSimpleComponent(43))
        entity2.addComponent(createAnotherComponent(43.0))
        val entity3 = state.createEntity()
        entity3.addComponent(createSimpleComponent(44))
        entity3.addComponent(createAnotherComponent(44.0))
        val entity4 = state.createEntity()
        entity4.addComponent(createSimpleComponent(45))
        entity4.addComponent(createAnotherComponent(45.0))
        val entity5 = state.createEntity()
        entity5.addComponent(createAnotherComponent(46.0))

        val processedEntities = mutableSetOf<EntityId>()

        state.queryAndExecute(SimpleComponent::class) { id, _ ->
            processedEntities.add(id)
            if (id == entity1.id) {
                state.removeEntity(entity3.id)
            }
        }

        assertEquals(setOf(entity1.id, entity2.id, entity3.id, entity4.id), processedEntities)

        assertNull(state.getEntity(entity3.id))
    }

    /**
     * id       | 1 | 2 | 3 | 4 | 5 |
     * simple   | x | x | x | x |   |
     * another  | x | x | x | x | x |
     *
     * We iterate over Simple, remove Simple from 2.
     * And we expect 1, 2, 3, 4 to be processed. Exactly those.
     */
    @Test
    fun `should remove component while iterating just fine`() {
        val entity1 = state.createEntity()
        entity1.addComponent(createSimpleComponent(42))
        entity1.addComponent(createAnotherComponent(42.0))
        val entity2 = state.createEntity()
        entity2.addComponent(createSimpleComponent(43))
        entity2.addComponent(createAnotherComponent(43.0))
        val entity3 = state.createEntity()
        entity3.addComponent(createSimpleComponent(44))
        entity3.addComponent(createAnotherComponent(44.0))
        val entity4 = state.createEntity()
        entity4.addComponent(createSimpleComponent(45))
        entity4.addComponent(createAnotherComponent(45.0))
        val entity5 = state.createEntity()
        entity5.addComponent(createAnotherComponent(46.0))

        val processedValues = mutableSetOf<Int>()

        state.queryAndExecute(SimpleComponent::class) { eId, simple ->
            processedValues.add((simple as SimpleComponent).value)
            if (simple.value == 43) {
                state.getEntity(entity3.id)?.removeComponent(SimpleComponent::class)
            }
        }

        assertEquals(setOf(42, 43, 44, 45), processedValues)
        assertFalse(entity3.hasComponent(SimpleComponent::class))
    }
}
