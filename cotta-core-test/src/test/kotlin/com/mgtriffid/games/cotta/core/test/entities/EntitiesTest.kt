package com.mgtriffid.games.cotta.core.test.entities

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.impl.ComponentRegistryImpl
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.test.workload.GameStub
import com.mgtriffid.games.cotta.core.test.workload.components.PositionTestComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createPositionTestComponent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class EntitiesTest {

    private lateinit var tickProvider: TickProvider
    private lateinit var componentRegistry: ComponentRegistry

    @BeforeEach
    fun setUp() {
        tickProvider = AtomicLongTickProvider()
        componentRegistry = ComponentRegistryImpl(
            com.esotericsoftware.kryo.Kryo(),
        )
        registerComponents(GameStub, componentRegistry)
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `should return hasComponent of true after component added`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(0, 0))

        assertTrue(entity.hasComponent(PositionTestComponent::class))
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `should return component with correct values after added`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))

        assertEquals(1, entity.getComponent(PositionTestComponent::class).x)
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `should say hasComponent is false if it was removed`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(0, 0))
        entity.removeComponent(PositionTestComponent::class)

        assertFalse(entity.hasComponent(PositionTestComponent::class))
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `cottaState should use the same Entities if called repeatedly`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id

        assertEquals(
            1,
            cottaState.entities().get(entityId)
                ?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `should have the same value for components after advancing a tick`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id

        cottaState.advance()

        assertEquals(
            1,
            cottaState.entities().get(entityId)
                ?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `should remember previous value after advancing`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id
        val tick = tickProvider.tick

        cottaState.advance()

        assertEquals(
            1,
            cottaState.entities(tick).get(entityId)
                ?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `should remember previous value after advancing and altering value`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id
        val tick = tickProvider.tick

        cottaState.advance()

        cottaState.entities().get(entityId)
            ?.getComponent(PositionTestComponent::class)?.x = 2

        assertEquals(
            1,
            cottaState.entities(tick).get(entityId)
                ?.getComponent(PositionTestComponent::class)?.x
        )
    }

    @ParameterizedTest
    @MethodSource("states")
    fun `should be possible to advance like 100 times`(
        stateFactory: CottaStateFactory
    ) {
        val cottaState = stateFactory.getInstance(componentRegistry)
        val entities = cottaState.entities()
        val entity = entities.create()
        entity.addComponent(createPositionTestComponent(1, 1))
        val entityId = entity.id

        repeat(100) {
            cottaState.advance()
        }

        assertEquals(
            1,
            cottaState.entities().get(entityId)
                ?.getComponent(PositionTestComponent::class)?.x
        )
    }

    private fun CottaState.entities() = entities(tickProvider.tick)

    private fun CottaState.advance() {
        advance(tickProvider.tick)
        tickProvider.tick++
    }

    interface CottaStateFactory {
        fun getInstance(componentRegistry: ComponentRegistry): CottaState
    }

    private class CottaStateImplFactory : CottaStateFactory {
        override fun getInstance(componentRegistry: ComponentRegistry): CottaState {
            return CottaState.getInstance(componentRegistry)
        }
    }

/*
    private class ArrayBasedCottaStateFactory : CottaStateFactory {
        override fun getInstance(componentRegistry: ComponentRegistry): CottaState {
            return ArraysBasedCottaState(componentRegistry)
        }
    }
*/

    companion object {
        @JvmStatic
        fun states(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(CottaStateImplFactory()),
//                Arguments.of(ArrayBasedCottaStateFactory())
            )
        }
    }
}
