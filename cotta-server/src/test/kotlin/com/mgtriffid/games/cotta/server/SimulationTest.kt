package com.mgtriffid.games.cotta.server

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.name.Names
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.entities.*
import com.mgtriffid.games.cotta.core.input.NonPlayerInput
import com.mgtriffid.games.cotta.core.input.PlayerInput
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.registerComponents
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.core.simulation.PlayersDiff
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

class SimulationTest {
    private lateinit var tickProvider: TickProvider
    private lateinit var simulationInputHolder: SimulationInputHolder
    private lateinit var state: CottaState
    private lateinit var simulation: Simulation
    private lateinit var playersSawTicks: PlayersSawTicks
    private lateinit var dataForClients: DataForClients

    @BeforeEach
    fun setUp() {
        val injector = Guice.createInjector(CottaServerModule(GameStub()))
        tickProvider = injector.getInstance(Key.get(TickProvider::class.java, Names.named(SIMULATION)))
        simulationInputHolder = injector.getInstance(SimulationInputHolder::class.java)
        simulationInputHolder.set(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }

            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())
        })
        state = injector.getInstance(Key.get(CottaState::class.java, Names.named("simulation")))
        simulation = injector.getInstance(Simulation::class.java)
        registerComponents(GameStub(), injector.getInstance(ComponentRegistry::class.java))
        dataForClients = injector.getInstance(DataForClients::class.java)
        playersSawTicks = injector.getInstance(PlayersSawTicks::class.java)
    }

    @Test
    fun `systems should listen to effects`() {
        val entity = state.entities().create()
        val entityId = entity.id
        entity.addComponent(createHealthTestComponent(0))
        simulation.registerSystem(RegenerationTestSystem())
        simulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem())

        simulation.tick(object : SimulationInput {

            override fun nonPlayerInput() = object: NonPlayerInput {}
            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

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
        simulation.registerSystem(RegenerationTestSystem())
        simulation.registerSystem(HealthRegenerationTestEffectsConsumerSystem())
        repeat(2) {
            simulation.tick(object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
                override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

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
        simulation.registerSystem(PlayerProcessingTestSystem())
        simulation.registerSystem(ShotFiredTestEffectConsumerSystem())
        simulation.registerSystem(MovementTestSystem())
        simulation.registerSystem(EntityShotTestEffectConsumerSystem())

        repeat(2) {
            simulation.tick(object : SimulationInput {

                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
                override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

            })
        }

        val input1 = PlayerInputStub(
            aim = 4,
            shoot = true
        )

        simulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input1)
            }

            override fun playersSawTicks() = emptyMap<PlayerId, Long>()
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

        })

        val input2 = PlayerInputStub(
            aim = 4,
            shoot = true
        )

        simulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input2)
            }

            override fun playersSawTicks() = emptyMap<PlayerId, Long>()
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

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
        simulation.registerSystem(PlayerProcessingTestSystem())
        simulation.registerSystem(LagCompensatedShotFiredTestEffectConsumer())
        simulation.registerSystem(MovementTestSystem())
        simulation.registerSystem(EntityShotTestEffectConsumerSystem())
        repeat(6) {
            simulation.tick(object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
                override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

            })
        }

        simulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input)
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

        })
        val input2 = PlayerInputStub(
            aim = 4,
            shoot = false
        )
        simulation.tick(object : SimulationInput {

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input2)
            }
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun playersSawTicks() = mapOf(playerId to 3L)
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

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
        simulation.registerSystem(PlayerProcessingTestSystem())
        simulation.registerSystem(StepOneShotFiredTestEffectConsumerSystem())
        simulation.registerSystem(LagCompensatedActualShotFiredTestEffectConsumer())
        simulation.registerSystem(MovementTestSystem())
        simulation.registerSystem(EntityShotTestEffectConsumerSystem())
        repeat(6) {
            simulation.tick(object : SimulationInput {
                override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                    return emptyMap()
                }
                override fun nonPlayerInput() = object: NonPlayerInput {}

                override fun playersSawTicks(): Map<PlayerId, Long> {
                    return emptyMap()
                }
                override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

            })
        }

        simulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input)
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

        })
        val input2 = PlayerInputStub(
            aim = 4,
            shoot = false
        )
        simulation.tick(object : SimulationInput {

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to input2)
            }
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun playersSawTicks() = mapOf(playerId to 3L)
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

        })

        assertEquals(
            15,
            state.entities().get(damageableId)?.getComponent(HealthTestComponent::class)?.health
        )
    }

    @Test
    fun `should invoke systems`() {
        state.entities().create()
        simulation.registerSystem(BlankTestSystem())

        simulation.tick(object : SimulationInput {
            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return emptyMap()
            }
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

        })

        assertEquals(
            1,
            BlankTestSystem.counter
        )
    }

    @Test
    fun `should prepare inputs to be sent to clients`() {
        val playerId = PlayerId(0)
        val damageDealer = state.entities().create(
            ownedBy = Entity.OwnedBy.Player(playerId)
        )
        damageDealer.addComponent(createPlayerControlledStubComponent(0, false))
        simulation.registerSystem(PlayerProcessingTestSystem())
        simulationInputHolder.set(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to PlayerInputStub(4, true))
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

        })
        simulation.tick(object : SimulationInput {
            override fun nonPlayerInput() = object: NonPlayerInput {}

            override fun inputForPlayers(): Map<PlayerId, PlayerInput> {
                return mapOf(playerId to PlayerInputStub(4, true))
            }

            override fun playersSawTicks() = mapOf(playerId to 2L)
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

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
            override fun playersDiff() = PlayersDiff(emptySet(), emptySet())

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
