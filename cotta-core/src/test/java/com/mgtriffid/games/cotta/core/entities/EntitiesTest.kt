package com.mgtriffid.games.cotta.core.entities

import com.mgtriffid.games.cotta.core.entities.workload.components.PositionTestComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EntitiesTest {

    @Test
    fun `should return hasComponent of true after component added`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(0, 0))

        assertTrue(entity.hasComponent(PositionTestComponent::class))
    }

    @Test
    fun `should return component with correct values after added`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(1, 1))

        assertEquals(1, entity.getComponent(PositionTestComponent::class).x)
    }

    @Test
    fun `should say hasComponent is false if it was removed`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(0, 0))
        entity.removeComponent(PositionTestComponent::class)

        assertFalse(entity.hasComponent(PositionTestComponent::class))
    }

    @Test
    fun `cottaState should use the same Entities if called repeatedly`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(1, 1))
        val entityId = entity.id

        assertEquals(
            1,
            cottaState.entities().get(entityId).getComponent(PositionTestComponent::class).x
        )
    }

    @Test
    fun `should have the same value for components after advancing a tick`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(1, 1))
        val entityId = entity.id

        cottaState.advance()

        assertEquals(
            1,
            cottaState.entities().get(entityId).getComponent(PositionTestComponent::class).x
        )
    }

    @Test
    fun `should remember previous value after advancing`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(1, 1))
        val entityId = entity.id
        val tick = cottaState.currentTick()

        cottaState.advance()

        assertEquals(
            1,
            cottaState.entities(tick).get(entityId).getComponent(PositionTestComponent::class).x
        )
    }

    @Test
    fun `should remember previous value after advancing and altering value`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(1, 1))
        val entityId = entity.id
        val tick = cottaState.currentTick()

        cottaState.advance()

        cottaState.entities().get(entityId).getComponent(PositionTestComponent::class).x = 2

        assertEquals(
            1,
            cottaState.entities(tick).get(entityId).getComponent(PositionTestComponent::class).x
        )
    }

    @Test
    fun `should be possible to advance like 100 times`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.createEntity()
        entity.addComponent(PositionTestComponent.create(1, 1))
        val entityId = entity.id

        repeat(100) {
            cottaState.advance()
        }

        assertEquals(
            1,
            cottaState.entities().get(entityId).getComponent(PositionTestComponent::class).x
        )
    }
}
