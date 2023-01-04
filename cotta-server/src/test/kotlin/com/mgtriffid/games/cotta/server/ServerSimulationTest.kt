package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.components.VelocityTestComponent
import com.mgtriffid.games.cotta.server.workload.systems.BlankTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.EntityShotTestEffectConsumer
import com.mgtriffid.games.cotta.server.workload.systems.HealthRegenerationTestEffectsConsumer
import com.mgtriffid.games.cotta.server.workload.systems.MovementTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.PlayerInputProcessingSystem
import com.mgtriffid.games.cotta.server.workload.systems.RegenerationTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.ShotFiredTestEffectConsumer
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
        serverSimulation.setState(state)
        serverSimulation.registerSystem(system)

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
        serverSimulation.setState(state)
        val regenerationTestSystem = RegenerationTestSystem(serverSimulation.effectBus())
        serverSimulation.registerSystem(regenerationTestSystem)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumer(state))

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
        serverSimulation.setState(state)
        val regenerationTestSystem = RegenerationTestSystem(serverSimulation.effectBus())
        serverSimulation.registerSystem(regenerationTestSystem)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumer(state))

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
    fun `should consume input`() {
        val state = CottaState.getInstance()
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity()
        val input = PlayerInputTestComponent.create()
        damageDealer.addComponent(input)
        val serverSimulation = ServerSimulation.getInstance()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(PlayerInputProcessingSystem(serverSimulation.effectBus()))
        serverSimulation.registerSystem(ShotFiredTestEffectConsumer(
            serverSimulation.effectBus(),
            state
        ))
        serverSimulation.registerSystem(MovementTestSystem())
        serverSimulation.registerSystem(EntityShotTestEffectConsumer(state))

        serverSimulation.tick()
        serverSimulation.tick()
        input.aim = 4
        input.shoot = true
        serverSimulation.tick()
        input.aim = 4
        input.shoot = false

        serverSimulation.tick()

        assertEquals(
            15,
            state.entities().get(damageableId).getComponent(HealthTestComponent::class).health
        )
    }
}
