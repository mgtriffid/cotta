//package com.mgtriffid.games.cotta.serialization
//
//import com.mgtriffid.games.cotta.core.entities.id.AuthoritativeEntityId
//import com.mgtriffid.games.cotta.serialization.workload.components.AllTypesComponent
////import com.mgtriffid.games.cotta.serialization.workload.components.AllTypesComponentSerializer
//import com.mgtriffid.games.cotta.serialization.workload.components.TwoNumericalFieldsComponent
////import com.mgtriffid.games.cotta.serialization.workload.components.TwoNumericalFieldsComponentSerializer
//import com.mgtriffid.games.cotta.serialization.workload.components.createAllTypesComponent
//import com.mgtriffid.games.cotta.serialization.workload.components.createTwoNumericalFieldsComponent
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Test
//
//class GeneratedSerializationTest {
//
//    @Test
//    fun `generated serializer should serialize and deserialize`() {
//        val component = createTwoNumericalFieldsComponent(12, 1235)
//        val serializer = TwoNumericalFieldsComponentSerializer()
//        val serialized: ByteArray = serializer.serialize(component)
//        val after: TwoNumericalFieldsComponent = serializer.deserialize(serialized)
//        assertEquals(12, after.a)
//        assertEquals(1235, after.b)
//    }
//
//    @Test
//    fun `should support all types`() {
//        val component = createAllTypesComponent(1, 2, 3, 4, 5.0f, 6.0, AuthoritativeEntityId(2))
//        val serializer = AllTypesComponentSerializer()
//        val serialized: ByteArray = serializer.serialize(component)
//        val after: AllTypesComponent = serializer.deserialize(serialized)
//        assertEquals(1, after.a)
//        assertEquals(2, after.b)
//        assertEquals(3, after.c)
//        assertEquals(4, after.d)
//        assertEquals(5.0f, after.e)
//        assertEquals(6.0, after.f)
//        assertEquals(AuthoritativeEntityId(2), after.entityId)
//    }
//}
