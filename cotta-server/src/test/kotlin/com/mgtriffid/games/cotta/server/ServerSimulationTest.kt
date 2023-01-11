package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.components.VelocityTestComponent
import com.mgtriffid.games.cotta.server.workload.systems.BlankTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.EntityShotTestEffectConsumer
import com.mgtriffid.games.cotta.server.workload.systems.HealthRegenerationTestEffectsConsumer
import com.mgtriffid.games.cotta.server.workload.systems.LagCompensatedShotFiredTestEffectConsumer
import com.mgtriffid.games.cotta.server.workload.systems.MovementTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.PlayerInputProcessingSystem
import com.mgtriffid.games.cotta.server.workload.systems.RegenerationTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.ShotFiredTestEffectConsumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServerSimulationTest {

    @Test
    fun `systems should listen to effects`() {
        val state = CottaState.getInstance()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = ServerSimulation.getInstance()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumer::class)

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
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumer::class)

        serverSimulation.tick()
        serverSimulation.tick()

        assertEquals(
            2,
            state.entities().get(entityId).getComponent(HealthTestComponent::class).health
        )
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
        serverSimulation.registerSystem(PlayerInputProcessingSystem::class)
        serverSimulation.registerSystem(ShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumer::class)

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

    @Test
    fun `should compensate lags`() {
        val playerId = PlayerId(0)
        val state = CottaState.getInstance()
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity()
        val damageDealerId = damageDealer.id
        val input = PlayerInputTestComponent.create()
        damageDealer.addComponent(input)
        val serverSimulation = ServerSimulation.getInstance()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(PlayerInputProcessingSystem::class)
        serverSimulation.registerSystem(LagCompensatedShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumer::class)
        repeat(6) {
            serverSimulation.tick()
        }

        serverSimulation.setEntityOwner(damageDealerId, playerId)
        serverSimulation.setPlayerSawTick(playerId, 2L)
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

    @Test
    fun `should invoke systems`() {
        val state = CottaState.getInstance()
        state.entities().createEntity()
        val serverSimulation = ServerSimulation.getInstance()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(BlankTestSystem::class)

        serverSimulation.tick()

        assertEquals(
            1,
            BlankTestSystem.counter
        )
    }
}
