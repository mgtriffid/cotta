package com.mgtriffid.games.cotta.network.idiotic

import com.mgtriffid.games.cotta.network.idiotic.workload.MaterialPointTestComponent
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

    @Test
    fun `should serialize delta and then apply it correctly`() {
        val serde = IdioticSerializationDeserialization()
        serde.registerComponentClass(MaterialPointTestComponent::class)

        val c0 = MaterialPointTestComponent.create(1, 2, 3)
        val c1 = MaterialPointTestComponent.create(1, 4, 5)

        val target = MaterialPointTestComponent.create(1, 2, 3)
        val delta = serde.serializeDelta(c0, c1)
        serde.deserializeAndApplyDelta(delta, target)

        assertEquals(4, target.xPos)
        assertEquals(5, target.yPos)
    }
}
