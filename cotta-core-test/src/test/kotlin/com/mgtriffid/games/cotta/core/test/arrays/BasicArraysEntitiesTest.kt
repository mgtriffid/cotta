package com.mgtriffid.games.cotta.core.test.arrays

import com.esotericsoftware.kryo.Kryo
import com.mgtriffid.games.cotta.core.entities.arrays.ArraysBasedState
import com.mgtriffid.games.cotta.core.registry.impl.ComponentRegistryImpl
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.test.workload.GameStub
import com.mgtriffid.games.cotta.core.test.workload.components.AnotherComponent
import com.mgtriffid.games.cotta.core.test.workload.components.SimpleComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createAnotherComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createSimpleComponent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BasicArraysEntitiesTest : ArraysEcsTest() {

    @Test
    fun `should create entity`() {
        state.createEntity()
    }

    @Test
    fun `should add component and then retrieve it`() {
        val entity = state.createEntity()
        entity.addComponent(createSimpleComponent(42))
        val component = entity.getComponent(SimpleComponent::class)
        assertEquals(42, component.value)
    }

    @Test
    fun `should add component and not mix it up`() {
        val entity1 = state.createEntity()
        val entity2 = state.createEntity()
        entity1.addComponent(createSimpleComponent(42))
        entity2.addComponent(createSimpleComponent(43))
        val component1 = entity1.getComponent(SimpleComponent::class)
        val component2 = entity2.getComponent(SimpleComponent::class)
        assertEquals(42, component1.value)
        assertEquals(43, component2.value)
    }

    @Test
    fun `should be hasComponent false after removal`() {
        val entity = state.createEntity()
        entity.addComponent(createSimpleComponent(42))
        assertTrue(entity.hasComponent(SimpleComponent::class))
        entity.removeComponent(SimpleComponent::class)
        assertFalse(entity.hasComponent(SimpleComponent::class))
    }

    @Test
    fun `when component is removed another component works well`() {
        val entity1 = state.createEntity()
        val entity2 = state.createEntity()
        entity1.addComponent(createSimpleComponent(42))
        entity2.addComponent(createSimpleComponent(43))
        entity1.removeComponent(SimpleComponent::class)
        assertEquals(43, entity2.getComponent(SimpleComponent::class).value)
    }

    @Test
    fun `multiple components adding`() {
        val entity = state.createEntity()
        entity.addComponent(createSimpleComponent(42))
        entity.addComponent(createAnotherComponent(43.0))
        assertEquals(43.0, entity.getComponent(AnotherComponent::class).value)
        assertEquals(42, entity.getComponent(SimpleComponent::class).value)
    }

    @Test
    fun `multiple components removal`() {
        val entity1 = state.createEntity()
        val entity2 = state.createEntity()
        val entity3 = state.createEntity()
        entity1.addComponent(createSimpleComponent(42))
        entity2.addComponent(createSimpleComponent(43))
        entity3.addComponent(createSimpleComponent(44))
        entity2.removeComponent(SimpleComponent::class)
        entity1.removeComponent(SimpleComponent::class)
        assertEquals(44, entity3.getComponent(SimpleComponent::class).value)
    }

    @Test
    fun `should retrieve entity by id correctly`() {
        val entity = state.createEntity()
        entity.addComponent(createSimpleComponent(42))
        val retrievedEntity = state.getEntity(entity.id)!!
        assertEquals(42, retrievedEntity.getComponent(SimpleComponent::class).value)
    }
}
