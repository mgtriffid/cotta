package com.mgtriffid.games.cotta.server

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import com.mgtriffid.games.cotta.core.CottaConfig
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.effects.CottaEffect
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.entities.id.EntityId
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.server.guice.CottaServerModule
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.components.LinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.components.PlayerInputTestComponent
import com.mgtriffid.games.cotta.server.workload.components.VelocityTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.HealthRegenerationTestEffect
import com.mgtriffid.games.cotta.server.workload.systems.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class ServerSimulationTest {
    private lateinit var tickProvider: TickProvider
    private lateinit var simulationInputHolder: SimulationInputHolder
    private lateinit var state: CottaState
    private lateinit var serverSimulation: ServerSimulation
    private lateinit var playersSawTicks: PlayersSawTicks
    private lateinit var dataForClients: DataForClients

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
        state = injector.getInstance(Key.get(CottaState::class.java, Names.named("simulation")))
        serverSimulation = injector.getInstance(ServerSimulation::class.java)
        dataForClients = injector.getInstance(DataForClients::class.java)
        playersSawTicks = injector.getInstance(PlayersSawTicks::class.java)
    }

    @Test
    fun `systems should listen to effects`() {
        val entity = state.entities().create()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)

        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
        })

        assertEquals(
            1,
            state.entities().get(entityId)?.getComponent(HealthTestComponent::class)?.health
        )
    }

    @Test
    fun `effects should be processed within given tick once`() {
        val entity = state.entities().create()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)
        repeat(2) {
            serverSimulation.tick(object : SimulationInput {
                override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                    return emptyMap()
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
            })
        }

        assertEquals(
            2,
            state.entities().get(entityId)?.getComponent(HealthTestComponent::class)?.health
        )
    }

    @Test
    fun `should consume input`() {
        val damageable = state.entities().create()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().create()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        serverSimulation.registerSystem(PlayerInputProcessingTestSystem::class)
        serverSimulation.registerSystem(ShotFiredTestEffectConsumerSystem::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)

        repeat(2) {
            serverSimulation.tick(object : SimulationInput {
                override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                    return emptyMap()
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
            })
        }

        val input1 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )

        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input1)
                )
            }

            override fun playersSawTicks() = emptyMap<PlayerId, Long>()
        })

        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )

        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }

            override fun playersSawTicks() = emptyMap<PlayerId, Long>()
        })

        assertEquals(
            15,
            state.entities().get(damageableId)?.getComponent(HealthTestComponent::class)?.health
        )
    }

    @Test
    fun `should compensate lags`() {
        val playerId = PlayerId(0)
        val damageable = state.entities().create()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().create(ownedBy = Entity.OwnedBy.Player(playerId))
        val damageDealerId = damageDealer.id
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val input = PlayerInputTestComponent.create(aim = 4, shoot = true)
        serverSimulation.registerSystem(PlayerInputProcessingTestSystem::class)
        serverSimulation.registerSystem(LagCompensatedShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)
        repeat(6) {
            serverSimulation.tick(object : SimulationInput {
                override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                    return emptyMap()
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
            })
        }

        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input)
                )
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = false
        )
        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }

            override fun playersSawTicks() = mapOf(playerId to 3L)
        })

        assertEquals(
            15,
            state.entities().get(damageableId)?.getComponent(HealthTestComponent::class)?.health
        )
    }

    @Test
    fun `should compensate lags if effect creates another effect`() {
        val playerId = PlayerId(0)
        val damageable = state.entities().create()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().create(ownedBy = Entity.OwnedBy.Player(playerId))
        val damageDealerId = damageDealer.id
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val input = PlayerInputTestComponent.create(aim = 4, shoot = true)
        serverSimulation.registerSystem(PlayerInputProcessingTestSystem::class)
        serverSimulation.registerSystem(StepOneShotFiredTestEffectConsumerSystem::class)
        serverSimulation.registerSystem(LagCompensatedActualShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)
        repeat(6) {
            serverSimulation.tick(object : SimulationInput {
                override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                    return emptyMap()
                }

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
            })
        }

        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input)
                )
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = false
        )
        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }

            override fun playersSawTicks() = mapOf(playerId to 3L)
        })

        assertEquals(
            15,
            state.entities().get(damageableId)?.getComponent(HealthTestComponent::class)?.health
        )
    }

    @Test
    fun `should invoke systems`() {
        state.entities().create()
        serverSimulation.registerSystem(BlankTestSystem::class)

        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
        })

        assertEquals(
            1,
            BlankTestSystem.counter
        )
    }

    @Test
    fun `should return effects that are to be returned to clients`() {
        val entity = state.entities().create()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)

        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
        })

        assertEquals(
            HealthRegenerationTestEffect(entityId, 1),
            dataForClients.effects(tick = tickProvider.tick).first()
        )
    }

    @Test
    fun `should prepare inputs to be sent to clients`() {
        val playerId = PlayerId(0)
        val damageDealer = state.entities().create()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        serverSimulation.registerSystem(PlayerInputProcessingTestSystem::class)
        val input1 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = true
        )
        simulationInputHolder.set(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input1)
                )
            }
            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        serverSimulation.tick(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input1)
                )
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        assertEquals(
            4,
            (dataForClients.inputs()[damageDealerId]?.first() as PlayerInputTestComponent).aim
        )
        assertEquals(
            true,
            (dataForClients.inputs()[damageDealerId]?.first() as PlayerInputTestComponent).shoot
        )
        val input2 = PlayerInputTestComponent.create(
            aim = 4,
            shoot = false
        )
        simulationInputHolder.set(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Set<InputComponent<*>>> {
                return mapOf(
                    damageDealerId to setOf(input2)
                )
            }

            override fun playersSawTicks() = mapOf(playerId to 3L)
        })
        assertEquals(
            4,
            (dataForClients.inputs()[damageDealerId]?.first() as PlayerInputTestComponent).aim
        )
        assertEquals(
            false,
            (dataForClients.inputs()[damageDealerId]?.first() as PlayerInputTestComponent).shoot
        )
    }

    private object GameStub : CottaGame {
        override val serverSystems: List<KClass<*>> = emptyList()
        override val nonPlayerInputProvider = object : NonPlayerInputProvider {
            override fun input(entities: Entities) = emptyMap<EntityId, Collection<InputComponent<*>>>()
        }

        override fun initializeServerState(entities: Entities) {}
        override fun initializeStaticState(entities: Entities) {}

        override val componentClasses: Set<KClass<out Component<*>>> = emptySet()
        override val inputComponentClasses: Set<KClass<out InputComponent<*>>> = emptySet()
        override val effectClasses: Set<KClass<out CottaEffect>> = emptySet()
        override val metaEntitiesInputComponents: Set<KClass<out InputComponent<*>>> = emptySet()
        override val config: CottaConfig = object : CottaConfig {
            override val tickLength: Long = 20
        }
    }

    private fun CottaState.entities() = entities(tickProvider.tick)
}
