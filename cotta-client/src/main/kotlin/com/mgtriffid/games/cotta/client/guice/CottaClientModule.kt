package com.mgtriffid.games.cotta.client.guice

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import com.mgtriffid.games.cotta.client.*
import com.mgtriffid.games.cotta.client.impl.*
import com.mgtriffid.games.cotta.client.invokers.PredictedInputProcessingSystemInvoker
import com.mgtriffid.games.cotta.client.invokers.PredictionEffectsConsumerSystemInvoker
import com.mgtriffid.games.cotta.client.invokers.PredictionEntityProcessingSystemInvoker
import com.mgtriffid.games.cotta.client.invokers.PredictionInvokersFactory
import com.mgtriffid.games.cotta.client.invokers.impl.*
import com.mgtriffid.games.cotta.client.network.NetworkClient
import com.mgtriffid.games.cotta.client.network.impl.NetworkClientImpl
import com.mgtriffid.games.cotta.core.CottaGame
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
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsDeltaRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsInputRecipe
import com.mgtriffid.games.cotta.core.serialization.impl.recipe.MapsStateRecipe
import com.mgtriffid.games.cotta.core.simulation.EffectsHistory
import com.mgtriffid.games.cotta.core.simulation.EntityOwnerSawTickProvider
import com.mgtriffid.games.cotta.core.simulation.PlayersSawTicks
import com.mgtriffid.games.cotta.core.simulation.SimulationInputHolder
import com.mgtriffid.games.cotta.core.simulation.impl.EffectsHistoryImpl
import com.mgtriffid.games.cotta.core.simulation.impl.EntityOwnerSawTickProviderImpl
import com.mgtriffid.games.cotta.core.simulation.impl.PlayersSawTickImpl
import com.mgtriffid.games.cotta.core.simulation.impl.SimulationInputHolderImpl
import com.mgtriffid.games.cotta.core.simulation.invokers.*
import com.mgtriffid.games.cotta.core.simulation.invokers.context.CreateEntityStrategy
import com.mgtriffid.games.cotta.core.simulation.invokers.context.EntityProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingEffectProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.TracingInputProcessingContext
import com.mgtriffid.games.cotta.core.simulation.invokers.context.impl.*
import com.mgtriffid.games.cotta.core.simulation.invokers.impl.LagCompensatingInputProcessingSystemInvokerImpl
import com.mgtriffid.games.cotta.core.tracing.Traces
import com.mgtriffid.games.cotta.core.tracing.impl.TracesImpl
import com.mgtriffid.games.cotta.network.CottaClientNetworkTransport
import com.mgtriffid.games.cotta.network.kryonet.KryonetCottaClientNetworkTransport

class CottaClientModule(
    private val game: CottaGame,
    private val input: CottaClientInput
) : AbstractModule() {
    override fun configure() {
        bind(CottaGame::class.java).toInstance(game)
        bind(CottaClientInput::class.java).toInstance(input)
        bind(CottaClientNetworkTransport::class.java).to(KryonetCottaClientNetworkTransport::class.java).`in`(Scopes.SINGLETON)
        bind(NetworkClient::class.java)
            .to(NetworkClientImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CottaClient::class.java)
            .to(object: TypeLiteral<CottaClientImpl>(){})
        bind(Int::class.java).annotatedWith(Names.named("historyLength")).toInstance(8)
        bind(Int::class.java).annotatedWith(Names.named("stateHistoryLength")).toInstance(128)

        bind(ClientSimulation::class.java).to(ClientSimulationImpl::class.java).`in`(Scopes.SINGLETON)

        val simulationTickProvider = AtomicLongTickProvider()
        bind(TickProvider::class.java).toInstance(simulationTickProvider)
        bind(CottaClock::class.java).toInstance(CottaClockImpl(simulationTickProvider, game.config.tickLength))

        bind(CottaState::class.java).annotatedWith(Names.named("simulation")).to(CottaStateImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SimulationInputHolder::class.java).to(SimulationInputHolderImpl::class.java).`in`(Scopes.SINGLETON)
        bind(object : TypeLiteral<ClientIncomingDataBuffer<MapsStateRecipe, MapsDeltaRecipe, MapsInputRecipe>>() {})
            .toInstance(ClientIncomingDataBuffer())

        bind(InvokersFactory::class.java)
            .annotatedWith(Names.named("simulation"))
            .to(SimulationInvokersFactory::class.java)
            .`in`(Scopes.SINGLETON)

        bind(InvokersFactory::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionInvokersFactory::class.java)
            .`in`(Scopes.SINGLETON)

        bind(PredictedInputProcessingSystemInvoker::class.java).to(PredictedInputProcessingSystemInvokerImpl::class.java).`in`(Scopes.SINGLETON)

        bind(SawTickHolder::class.java).toInstance(SawTickHolder(null))
        bind(EffectsHistory::class.java).to(EffectsHistoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EffectBus::class.java).to(EffectBusImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EffectBus::class.java).annotatedWith(Names.named("prediction")).to(EffectBusImpl::class.java).`in`(Scopes.SINGLETON)
        bind(LagCompensatingEffectBus::class.java).annotatedWith(Names.named("historical")).to(
            HistoricalLagCompensatingEffectBus::class.java).`in`(Scopes.SINGLETON)
        bind(LagCompensatingEffectBus::class.java).annotatedWith(Names.named("lagCompensated")).to(
            LagCompensatingEffectBusImpl::class.java).`in`(Scopes.SINGLETON)
        bind(LagCompensatingInputProcessingSystemInvoker::class.java).to(LagCompensatingInputProcessingSystemInvokerImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EntityOwnerSawTickProvider::class.java).to(EntityOwnerSawTickProviderImpl::class.java).`in`(Scopes.SINGLETON)
        bind(Entities::class.java).annotatedWith(Names.named("latest")).to(LatestEntities::class.java)
        bind(TracingInputProcessingContext::class.java).to(InputProcessingContextImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EntityProcessingContext::class.java).to(EntityProcessingContextImpl::class.java).`in`(Scopes.SINGLETON)
        bind(TracingEffectProcessingContext::class.java)
            .annotatedWith(Names.named("lagCompensated"))
            .to(LagCompensatingTracingEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        bind(TracingEffectProcessingContext::class.java)
            .annotatedWith(Names.named("simple"))
            .to(SimpleTracingEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PlayersSawTicks::class.java).to(PlayersSawTickImpl::class.java).`in`(Scopes.SINGLETON)
        bind(Traces::class.java).to(TracesImpl::class.java).`in`(Scopes.SINGLETON)
        bind(Traces::class.java).annotatedWith(Names.named("prediction")).to(TracesImpl::class.java).`in`(Scopes.SINGLETON)

        bind(CreateEntityStrategy::class.java).annotatedWith(Names.named("effectProcessing")).to(UseIdFromServerCreateEntityStrategy::class.java).`in`(Scopes.SINGLETON)
        bind(ServerCreatedEntitiesRegistry::class.java).`in`(Scopes.SINGLETON)

        bind(PredictionSimulation::class.java).to(PredictionSimulationImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CottaState::class.java).annotatedWith(Names.named("prediction")).to(CottaStateImpl::class.java).`in`(Scopes.SINGLETON)
        bind(TracingInputProcessingContext::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionInputProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        val predictionTickProvider = AtomicLongTickProvider()
        bind(TickProvider::class.java)
            .annotatedWith(Names.named("prediction"))
            .toInstance(predictionTickProvider)
        val predictionClock = PredictionCottaClockImpl(predictionTickProvider, game.config.tickLength)
        bind(CottaClock::class.java)
            .annotatedWith(Names.named("prediction"))
            .toInstance(predictionClock)
        bind(PredictionCottaClockImpl::class.java).toInstance(predictionClock)
        bind(Entities::class.java).annotatedWith(Names.named("prediction")).to(PredictedLatestEntities::class.java)
        bind(PredictionEffectsConsumerSystemInvoker::class.java)
            .to(PredictionEffectsConsumerSystemInvokerImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PredictionEntityProcessingSystemInvoker::class.java)
            .to(PredictionEntityProcessingSystemInvokerImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(TracingEffectProcessingContext::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionTracingEffectProcessingContext::class.java)
            .`in`(Scopes.SINGLETON)
        bind(ClientInputs::class.java).to(ClientInputsImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CreateEntityStrategy::class.java)
            .annotatedWith(Names.named("prediction"))
            .to(PredictionCreateEntityStrategy::class.java)
            .`in`(Scopes.SINGLETON)
        bind(PredictedCreatedEntitiesRegistry::class.java).to(PredictedCreatedEntitiesRegistryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PredictedEntityIdGenerator::class.java).to(PredictedEntityIdGeneratorImpl::class.java).`in`(Scopes.SINGLETON)
        bind(Int::class.java).annotatedWith(Names.named("clientInputBufferLength")).toInstance(128)
        bind(LocalPlayer::class.java).toInstance(LocalPlayer())

        bind(AuthoritativeToPredictedEntityIdMappings::class.java).to(AuthoritativeToPredictedEntityIdMappingsImpl::class.java).`in`(Scopes.SINGLETON)
        install(SerializationModule())
    }
}
