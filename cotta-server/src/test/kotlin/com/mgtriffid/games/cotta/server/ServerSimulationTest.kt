package com.mgtriffid.games.cotta.server

import com.google.inject.Guice
import com.mgtriffid.games.cotta.core.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.server.guice.CottaServerModule
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.components.VelocityTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect
import com.mgtriffid.games.cotta.server.workload.systems.BlankTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.EntityShotTestEffectConsumerSystem
import com.mgtriffid.games.cotta.server.workload.systems.HealthRegenerationTestEffectsConsumerSystem
import com.mgtriffid.games.cotta.server.workload.systems.LagCompensatedShotFiredTestEffectConsumer
import com.mgtriffid.games.cotta.server.workload.systems.MovementTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.PlayerInputProcessingTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.RegenerationTestSystem
import com.mgtriffid.games.cotta.server.workload.systems.ShotFiredTestEffectConsumerSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class ServerSimulationTest {
    private lateinit var tickProvider: TickProvider
    private lateinit var simulationInputHolder: SimulationInputHolder
    private lateinit var state: CottaState
    private lateinit var serverSimulation: ServerSimulation

    @BeforeEach
    fun setUp() {
        val injector = Guice.createInjector(CottaServerModule(GameStub))
        tickProvider = injector.getInstance(TickProvider::class.java)
        simulationInputHolder = injector.getInstance(SimulationInputHolder::class.java)
        simulationInputHolder.set(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
        })
        state = injector.getInstance(CottaState::class.java)
        serverSimulation = injector.getInstance(ServerSimulation::class.java)
    }

    @Test
    fun `systems should listen to effects`() {
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)

        serverSimulation.tick()

        assertEquals(
            1,
            state.entities().get(entityId).getComponent(HealthTestComponent::class).health
        )
    }

    @Test
    fun `effects should be processed within given tick once`() {
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)

        serverSimulation.tick()
        serverSimulation.tick()

        assertEquals(
            2,
            state.entities().get(entityId).getComponent(HealthTestComponent::class).health
        )
    }

    @Test
    fun `should consume input`() {
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        serverSimulation.registerSystem(PlayerInputProcessingTestSystem::class)
        serverSimulation.registerSystem(ShotFiredTestEffectConsumerSystem::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)

        serverSimulation.tick()
        serverSimulation.tick()

        val input1 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )

        simulationInputHolder.set(object: SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input1)
                )
            }

            override fun playersSawTicks() = emptyMap<PlayerId, Long>()
        })

        serverSimulation.tick()

        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )
        simulationInputHolder.set(object: SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }
            override fun playersSawTicks() = emptyMap<PlayerId, Long>()
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
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity(ownedBy = Entity.OwnedBy.Player(playerId))
        val damageDealerId = damageDealer.id
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val input = PlayerInputTestComponent.create(aim = 4, shoot = true)
        serverSimulation.registerSystem(PlayerInputProcessingTestSystem::class)
        serverSimulation.registerSystem(LagCompensatedShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)
        repeat(6) {
            serverSimulation.tick()
        }

        simulationInputHolder.set(object: SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input)
                )
            }
            override fun playersSawTicks() = mapOf(playerId to 2L)
        })


        serverSimulation.tick()
        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = false
        )
        simulationInputHolder.set(object: SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }
            override fun playersSawTicks() = mapOf(playerId to 3L)
        })
        serverSimulation.tick()

        assertEquals(
            15,
            state.entities().get(damageableId).getComponent(HealthTestComponent::class).health
        )
    }

    @Test
    fun `should invoke systems`() {
        state.entities().createEntity()
        serverSimulation.registerSystem(BlankTestSystem::class)

        serverSimulation.tick()

        assertEquals(
            1,
            BlankTestSystem.counter
        )
    }

    @Test
    fun `should return effects that are to be returned to clients`() {
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)

        serverSimulation.tick()

        assertEquals(
            HealthRegenerationTestEffect(entityId, 1),
            serverSimulation.getDataToBeSentToClients().effects(tick = tickProvider.tick).first()
        )
    }

    @Test
    fun `should prepare inputs to be sent to clients`() {
        val playerId = PlayerId(0)
        val damageDealer = state.entities().createEntity()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        serverSimulation.registerSystem(PlayerInputProcessingTestSystem::class)
        val input1 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )
        simulationInputHolder.set(object: SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input1)
                )
            }
            override fun playersSawTicks() = mapOf(playerId to 2L)
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
        simulationInputHolder.set(object: SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }
            override fun playersSawTicks() = mapOf(playerId to 3L)
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

    private object GameStub : CottaGame {
        override val serverSystems: List<KClass<*>> = emptyList()
        override val nonPlayerInputProvider = object : NonPlayerInputProvider {
            override fun input(entities: Entities) = emptyMap<EntityId, Collection<InputComponent<*>>>()
        }
        override fun initializeServerState(state: CottaState) { }
        override val componentClasses: Set<KClass<out Component<*>>> = emptySet()
        override val inputComponentClasses: Set<KClass<out InputComponent<*>>> = emptySet()
        override val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>> = emptySet()
        override val config: CottaConfig = object : CottaConfig {
            override val tickLength: Long = 20
        }
    }
}
