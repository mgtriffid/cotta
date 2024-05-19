package com.mgtriffid.games.cotta.core.test.arrays

import com.mgtriffid.games.cotta.core.entities.arrays.ComponentStorage
import com.mgtriffid.games.cotta.core.test.workload.components.SimpleComponentDataStorage
import com.mgtriffid.games.cotta.core.test.workload.components.createSimpleComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// TODO remove, this tests internals while it probably shouldn't
class ComponentsStorageTest {
    @Test
    fun `should add component`() {
        // Given
        val storage = ComponentStorage(SimpleComponentDataStorage())

        // When
        storage.add(createSimpleComponent(2), 1)
//        storage.flushOperations()
        // Then
        assertEquals(2, storage.get(0).value)
    }

    @Test
    fun `should remove component`() {
        // Given
        val storage = ComponentStorage(SimpleComponentDataStorage())
        storage.add(createSimpleComponent(2), 1)
        storage.add(createSimpleComponent(3), 2)
//        storage.flushOperations()
        // When
        storage.remove(0)
//        storage.flushOperations()
        // Then
        assertEquals(3, storage.get(0).value)
        assertEquals(2, storage.getEntityId(0))
    }
}
