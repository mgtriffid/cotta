package com.mgtriffid.games.cotta.server

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.server.guice.CottaServerModule
import com.mgtriffid.games.cotta.server.workload.GameStub
import com.mgtriffid.games.cotta.server.workload.PlayerInputStub
import com.mgtriffid.games.cotta.server.workload.components.HealthTestComponent
import com.mgtriffid.games.cotta.server.workload.components.createHealthTestComponent
import com.mgtriffid.games.cotta.server.workload.components.createLinearPositionTestComponent
import com.mgtriffid.games.cotta.server.workload.components.createPlayerControlledStubComponent
import com.mgtriffid.games.cotta.server.workload.components.createVelocityTestComponent
import com.mgtriffid.games.cotta.server.workload.effects.createHealthRegenerationTestEffect
import com.mgtriffid.games.cotta.server.workload.systems.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ServerSimulationTest {
    private lateinit var tickProvider: TickProvider
    private lateinit var simulationInputHolder: SimulationInputHolder
    private lateinit var state: CottaState
    private lateinit var serverSimulation: ServerSimulation
    private lateinit var playersSawTicks: PlayersSawTicks
    private lateinit var dataForClients: DataForClients

    @BeforeEach
    fun setUp() {
        val injector = Guice.createInjector(CottaServerModule(GameStub()))
        tickProvider = injector.getInstance(TickProvider::class.java)
        simulationInputHolder = injector.getInstance(SimulationInputHolder::class.java)
        simulationInputHolder.set(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
        })
        state = injector.getInstance(Key.get(CottaState::class.java, Names.named("simulation")))
        serverSimulation = injector.getInstance(ServerSimulation::class.java)
        registerComponents(GameStub(), injector.getInstance(ComponentRegistry::class.java))
        dataForClients = injector.getInstance(DataForClients::class.java)
        playersSawTicks = injector.getInstance(PlayersSawTicks::class.java)
    }

    @Test
    fun `systems should listen to effects`() {
        val entity = state.entities().create()
        val entityId = entity.id
        entity.addComponent(createHealthTestComponent(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)

        serverSimulation.tick(object : SimulationInput {

            override fun nonPlayerInput() = object: NonPlayerInput {}
            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
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
        entity.addComponent(createHealthTestComponent(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)
        repeat(2) {
            serverSimulation.tick(object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

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
        val playerId = PlayerId(0)
        val damageable = state.entities().create()
        val damageableId = damageable.id
        damageable.addComponent(createHealthTestComponent(20))
        damageable.addComponent(createLinearPositionTestComponent(0))
        damageable.addComponent(createVelocityTestComponent(2))

        val damageDealer = state.entities().create(ownedBy = Entity.OwnedBy.Player(playerId))
        damageDealer.addComponent(createPlayerControlledStubComponent(0, false))
        serverSimulation.registerSystem(PlayerProcessingTestSystem::class)
        serverSimulation.registerSystem(ShotFiredTestEffectConsumerSystem::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)

        repeat(2) {
            serverSimulation.tick(object : SimulationInput {

                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
            })
        }

        val input1 = PlayerInputStub(
            aim = 4,
            shoot = true
        )

        serverSimulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input1)
            }

            override fun playersSawTicks() = emptyMap<PlayerId, Long>()
        })

        val input2 = PlayerInputStub(
            aim = 4,
            shoot = true
        )

        serverSimulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input2)
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
        damageable.addComponent(createHealthTestComponent(20))
        damageable.addComponent(createLinearPositionTestComponent(0))
        damageable.addComponent(createVelocityTestComponent(2))

        val damageDealer = state.entities().create(ownedBy = Entity.OwnedBy.Player(playerId))
        damageDealer.addComponent(createPlayerControlledStubComponent(0, false))
        val input = PlayerInputStub(aim = 4, shoot = true)
        serverSimulation.registerSystem(PlayerProcessingTestSystem::class)
        serverSimulation.registerSystem(LagCompensatedShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)
        repeat(6) {
            serverSimulation.tick(object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
            })
        }

        serverSimulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input)
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        val input2 = PlayerInputStub(
            aim = 4,
            shoot = false
        )
        serverSimulation.tick(object : SimulationInput {

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input2)
            }
            override fun nonPlayerInput() = object: NonPlayerInput {}

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
        damageable.addComponent(createHealthTestComponent(20))
        damageable.addComponent(createLinearPositionTestComponent(0))
        damageable.addComponent(createVelocityTestComponent(2))

        val damageDealer = state.entities().create(ownedBy = Entity.OwnedBy.Player(playerId))
        damageDealer.addComponent(createPlayerControlledStubComponent(0, false))
        val input = PlayerInputStub(aim = 4, shoot = true)
        serverSimulation.registerSystem(PlayerProcessingTestSystem::class)
        serverSimulation.registerSystem(StepOneShotFiredTestEffectConsumerSystem::class)
        serverSimulation.registerSystem(LagCompensatedActualShotFiredTestEffectConsumer::class)
        serverSimulation.registerSystem(MovementTestSystem::class)
        serverSimulation.registerSystem(EntityShotTestEffectConsumerSystem::class)
        repeat(6) {
            serverSimulation.tick(object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
            })
        }

        serverSimulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input)
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        val input2 = PlayerInputStub(
            aim = 4,
            shoot = false
        )
        serverSimulation.tick(object : SimulationInput {

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input2)
            }
            override fun nonPlayerInput() = object: NonPlayerInput {}

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
            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return emptyMap()
            }
            override fun nonPlayerInput() = object: NonPlayerInput {}

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
        entity.addComponent(createHealthTestComponent(0))
        serverSimulation.registerSystem(RegenerationTestSystem::class)
        serverSimulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem::class)

        serverSimulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
        })

        assertEquals(
            createHealthRegenerationTestEffect(entityId, 1),
            dataForClients.effects(tick = tickProvider.tick).first()
        )
    }

    @Test
    fun `should prepare inputs to be sent to clients`() {
        val playerId = PlayerId(0)
        val damageDealer = state.entities().create(
            ownedBy = Entity.OwnedBy.Player(playerId)
        )
        damageDealer.addComponent(createPlayerControlledStubComponent(0, false))
        serverSimulation.registerSystem(PlayerProcessingTestSystem::class)
        simulationInputHolder.set(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to PlayerInputStub(4, true))
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        serverSimulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to PlayerInputStub(4, true))
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
        })
        assertEquals(
            4,
            (dataForClients.playerInputs()[playerId] as PlayerInputStub).aim,
        )
        assertEquals(
            true,
            (dataForClients.playerInputs()[playerId] as PlayerInputStub).shoot
        )
        simulationInputHolder.set(object : SimulationInput {
            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to PlayerInputStub(4, false))
            }
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun playersSawTicks() = mapOf(playerId to 3L)
        })
        assertEquals(
            4,
            (dataForClients.playerInputs()[playerId] as PlayerInputStub).aim
        )
        assertEquals(
            false,
            (dataForClients.playerInputs()[playerId] as PlayerInputStub).shoot
        )
    }

    private fun CottaState.entities() = entities(tickProvider.tick)
}
