package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.components.VelocityTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect
import com.mgtriffid.games.cotta.server.workload.systems.BlankTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.EntityShotTestEffectConsumer
import com.mgtriffid.games.cotta.server.workload.systems.HealthRegenerationTestEffectsConsumer
import com.mgtriffid.games.cotta.server.workload.systems.LagCompensatedShotFiredTestEffectConsumer
import com.mgtriffid.games.cotta.server.workload.systems.MovementTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.PlayerInputProcessingSystem
import com.mgtriffid.games.cotta.server.workload.systems.RegenerationTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.ShotFiredTestEffectConsumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ServerSimulationTest {
    private lateinit var tickProvider: TickProvider

    @BeforeEach
    fun setUp() { tickProvider = AtomicLongTickProvider() }

    @Test
    fun `systems should listen to effects`() {
        val state = getCottaState()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = getServerSimulation()
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
        val state = getCottaState()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = getServerSimulation()
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
        val state = getCottaState()
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        val serverSimulation = getServerSimulation()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(PlayerInputProcessingSystem::class)
        serverSimulation.registerSystem(ShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumer::class)

        serverSimulation.tick()
        serverSimulation.tick()

        val input1 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )

        serverSimulation.setInputForUpcomingTick(object: IncomingInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input1)
                )
            }
        })

        serverSimulation.tick()

        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )
        serverSimulation.setInputForUpcomingTick(object: IncomingInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }
        })

        serverSimulation.tick()

        assertEquals(
            15,
            state.entities().get(damageableId).getComponent(HealthTestComponent::class).health
        )
    }

    @Test
    fun `should compensate lags`() {
        val playerId = PlayerId(0)
        val state = getCottaState()
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity()
        val damageDealerId = damageDealer.id
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val input = PlayerInputTestComponent.create(aim = 4, shoot = true)
        val serverSimulation = getServerSimulation()
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
        serverSimulation.setInputForUpcomingTick(object: IncomingInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input)
                )
            }
        })


        serverSimulation.tick()
        input.aim = 4
        input.shoot = false
        serverSimulation.setInputForUpcomingTick(object: IncomingInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input)
                )
            }
        })
        serverSimulation.tick()

        assertEquals(
            15,
            state.entities().get(damageableId).getComponent(HealthTestComponent::class).health
        )
    }

    @Test
    fun `should invoke systems`() {
        val state = getCottaState()
        state.entities().createEntity()
        val serverSimulation = getServerSimulation()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(BlankTestSystem::class)

        serverSimulation.tick()

        assertEquals(
            1,
            BlankTestSystem.counter
        )
    }

    @Test
    fun `should return effects that are to be returned to clients`() {
        val state = getCottaState()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = getServerSimulation()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumer::class)

        serverSimulation.tick()

        assertEquals(
            HealthRegenerationTestEffect(entityId, 1),
            serverSimulation.getDataToBeSentToClients().effects(tick = tickProvider.tick).first()
        )
    }

    @Test
    fun `should prepare inputs to be sent to clients`() {
        val playerId = PlayerId(0)
        val state = getCottaState()
        val damageDealer = state.entities().createEntity()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        val serverSimulation = getServerSimulation()
        serverSimulation.setState(state)
        serverSimulation.registerSystem(PlayerInputProcessingSystem::class)
        serverSimulation.setEntityOwner(damageDealerId, playerId)
        serverSimulation.setPlayerSawTick(playerId, 2L)
        val input1 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )
        serverSimulation.setInputForUpcomingTick(object: IncomingInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input1)
                )
            }
        })
        serverSimulation.tick()
        val dataToBeSentToClients = serverSimulation.getDataToBeSentToClients()
        assertEquals(
            4,
            (dataToBeSentToClients.inputs(tickProvider.tick)[damageDealerId]?.first() as PlayerInputTestComponent).aim
        )
        assertEquals(
            true,
            (dataToBeSentToClients.inputs(tickProvider.tick)[damageDealerId]?.first() as PlayerInputTestComponent).shoot
        )
        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = false
        )
        serverSimulation.setInputForUpcomingTick(object: IncomingInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }
        })
        assertEquals(
            4,
            (dataToBeSentToClients.inputs(tickProvider.tick)[damageDealerId]?.first() as PlayerInputTestComponent).aim
        )
        assertEquals(
            true,
            (dataToBeSentToClients.inputs(tickProvider.tick)[damageDealerId]?.first() as PlayerInputTestComponent).shoot
        )
    }


    private fun getCottaState() = CottaState.getInstance(tickProvider)

    private fun getServerSimulation() = ServerSimulation.getInstance(tickProvider = tickProvider, historyLength = 8)
}
