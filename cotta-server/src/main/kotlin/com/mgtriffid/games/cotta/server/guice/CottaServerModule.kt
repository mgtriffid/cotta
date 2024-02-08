package com.mgtriffid.games.cotta.server.guice

import com.google.inject.*
import com.google.inject.name.Names.named
import com.mgtriffid.games.cotta.core.CottaGame
import com.mgtriffid.games.cotta.core.NonPlayerInputProvider
import com.mgtriffid.games.cotta.core.clock.CottaClock
import com.mgtriffid.games.cotta.core.clock.impl.CottaClockImpl
import com.mgtriffid.games.cotta.core.effects.EffectBus
import com.mgtriffid.games.cotta.core.effects.impl.EffectBusImpl
import com.mgtriffid.games.cotta.core.entities.CottaState
import com.mgtriffid.games.cotta.core.entities.Entities
import com.mgtriffid.games.cotta.core.entities.TickProvider
import com.mgtriffid.games.cotta.core.entities.impl.AtomicLongTickProvider
import com.mgtriffid.games.cotta.core.entities.impl.CottaStateImpl
import com.mgtriffid.games.cotta.core.guice.SerializationModule
import com.mgtriffid.games.cotta.core.registry.ComponentsRegistryImpl
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.maps.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.impl.EntityOwnerSawTickProviderImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersSawTickImpl
import com.mgtriffid.games.cotta.core.simulation.impl.SimulationInputHolderImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.CreatedEntitiesImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.EntityProcessingContextImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.InputProcessingContextImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.LagCompensatingTracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.SimpleTracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.LagCompensatingInputProcessingSystemInvokerImpl
import com.mgtriffid.games.cotta.core.tracing.Traces
import com.mgtriffid.games.cotta.core.tracing.impl.TracesImpl
import com.mgtriffid.games.cotta.network.CottaServerNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaServerNetworkTransport
import com.mgtriffid.games.cotta.server.*
import com.mgtriffid.games.cotta.server.impl.*

class CottaServerModule(
    private val game: CottaGame
) : Module {
    override fun configure(binder: Binder) {
        with (binder) {
            bind(CottaGameInstance::class.java).to(object : TypeLiteral<CottaGameInstanceImpl>() {})
            bind(CottaGame::class.java).toInstance(game)
            bind(ClientsGhosts::class.java).`in`(Scopes.SINGLETON)

            bind(ComponentsRegistryImpl::class.java).`in`(Scopes.SINGLETON)

            val simulationTickProvider = AtomicLongTickProvider()
            bind(TickProvider::class.java).toInstance(simulationTickProvider)
            bind(CottaClock::class.java).toInstance(CottaClockImpl(simulationTickProvider, game.config.tickLength))
            bind(Int::class.java).annotatedWith(named("historyLength")).toInstance(8)
            bind(Int::class.java).annotatedWith(named("stateHistoryLength")).toInstance(128)
            bind(CottaState::class.java).annotatedWith(named("simulation")).to(CottaStateImpl::class.java).`in`(Scopes.SINGLETON)
            bind(ServerToClientDataDispatcher::class.java)
                .to(object : TypeLiteral<ServerToClientDataDispatcherImpl<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>>(){})

            bind(ServerSimulation::class.java).to(ServerSimulationImpl::class.java).`in`(Scopes.SINGLETON)

            bind(NonPlayerInputProvider::class.java).toInstance(game.nonPlayerInputProvider)
            bind(ServerSimulationInputProvider::class.java).to(ServerSimulationInputProviderImpl::class.java).`in`(Scopes.SINGLETON)
            bind(SimulationInputHolder::class.java).to(SimulationInputHolderImpl::class.java).`in`(Scopes.SINGLETON)
            bind(MetaEntities::class.java).to(MetaEntitiesImpl::class.java).`in`(Scopes.SINGLETON)
            bind(PlayersSawTicks::class.java).to(PlayersSawTickImpl::class.java).`in`(Scopes.SINGLETON)
            bind(InvokersFactory::class.java).to(SimulationInvokersFactory::class.java).`in`(Scopes.SINGLETON)
            bind(SawTickHolder::class.java).toInstance(SawTickHolder(null))
            bind(EffectsHistory::class.java).to(EffectsHistoryImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EffectBus::class.java).to(EffectBusImpl::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingEffectBus::class.java).annotatedWith(named("historical")).to(HistoricalLagCompensatingEffectBus::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingEffectBus::class.java).annotatedWith(named("lagCompensated")).to(LagCompensatingEffectBusImpl::class.java).`in`(Scopes.SINGLETON)
            bind(LagCompensatingInputProcessingSystemInvoker::class.java).to(LagCompensatingInputProcessingSystemInvokerImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EntityOwnerSawTickProvider::class.java).to(EntityOwnerSawTickProviderImpl::class.java).`in`(Scopes.SINGLETON)
            bind(Entities::class.java).annotatedWith(named("latest")).to(LatestEntities::class.java)

            bind(TracingInputProcessingContext::class.java).to(InputProcessingContextImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EntityProcessingContext::class.java).to(EntityProcessingContextImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EffectProcessingContext::class.java).annotatedWith(named("lagCompensated")).to(LagCompensatingTracingEffectProcessingContext::class.java).`in`(Scopes.SINGLETON)
            bind(TracingEffectProcessingContext::class.java).annotatedWith(named("lagCompensated")).to(LagCompensatingTracingEffectProcessingContext::class.java)
            bind(TracingEffectProcessingContext::class.java).annotatedWith(named("simple")).to(
                SimpleTracingEffectProcessingContext::class.java).`in`(Scopes.SINGLETON)
            bind(CreateEntityStrategy::class.java).annotatedWith(named("effectProcessing")).to(
                CreateAndRecordCreateEntityStrategy::class.java).`in`(Scopes.SINGLETON)
            bind(CreatedEntities::class.java).to(CreatedEntitiesImpl::class.java).`in`(Scopes.SINGLETON)
            bind(DataForClients::class.java).to(DataForClientsImpl::class.java).`in`(Scopes.SINGLETON)
            install(SerializationModule())

            bind(Traces::class.java).to(TracesImpl::class.java).`in`(Scopes.SINGLETON)

            bind(PredictedToAuthoritativeIdMappings::class.java).to(PredictedToAuthoritativeIdMappingsImpl::class.java).`in`(Scopes.SINGLETON)
            bind(EntitiesCreatedOnClientsRegistry::class.java).to(EntitiesCreatedOnClientsRegistryImpl::class.java).`in`(Scopes.SINGLETON)
        }
    }

    @Provides @Singleton // TODO game instance scoped
    fun provideCottaServerNetwork(): CottaServerNetworkTransport {
        val network = KryonetCottaServerNetworkTransport()
        network.initialize()
        return network
    }
}
