package com.mgtriffid.games.cotta.server

import com.mgtriffid.games.cotta.core.effects.impl.EffectBusImpl
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entity
import com.mgtriffid.games.cotta.core.entities.EntityId
import com.mgtriffid.games.cotta.core.entities.InputComponent
import com.mgtriffid.games.cotta.core.entities.PlayerId
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInput
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersSawTickImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.InputProcessingContextImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.LagCompensatingInputProcessingSystemInvokerImpl
import com.mgtriffid.games.cotta.server.impl.MetaEntitiesImpl
import com.mgtriffid.games.cotta.server.impl.ServerSimulationImpl
import com.mgtriffid.games.cotta.server.impl.SimulationInputHolderImpl
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

class ServerSimulationTest {
    private lateinit var tickProvider: TickProvider
    private lateinit var simulationInputHolder: SimulationInputHolder

    @BeforeEach
    fun setUp() {
        tickProvider = AtomicLongTickProvider()
        simulationInputHolder = SimulationInputHolderImpl()
        simulationInputHolder.set(object : SimulationInput {
            override fun inputsForEntities(): Map<EntityId, Collection<InputComponent<*>>> {
                return emptyMap()
            }

            override fun playersSawTicks(): Map<PlayerId, Long> {
                return emptyMap()
            }
        })
    }

    @Test
    fun `systems should listen to effects`() {
        val state = getCottaState()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = getServerSimulation(state)
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
        val state = getCottaState()
        val entity = state.entities().createEntity()
        val entityId = entity.id
        entity.addComponent(HealthTestComponent.create(0))
        val serverSimulation = getServerSimulation(state)
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
        val state = getCottaState()
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        val serverSimulation = getServerSimulation(state)
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
        val state = getCottaState()
        val damageable = state.entities().createEntity()
        val damageableId = damageable.id
        damageable.addComponent(HealthTestComponent.create(20))
        damageable.addComponent(LinearPositionTestComponent.create(0))
        damageable.addComponent(VelocityTestComponent.create(2))

        val damageDealer = state.entities().createEntity(ownedBy = Entity.OwnedBy.Player(playerId))
        val damageDealerId = damageDealer.id
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val input = PlayerInputTestComponent.create(aim = 4, shoot = true)
        val serverSimulation = getServerSimulation(state)
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
        val state = getCottaState()
        state.entities().createEntity()
        val serverSimulation = getServerSimulation(state)
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
        val serverSimulation = getServerSimulation(state)
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
        val state = getCottaState()
        val damageDealer = state.entities().createEntity()
        damageDealer.addInputComponent(PlayerInputTestComponent::class)
        val damageDealerId = damageDealer.id
        val serverSimulation = getServerSimulation(state)
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


    private fun getCottaState() = CottaState.getInstance(tickProvider)

    private fun getServerSimulation(state: CottaState): ServerSimulation {
        val sawTickHolder = InvokersFactoryImpl.SawTickHolder(null)
        val effectBus = EffectBusImpl()
        val effectsHistory = EffectsHistoryImpl(8)
        val lagCompensatingEffectBus = LagCompensatingEffectBusImpl(
            effectBus = effectBus,
            sawTickHolder = sawTickHolder,
        )
        val historicalLagCompensatingEffectBus = HistoricalLagCompensatingEffectBus(
            history = effectsHistory,
            impl = lagCompensatingEffectBus,
            tickProvider = tickProvider
        )
        val playersSawTicks = PlayersSawTickImpl(simulationInputHolder)
        return ServerSimulationImpl(
            state = state,
            tickProvider = tickProvider,
            historyLength = 8,
            simulationInputHolder = simulationInputHolder,
            metaEntities = MetaEntitiesImpl(),
            invokersFactory = InvokersFactoryImpl(
                lagCompensatingEffectBus = historicalLagCompensatingEffectBus,
                state = state,
                playersSawTicks = playersSawTicks,
                sawTickHolder = sawTickHolder,
                lagCompensatingEffectsConsumerInvoker = LagCompensatingEffectsConsumerInvoker(
                    effectBus = historicalLagCompensatingEffectBus,
                    sawTickHolder = sawTickHolder
                ),
                simpleEffectsConsumerSystemInvoker = SimpleEffectsConsumerSystemInvoker(lagCompensatingEffectBus),
                entityProcessingSystemInvoker = EntityProcessingSystemInvoker(state),
                lagCompensatingInputProcessingSystemInvoker = LagCompensatingInputProcessingSystemInvokerImpl(
                    LatestEntities(state),
                    entityOwnerSawTickProvider = object : EntityOwnerSawTickProvider {
                        override fun getSawTickByEntity(entity: Entity): Long? {
                            return (entity.ownedBy as? Entity.OwnedBy.Player)?.let { playersSawTicks[it.playerId] }
                        }
                    },
                    sawTickHolder = sawTickHolder,
                    InputProcessingContextImpl(
                        lagCompensatingEffectBus = lagCompensatingEffectBus
                    )
                )
            ),
            effectBus = effectBus,
            playersSawTicks = object : PlayersSawTicks {
                override fun get(playerId: PlayerId): Long? {
                    return simulationInputHolder.get().playersSawTicks()[playerId]
                }
            },
            effectsHistory = effectsHistory
        )
    }
}
