package com.mgtriffid.games.cotta.core.test.entities

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.test.workload.components.PositionTestComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createPositionTestComponent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EntitiesTest {

    private lateinit var tickProvider: TickProvider

    @BeforeEach fun setUp() { tickProvider = AtomicLongTickProvider() }

    @Test
    fun `should return hasComponent of true after component added`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(0, 0))

        assertTrue(entity.hasComponent(PositionTestComponent::class))
    }

    @Test
    fun `should return component with correct values after added`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))

        assertEquals(1, entity.getComponent(PositionTestComponent::class).x)
    }

    @Test
    fun `should say hasComponent is false if it was removed`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(0, 0))
        entity.removeComponent(PositionTestComponent::class)

        assertFalse(entity.hasComponent(PositionTestComponent::class))
    }

    @Test
    fun `cottaState should use the same Entities if called repeatedly`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id

        assertEquals(
            1,
            cottaState.entities().get(entityId)?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @Test
    fun `should have the same value for components after advancing a tick`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id

        cottaState.advance()

        assertEquals(
            1,
            cottaState.entities().get(entityId)?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @Test
    fun `should remember previous value after advancing`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id
        val tick = tickProvider.tick

        cottaState.advance()

        assertEquals(
            1,
            cottaState.entities(tick).get(entityId)?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @Test
    fun `should remember previous value after advancing and altering value`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id
        val tick = tickProvider.tick

        cottaState.advance()

        cottaState.entities().get(entityId)?.getComponent(PositionTestComponent::class)?.x = 2

        assertEquals(
            1,
            cottaState.entities(tick).get(entityId)?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @Test
    fun `should be possible to advance like 100 times`() {
        val cottaState = CottaState.getInstance()
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id

        repeat(100) {
            cottaState.advance()
        }

        assertEquals(
            1,
            cottaState.entities().get(entityId)?.getComponent(PositionTestComponent::class)?.x
        )
    }

    private fun CottaState.entities() = entities(tickProvider.tick)
    private fun CottaState.advance() {
        advance(tickProvider.tick)
        tickProvider.tick++
    }
}
