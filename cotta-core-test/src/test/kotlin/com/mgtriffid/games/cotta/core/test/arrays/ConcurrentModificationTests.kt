package com.mgtriffid.games.cotta.core.test.arrays

import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.test.workload.components.AnotherComponent
import com.mgtriffid.games.cotta.core.test.workload.components.SimpleComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createAnotherComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createSimpleComponent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.math.abs

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

        assertEquals(
            setOf(entity1.id, entity2.id, entity3.id, entity4.id),
            processedEntities
        )

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
                state.getEntity(entity3.id)
                    ?.removeComponent(SimpleComponent::class)
            }
        }

        assertEquals(setOf(42, 43, 44, 45), processedValues)
        assertFalse(entity3.hasComponent(SimpleComponent::class))
    }

    /**
     * id       | 1 | 2 | 3 | 4 | 5 | 6 |
     * simple   | x |   | x | x | x |   |
     * another  | x | x | x |   | x | x |
     */
    @Test
    fun `iteration over two with removal should actually remove`() {
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
        val entity5 = state.createEntity()
        entity5.addComponent(createSimpleComponent(48))
        entity5.addComponent(createAnotherComponent(49.0))
        val entity6 = state.createEntity()
        entity6.addComponent(createAnotherComponent(50.0))

        val processedEntities = mutableSetOf<EntityId>()
        state.queryAndExecute(
            SimpleComponent::class,
            AnotherComponent::class
        ) { id, _, _ ->
            processedEntities.add(id)
            if (id == entity3.id) {
                state.getEntity(id)!!.removeComponent(SimpleComponent::class)
            }
            if (id == entity5.id) {
                state.getEntity(id)!!.removeComponent(AnotherComponent::class)
            }
        }

        assertEquals(
            setOf(entity1.id, entity3.id, entity5.id),
            processedEntities
        )
        assertFalse(entity3.hasComponent(SimpleComponent::class))
        assertFalse(entity5.hasComponent(AnotherComponent::class))
        assertTrue(entity5.hasComponent(SimpleComponent::class))
        assertTrue(entity3.hasComponent(AnotherComponent::class))
    }

    /**
     * id       | 1  | 2  | 3  | 4  | 5  | 6  |
     * simple   | 10 | 20 | 30 | 40 | 50 | 60 |
     * another  | 18 | 19 | 20 | 21 | 22 | 23 |
     */
    @Test
    fun `nest iteration over entities inside of iteration over components`() {
        val entity1 = state.createEntity()
        entity1.addComponent(createSimpleComponent(10))
        entity1.addComponent(createAnotherComponent(18.0))
        val entity2 = state.createEntity()
        entity2.addComponent(createSimpleComponent(20))
        entity2.addComponent(createAnotherComponent(19.0))
        val entity3 = state.createEntity()
        entity3.addComponent(createSimpleComponent(30))
        entity3.addComponent(createAnotherComponent(20.0))
        val entity4 = state.createEntity()
        entity4.addComponent(createSimpleComponent(40))
        entity4.addComponent(createAnotherComponent(21.0))
        val entity5 = state.createEntity()
        entity5.addComponent(createSimpleComponent(50))
        entity5.addComponent(createAnotherComponent(22.0))
        val entity6 = state.createEntity()
        entity6.addComponent(createSimpleComponent(60))
        entity6.addComponent(createAnotherComponent(23.0))

        val processedEntities = mutableSetOf<EntityId>()

        state.queryAndExecute(SimpleComponent::class) { id, simple ->
            processedEntities.add(id)
            simple as SimpleComponent
            state.queryAndExecute(AnotherComponent::class) { nestedId, another ->
                another as AnotherComponent
                if (abs(simple.value - another.value) < 1.5) {
                    state.removeEntity(nestedId)
                }
            }
        }

        assertNull(state.getEntity(entity2.id))
        assertNull(state.getEntity(entity3.id))
        assertNull(state.getEntity(entity4.id))
        assertNotNull(state.getEntity(entity1.id))
        assertNotNull(state.getEntity(entity5.id))
        assertNotNull(state.getEntity(entity6.id))

        assertEquals(
            10,
            state.getEntity(entity1.id)!!
                .getComponent(SimpleComponent::class).value
        )
        assertEquals(
            18.0,
            state.getEntity(entity1.id)!!
                .getComponent(AnotherComponent::class).value
        )
        assertEquals(
            50,
            state.getEntity(entity5.id)!!
                .getComponent(SimpleComponent::class).value
        )
        assertEquals(
            22.0,
            state.getEntity(entity5.id)!!
                .getComponent(AnotherComponent::class).value
        )
        assertEquals(
            60,
            state.getEntity(entity6.id)!!
                .getComponent(SimpleComponent::class).value
        )
        assertEquals(
            23.0,
            state.getEntity(entity6.id)!!
                .getComponent(AnotherComponent::class).value
        )

        assertEquals(
            setOf(
                entity1.id,
                entity2.id,
                entity3.id,
                entity4.id,
                entity5.id,
                entity6.id
            ), processedEntities
        )
    }
}
