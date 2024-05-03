package com.mgtriffid.games.cotta.server.guice

import com.google.inject.*
import com.google.inject.name.Names.named
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.PlayersHandler
import com.mgtriffid.games.cotta.core.SIMULATION
import com.mgtriffid.games.cotta.core.input.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.clock.impl.CottaClockImpl
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.impl.EffectBusImpl
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
import com.mgtriffid.games.cotta.core.guice.BytesSerializationModule
import com.mgtriffid.games.cotta.core.input.InputProcessing
import com.mgtriffid.games.cotta.core.registry.ComponentRegistry
import com.mgtriffid.games.cotta.core.registry.impl.ComponentRegistryImpl
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesInputRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesStateRecipe
import com.mgtriffid.games.cotta.core.serialization.bytes.recipe.BytesPlayersDeltaRecipe
import com.mgtriffid.games.cotta.core.simulation.Simulation
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.Players
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.AuthoritativeSimulationImpl
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.impl.EntityOwnerSawTickProviderImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersSawTickImpl
import com.mgtriffid.games.cotta.core.simulation.impl.SimulationInputHolderImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.EntityProcessingContextImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.LagCompensatingEffectProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.SimpleCreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.SimpleEffectProcessingContext
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.acking.AckingCottaServerNetworkTransport
import com.mgtriffid.games.cotta.server.*
import com.mgtriffid.games.cotta.server.impl.*

class CottaServerModule(
    private val game: CottaGame
) : Module {
    override fun configure(binder: Binder) {
        with(binder) {
            bind(CottaGame::class.java).toInstance(game)
            bind(PlayersHandler::class.java).toInstance(game.playersHandler)

            val simulationTickProvider = AtomicLongTickProvider()
            bind(TickProvider::class.java)
                .annotatedWith(named(SIMULATION))
                .toInstance(simulationTickProvider)
            bind(CottaClock::class.java).toInstance(CottaClockImpl(simulationTickProvider, game.config.tickLength))
            bind(Int::class.java).annotatedWith(named("historyLength")).toInstance(8)
            bind(Int::class.java).annotatedWith(named("stateHistoryLength")).toInstance(128)
            bind(CottaState::class.java).annotatedWith(named("simulation")).to(CottaStateImpl::class.java)
                .`in`(Scopes.SINGLETON)

            bind(Simulation::class.java).to(AuthoritativeSimulationImpl::class.java).`in`(Scopes.SINGLETON)
            bind(InputProcessing::class.java).toInstance(game.inputProcessing)

            bind(NonPlayerInputProvider::class.java).toInstance(game.nonPlayerInputProvider)
            bind(SimulationInputHolder::class.java).to(SimulationInputHolderImpl::class.java).`in`(Scopes.SINGLETON)
            bind(Players::class.java).to(PlayersImpl::class.java).`in`(Scopes.SINGLETON)
            bind(PlayersSawTicks::class.java).to(PlayersSawTickImpl::class.java).`in`(Scopes.SINGLETON)
            bind(InvokersFactory::class.java)
                .annotatedWith(named("simulation"))
                .to(SimulationInvokersFactory::class.java).`in`(Scopes.SINGLETON)
            bind(SawTickHolder::class.java).toInstance(SawTickHolder(null))
            bind(EffectsHistory::class.java).to(EffectsHistoryImpl::class.java).`in`(Scopes.SINGLETON)

            bind(EffectBus::class.java).to(EffectBusImpl::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingEffectBus::class.java).annotatedWith(named("historical"))
                .to(HistoricalLagCompensatingEffectBus::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingEffectBus::class.java).annotatedWith(named("lagCompensated"))
                .to(LagCompensatingEffectBusImpl::class.java).`in`(Scopes.SINGLETON)

            bind(EntityOwnerSawTickProvider::class.java).to(EntityOwnerSawTickProviderImpl::class.java)
                .`in`(Scopes.SINGLETON)
            bind(Entities::class.java).annotatedWith(named("latest")).to(LatestEntities::class.java)

            bind(EntityProcessingContext::class.java).to(EntityProcessingContextImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EffectProcessingContext::class.java).annotatedWith(named("lagCompensated"))
                .to(LagCompensatingEffectProcessingContext::class.java).`in`(Scopes.SINGLETON)
            bind(EffectProcessingContext::class.java).annotatedWith(named("simple")).to(
                SimpleEffectProcessingContext::class.java
            ).`in`(Scopes.SINGLETON)
            bind(CreateEntityStrategy::class.java).annotatedWith(named("effectProcessing")).to(
                SimpleCreateEntityStrategy::class.java
            ).`in`(Scopes.SINGLETON)
            bind(DataForClients::class.java).to(DataForClientsImpl::class.java).`in`(Scopes.SINGLETON)
            install(BytesSerializationModule(game.playerInputKClass))
            bind(ServerToClientDataDispatcher::class.java)
                .to(object :
                    TypeLiteral<ServerToClientDataDispatcherImpl<
                        BytesStateRecipe,
                        BytesDeltaRecipe,
                        BytesInputRecipe,
                        BytesPlayersDeltaRecipe
                        >>() {})
                .`in`(Scopes.SINGLETON)
            bind(CottaGameInstance::class.java).to(object :
                TypeLiteral<CottaGameInstanceImpl<BytesInputRecipe>>() {})

            bind(ServerSimulationInputProvider::class.java)
                .to(object : TypeLiteral<ServerSimulationInputProviderImpl<
                    BytesInputRecipe
                    >>() {})
                .`in`(Scopes.SINGLETON)

            bind(object : TypeLiteral<ClientsGhosts<BytesInputRecipe>>() {}).`in`(Scopes.SINGLETON)
            bind(ComponentRegistry::class.java).to(ComponentRegistryImpl::class.java).`in`(Scopes.SINGLETON)
        }
    }

    @Provides
    @Singleton
    fun provideCottaServerNetwork(): CottaServerNetworkTransport {
        val network = AckingCottaServerNetworkTransport(
            game.config.network.ports.tcp,
            game.config.network.ports.udp
        )
        network.initialize()
        return network
    }
}
