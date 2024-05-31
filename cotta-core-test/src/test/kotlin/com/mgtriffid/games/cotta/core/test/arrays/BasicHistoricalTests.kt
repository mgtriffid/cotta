package com.mgtriffid.games.cotta.core.test.arrays

import com.mgtriffid.games.cotta.core.test.workload.components.HistoricalMutableComponent
import com.mgtriffid.games.cotta.core.test.workload.components.SimpleComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createHistoricalMutableComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createSimpleComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class BasicHistoricalTests : ArraysEcsTest() {

    @Test
    fun `should advance and preserve previous value of historical component`() {
        val entity = state.createEntity()
        entity.addComponent(createHistoricalMutableComponent(42))
        state.advance()
        state.getEntity(entity.id)!!.getComponent(HistoricalMutableComponent::class).value = 43
        val previous = state.atTick(0L).getEntity(entity.id)!!.getComponent(HistoricalMutableComponent::class)
        assertEquals(42, previous.value)
        val current = state.getEntity(entity.id)!!.getComponent(HistoricalMutableComponent::class)
        assertEquals(43, current.value)
    }

    @Test
    fun `should see that there was no component in the past`() {
        val entity = state.createEntity()
        state.advance()
        entity.addComponent(createHistoricalMutableComponent(42))
        assertFalse(state.atTick(0L).getEntity(entity.id)!!
                .hasComponent(HistoricalMutableComponent::class))
        val current = state.getEntity(entity.id)!!.getComponent(HistoricalMutableComponent::class)
        assertEquals(42, current.value)
    }

    @Test
    fun `when looking for a non-historical component should use current state`() {
        val entity = state.createEntity()
        entity.addComponent(createSimpleComponent(42))
        state.advance()
        entity.addComponent(createSimpleComponent(43))
        val current = state.getEntity(entity.id)!!.getComponent(SimpleComponent::class)
        assertEquals(43, current.value)
        assertEquals(43, state.atTick(0L).getEntity(entity.id)!!.getComponent(SimpleComponent::class).value)
    }
}
