package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.systems.BlankTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.HealthRegenerationEffectsConsumer
import com.mgtriffid.games.cotta.server.workload.systems.RegenerationSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ServerSimulationTest {

    @Test
    fun `should invoke systems`() {
        val state = CottaState.getInstance()
        state.entities().createEntity()
        val system = BlankTestSystem(0)
        val serverSimulation = ServerSimulation.getInstance()
        serverSimulation.registerSystem(system)
        serverSimulation.setState(state)

        serverSimulation.tick()

        assertEquals(
            1,
            system.counter
        )
    }

    @Test
    fun `systems should listen to effects`() {
        val state = CottaState.getInstance()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = ServerSimulation.getInstance()
        val regenerationSystem = RegenerationSystem(serverSimulation.effectBus())
        serverSimulation.registerSystem(regenerationSystem)
        serverSimulation.registerSystem(HealthRegenerationEffectsConsumer(state))
        serverSimulation.setState(state)

        serverSimulation.tick()

        assertEquals(
            1,
            state.entities().get(entityId).getComponent(HealthTestComponent::class).health
        )
    }

    @Test
    fun `effects should be processed within given tick once`() {
        val state = CottaState.getInstance()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = ServerSimulation.getInstance()
        val regenerationSystem = RegenerationSystem(serverSimulation.effectBus())
        serverSimulation.registerSystem(regenerationSystem)
        serverSimulation.registerSystem(HealthRegenerationEffectsConsumer(state))
        serverSimulation.setState(state)

        serverSimulation.tick()
        serverSimulation.tick()

        assertEquals(
            2,
            state.entities().get(entityId).getComponent(HealthTestComponent::class).health
        )
    }

    @Test
    @Disabled
    fun `should be able to return all effects that happened`() {

    }

    @Test
    @Disabled
    fun `should be able to serialize state diff`() {

    }

    @Test
    @Disabled
    fun `should consume input`() {

    }
}
