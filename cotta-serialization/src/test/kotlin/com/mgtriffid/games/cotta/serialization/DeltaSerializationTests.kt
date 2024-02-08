package com.mgtriffid.games.cotta.serialization

import com.mgtriffid.games.cotta.serialization.workload.components.HalfMutableHealthComponentSerializer
import com.mgtriffid.games.cotta.serialization.workload.components.WildNineteenMutableFields
import com.mgtriffid.games.cotta.serialization.workload.components.WildNineteenMutableFieldsSerializer
import com.mgtriffid.games.cotta.serialization.workload.components.createHalfMutableHealthComponent
import com.mgtriffid.games.cotta.serialization.workload.components.createWildNineteenMutableFields
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeltaSerializationTests {
    @Test
    fun `should serialize simple components`() {
        val healthPrevious = createHalfMutableHealthComponent(100, 100)
        val healthCurrent = createHalfMutableHealthComponent(100, 15)
        val serializer = HalfMutableHealthComponentSerializer()
        val delta = serializer.serializeDelta(healthCurrent, healthPrevious)
        serializer.deserializeDelta(delta, healthPrevious)
        assertEquals(15, healthPrevious.health)
    }

    @Test
    fun `should serialize stupidly big components`() {
        val componentBefore = Fixtures.createNinetyFieldsComponentBefore()
        val componentAfter = Fixtures.createNinetyFieldsComponentAfter()
        val serializer = WildNineteenMutableFieldsSerializer()
        val delta = serializer.serializeDelta(componentAfter, componentBefore)
        serializer.deserializeDelta(delta, componentBefore)
        assertEquals(componentAfter, componentBefore)
    }

    object Fixtures {
        fun createNinetyFieldsComponentBefore(): WildNineteenMutableFields {
            val component = createWildNineteenMutableFields(
                a = 1,
                b = 2,
                c = 3,
                d = 4,
                e = 5.0,
                f = 6.0f,
                g = 7.0f,
                h = 8,
                i = 9,
                j = 10,
                k = 11,
                l = 12,
                m = 13,
                n = 14,
                o = 15.0f,
                p = 16,
                q = 17,
                r = 18,
                s = 19,
            )
            return component
        }

        fun createNinetyFieldsComponentAfter(): WildNineteenMutableFields {
            val component = createWildNineteenMutableFields(
                a = 1 * 99,
                b = 2,
                c = 3 * 99,
                d = 4,
                e = 5.0 * 99,
                f = 6.0f,
                g = 7.0f * 99,
                h = 8,
                i = 891,
                j = 10,
                k = 11 * 99,
                l = 12,
                m = 13 * 99,
                n = 14,
                o = 15.0f * 99,
                p = (16 * 99).toShort(),
                q = 17 * 99,
                r = 18,
                s = 19 * 99,
            )
            return component
        }
    }
}
