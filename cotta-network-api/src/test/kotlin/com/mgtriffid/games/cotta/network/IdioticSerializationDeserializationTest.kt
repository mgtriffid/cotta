package com.mgtriffid.games.cotta.network

import com.mgtriffid.games.cotta.network.workload.MaterialPointTestComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdioticSerializationDeserializationTest {
    @Test
    fun `should serialize and deserialize component correctly`() {
        val serde = IdioticSerializationDeserialization()
        serde.registerComponentClass(MaterialPointTestComponent::class)

        val initialComponent = MaterialPointTestComponent.create(1, 2, 3)

        val serialized = serde.serialize(initialComponent)

        val rebuilt = serde.deserialize(serialized, MaterialPointTestComponent::class)

        assertEquals(1, rebuilt.mass)
        assertEquals(2, rebuilt.xPos)
        assertEquals(3, rebuilt.yPos)
    }
}
