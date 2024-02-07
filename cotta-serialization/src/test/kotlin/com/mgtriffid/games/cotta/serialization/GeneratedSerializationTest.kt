package com.mgtriffid.games.cotta.serialization

import com.mgtriffid.games.cotta.serialization.workload.components.TwoNumericalFieldsComponent
import com.mgtriffid.games.cotta.serialization.workload.components.TwoNumericalFieldsComponentSerializer
import com.mgtriffid.games.cotta.serialization.workload.components.createTwoNumericalFieldsComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeneratedSerializationTest {

    @Test
    fun `generated serializer should serialize and deserialize`() {
        val component = createTwoNumericalFieldsComponent(12, 1235)
        val serializer = TwoNumericalFieldsComponentSerializer()
        val serialized: ByteArray = serializer.serialize(component)
        val after: TwoNumericalFieldsComponent = serializer.deserialize(serialized)
        assertEquals(12, after.a)
        assertEquals(1235, after.b)
    }
}
