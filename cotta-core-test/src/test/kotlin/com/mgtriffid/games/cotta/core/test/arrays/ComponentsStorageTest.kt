package com.mgtriffid.games.cotta.core.test.arrays

import com.mgtriffid.games.cotta.core.entities.arrays.ComponentStorage
import com.mgtriffid.games.cotta.core.test.arrays.workload.SimpleComponentDataStorage
import com.mgtriffid.games.cotta.core.test.arrays.workload.createSimpleComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
    }
}
