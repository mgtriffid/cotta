package com.mgtriffid.games.cotta.core.test.arrays

import com.mgtriffid.games.cotta.core.test.workload.components.HistoricalMutableComponent
import com.mgtriffid.games.cotta.core.test.workload.components.SimpleComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createHistoricalMutableComponent
import com.mgtriffid.games.cotta.core.test.workload.components.createSimpleComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BasicHistoricalTests : ArraysEcsTest() {

    @Test
    fun `should advance and preserve previous value of historical component`() {
        val entity = state.createEntity()
        entity.addComponent(createHistoricalMutableComponent(42))
        state.advance()
        state.getEntity(entity.id)!!.getComponent(HistoricalMutableComponent::class).value = 43
        val component = entity.getComponent(HistoricalMutableComponent::class)
        val previous = state.atTick(0L).getEntity(entity.id)!!.getComponent(HistoricalMutableComponent::class)
        assertEquals(42, previous.value)
    }
}
